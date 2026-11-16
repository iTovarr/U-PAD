/**
 * Proxy de IA de U-Pad.
 *
 * La app Android llama a esta función en lugar de llamar a Groq directamente,
 * de modo que la clave de Groq vive solo en el servidor (Secret Manager) y no
 * viaja dentro del APK.
 *
 * Solo atiende a usuarios con sesión iniciada en Firebase Auth y fija el modelo
 * y los límites de tamaño para que nadie pueda usarla como proxy genérico.
 */
const {onRequest} = require("firebase-functions/v2/https");
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
