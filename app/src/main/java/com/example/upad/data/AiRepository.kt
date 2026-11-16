package com.example.upad.data

import com.example.upad.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiRepository : AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun getRoutineSuggestions(routineTurn: String): List<String> {
        val rawText = chat(
            systemPrompt = "Eres un psicopedagogo experto en autismo (TEA). Sugiere exactamente 3 actividades, una por linea, empezando con numero y punto. Maximo 4 palabras cada una. Sin saludos ni explicaciones.",
            userPrompt = "Dame 3 actividades para la rutina de la $routineTurn de un nino con TEA.",
            maxTokens = 200
        )
        return parsearSugerencias(rawText)
    }

    override suspend fun getPersonalizedAnalysis(tasksReport: String): String {
        return chat(
            systemPrompt = "Eres un psicopedagogo experto en autismo (TEA). En base al reporte de actividades y emociones del niño que se te proporciona, realiza un análisis personalizado de máximo 3 párrafos cortos (100 palabras en total). Sugiere recomendaciones empáticas y concretas para mejorar su bienestar y rutinas. Sé empático, claro y estructurado.",
            userPrompt = tasksReport,
            maxTokens = 400
        ).trim()
    }

    override suspend fun getCustomRoutineAndReinforcements(need: String): Pair<List<String>, List<String>> {
        val rawText = chat(
            systemPrompt = "Eres un psicopedagogo experto en autismo (TEA). Diseña una rutina de máximo 5 actividades extremadamente cortas, puntuales y simples (máximo de 3 palabras por actividad) para que un niño con autismo aprenda o logre lo solicitado por el padre. Cada actividad debe ser una acción física directa y fácil de entender. Genera también 3 mensajes muy cortos de refuerzo positivo dirigidos directamente al niño al completar las tareas. Responde estrictamente con el siguiente formato, sin saludos ni comentarios:\nRUTINA:\n1. Tarea muy corta\n2. Tarea muy corta\n3. Tarea muy corta\nREFUERZOS:\n- Mensaje 1\n- Mensaje 2\n- Mensaje 3",
            userPrompt = need,
            maxTokens = 500
        )
        return parsearRutinaYRefuerzos(rawText)
    }

    /**
     * Envía una conversación de un solo turno al modelo y devuelve el texto de la respuesta.
     *
     * El cuerpo se construye con [JSONObject] para que cualquier texto escrito por
     * el usuario (comillas, saltos de línea, etc.) quede escapado correctamente.
     */
    private suspend fun chat(systemPrompt: String, userPrompt: String, maxTokens: Int): String =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("model", MODEL)
                .put("max_tokens", maxTokens)
                .put(
                    "messages",
                    JSONArray()
                        .put(JSONObject().put("role", "system").put("content", systemPrompt))
                        .put(JSONObject().put("role", "user").put("content", userPrompt))
                )
                .toString()

            val request = Request.Builder()
                .url(endpoint())
                .post(body.toRequestBody(JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer ${credencial()}")
                .build()

            // Los fallos pasajeros (sin red, límite de peticiones, error del servidor) se
            // reintentan con una espera creciente antes de mostrar el error al tutor.
            var ultimoError: IOException? = null
            repeat(MAX_INTENTOS) { intento ->
                if (intento > 0) delay(ESPERA_ENTRE_INTENTOS_MS * intento)
                try {
                    return@withContext ejecutar(request)
                } catch (e: ErrorReintentable) {
                    ultimoError = e
                }
            }
            throw ultimoError ?: IOException("Error desconocido")
        }

    /** Fallo que merece otro intento: problema de red, 429 o error 5xx. */
    private class ErrorReintentable(message: String, cause: Throwable? = null) : IOException(message, cause)

    private fun ejecutar(request: Request): String {
        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            throw ErrorReintentable(e.message ?: "Error de red", e)
        }

        response.use {
            if (!response.isSuccessful) {
                val mensaje = "Error de API: ${response.code}"
                if (response.code == 429 || response.code >= 500) throw ErrorReintentable(mensaje)
                throw IOException(mensaje)
            }
            val jsonObj = JSONObject(response.body?.string() ?: "")

            if (jsonObj.has("error")) {
                val errorMsg = jsonObj.getJSONObject("error").optString("message", "Error desconocido")
                throw IOException(errorMsg)
            }

            return jsonObj
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun usaProxy(): Boolean = BuildConfig.AI_PROXY_URL.isNotBlank()

    /** Con proxy configurado se llama a la Cloud Function; si no, a Groq directamente. */
    private fun endpoint(): String = if (usaProxy()) BuildConfig.AI_PROXY_URL else GROQ_URL

    /**
     * Con proxy se envía el ID token de Firebase del tutor (la clave de Groq vive
     * solo en el servidor). Sin proxy se usa la clave incluida en el build, lo que
     * solo es aceptable en desarrollo.
     */
    private suspend fun credencial(): String {
        if (!usaProxy()) return BuildConfig.GROQ_API_KEY
        val usuario = FirebaseAuth.getInstance().currentUser
            ?: throw IOException("Inicia sesión para usar las sugerencias con IA")
        return usuario.getIdToken(false).await().token
            ?: throw IOException("No se pudo validar la sesión")
    }

    companion object {
        private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL = "llama-3.3-70b-versatile"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val MAX_INTENTOS = 3
        private const val ESPERA_ENTRE_INTENTOS_MS = 1_000L

        private val PREFIJO_NUMERADO = Regex("^[0-9]+\\.\\s*")
        private val PREFIJO_VINETA = Regex("^[-*•]\\s*")

        /** Convierte una lista numerada ("1. Lavarse las manos") en sus elementos sin numeración. */
        internal fun parsearSugerencias(rawText: String): List<String> =
            rawText.split("\n")
                .map { it.trim().replace(PREFIJO_NUMERADO, "").trim() }
                .filter { it.isNotEmpty() }

        /** Separa la respuesta con secciones "RUTINA:" y "REFUERZOS:" en sus dos listas. */
        internal fun parsearRutinaYRefuerzos(rawText: String): Pair<List<String>, List<String>> {
            val rutina = mutableListOf<String>()
            val refuerzos = mutableListOf<String>()
            var leyendoRutina = false
            var leyendoRefuerzos = false

            for (line in rawText.split("\n")) {
                val linea = line.trim()
                val mayusculas = linea.uppercase()
                when {
                    mayusculas.startsWith("RUTINA") -> {
                        leyendoRutina = true
                        leyendoRefuerzos = false
                    }
                    mayusculas.startsWith("REFUERZO") -> {
                        leyendoRutina = false
                        leyendoRefuerzos = true
                    }
                    leyendoRutina -> {
                        val limpio = linea.replace(PREFIJO_NUMERADO, "").trim()
                        if (limpio.isNotEmpty()) rutina.add(limpio)
                    }
                    leyendoRefuerzos -> {
                        val limpio = linea.replace(PREFIJO_VINETA, "").trim()
                        if (limpio.isNotEmpty()) refuerzos.add(limpio)
                    }
                }
            }
            return Pair(rutina, refuerzos)
        }
    }
}
