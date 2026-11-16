/**
 * Cloud Functions de U-Pad.
 *
 * aiChat: proxy de IA. La app Android llama a esta función en lugar de llamar a
 * Groq directamente, de modo que la clave de Groq vive solo en el servidor
 * (Secret Manager) y no viaja dentro del APK. Solo atiende a usuarios con sesión
 * iniciada en Firebase Auth y fija el modelo y los límites de tamaño para que
 * nadie pueda usarla como proxy genérico.
 *
 * notifyParent: envía una notificación push al tutor cada vez que el dispositivo
 * del niño deja un aviso en users/{uid}/notifications, para que le llegue aunque
 * tenga la app cerrada.
 */
const {onRequest} = require("firebase-functions/v2/https");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {defineSecret} = require("firebase-functions/params");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");

admin.initializeApp();

const GROQ_API_KEY = defineSecret("GROQ_API_KEY");

const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
const MODEL = "llama-3.3-70b-versatile";
const MAX_TOKENS = 500;
const MAX_MESSAGES = 4;
const MAX_TOTAL_CHARS = 8000;
const ALLOWED_ROLES = ["system", "user"];

function sendError(res, status, message) {
  res.status(status).json({error: {message}});
}

/** Devuelve los mensajes validados o null si el cuerpo no es aceptable. */
function validateMessages(messages) {
  if (!Array.isArray(messages) || messages.length === 0 || messages.length > MAX_MESSAGES) {
    return null;
  }
  let totalChars = 0;
  const clean = [];
  for (const message of messages) {
    if (!message || !ALLOWED_ROLES.includes(message.role) || typeof message.content !== "string") {
      return null;
    }
    totalChars += message.content.length;
    clean.push({role: message.role, content: message.content});
  }
  return totalChars <= MAX_TOTAL_CHARS ? clean : null;
}

exports.aiChat = onRequest(
    {secrets: [GROQ_API_KEY], region: "us-central1", maxInstances: 5, timeoutSeconds: 60},
    async (req, res) => {
      if (req.method !== "POST") {
        return sendError(res, 405, "Método no permitido");
      }

      // 1. Solo usuarios autenticados en Firebase.
      const header = req.get("Authorization") || "";
      const idToken = header.startsWith("Bearer ") ? header.slice("Bearer ".length) : "";
      let uid;
      try {
        uid = (await admin.auth().verifyIdToken(idToken)).uid;
      } catch (error) {
        return sendError(res, 401, "Sesión no válida");
      }

      // 2. Solo conversaciones con la forma y el tamaño esperados.
      const messages = validateMessages(req.body && req.body.messages);
      if (!messages) {
        return sendError(res, 400, "Solicitud no válida");
      }
      const requested = Number(req.body.max_tokens);
      const maxTokens = Number.isInteger(requested) && requested > 0 ?
        Math.min(requested, MAX_TOKENS) :
        MAX_TOKENS;

      // 3. Se reenvía a Groq con el modelo fijado en el servidor.
      try {
        const groqResponse = await fetch(GROQ_URL, {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${GROQ_API_KEY.value()}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({model: MODEL, max_tokens: maxTokens, messages}),
        });
        const data = await groqResponse.json();
        if (!groqResponse.ok) {
          logger.warn("Groq respondió con error", {uid, status: groqResponse.status});
          return sendError(res, 502, "El servicio de IA no está disponible");
        }
        return res.status(200).json(data);
      } catch (error) {
        logger.error("Fallo al llamar a Groq", {uid, message: error.message});
        return sendError(res, 502, "El servicio de IA no está disponible");
      }
    },
);

// Debe coincidir con NotificationHelper.CHANNEL_ID de la app Android.
const NOTIFICATION_CHANNEL_ID = "upad_notifications_channel";
const INVALID_TOKEN_CODES = [
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
  "messaging/invalid-argument",
];

exports.notifyParent = onDocumentCreated(
    {document: "users/{uid}/notifications/{notificationId}", region: "us-central1"},
    async (event) => {
      const aviso = event.data && event.data.data();
      if (!aviso) return;

      const {uid, notificationId} = event.params;
      const userRef = admin.firestore().collection("users").doc(uid);
      const tokens = ((await userRef.get()).get("fcmTokens") || []).filter((t) => typeof t === "string");
      if (tokens.length === 0) return;

      const response = await admin.messaging().sendEachForMulticast({
        tokens,
        notification: {
          title: String(aviso.title || "U-Pad"),
          body: String(aviso.description || ""),
        },
        android: {
          priority: "high",
          notification: {
            channelId: NOTIFICATION_CHANNEL_ID,
            // La app usa el mismo tag al mostrar el aviso en primer plano, así que
            // Android lo reemplaza en lugar de enseñarlo dos veces.
            tag: notificationId,
          },
        },
      });

      // Se retiran los tokens de dispositivos que ya no existen o desinstalaron la app.
      const invalidos = tokens.filter((_, i) => {
        const error = response.responses[i].error;
        return error && INVALID_TOKEN_CODES.includes(error.code);
      });
      if (invalidos.length > 0) {
        await userRef.update({fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalidos)});
      }
      logger.info("Aviso enviado al tutor", {
        uid,
        enviados: response.successCount,
        fallidos: response.failureCount,
      });
    },
);
