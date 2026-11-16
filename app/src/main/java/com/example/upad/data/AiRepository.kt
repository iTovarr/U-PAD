package com.example.upad.data

import com.example.upad.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AiRepository {
    private val client = OkHttpClient()

    suspend fun getRoutineSuggestions(routineTurn: String): List<String> = withContext(Dispatchers.IO) {
        val body = """
            {
                "model": "llama-3.3-70b-versatile",
                "max_tokens": 200,
                "messages": [
                    {
                        "role": "system",
                        "content": "Eres un psicopedagogo experto en autismo (TEA). Sugiere exactamente 3 actividades, una por linea, empezando con numero y punto. Maximo 4 palabras cada una. Sin saludos ni explicaciones."
                    },
                    {
                        "role": "user",
                        "content": "Dame 3 actividades para la rutina de la $routineTurn de un nino con TEA."
                    }
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error de API: ${response.code}")
            }
            val jsonResponse = response.body?.string() ?: ""
            val jsonObj = JSONObject(jsonResponse)

            if (jsonObj.has("error")) {
                val errorMsg = jsonObj.getJSONObject("error").optString("message", "Error desconocido")
                throw IOException(errorMsg)
            }

            val rawText = jsonObj
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            val suggestions = mutableListOf<String>()
            if (rawText.isNotEmpty()) {
                val lines = rawText.split("\n")
                for (line in lines) {
                    val cleaned = line
                        .replace(Regex("^[0-9]+\\.\\s*"), "")
                        .trim()
                    if (cleaned.isNotEmpty()) {
                        suggestions.add(cleaned)
                    }
                }
            }
            suggestions
        }
    }

    suspend fun getPersonalizedAnalysis(tasksReport: String): String = withContext(Dispatchers.IO) {
        val promptEscapado = JSONObject.quote(tasksReport)
        val body = """
            {
                "model": "llama-3.3-70b-versatile",
                "max_tokens": 400,
                "messages": [
                    {
                        "role": "system",
                        "content": "Eres un psicopedagogo experto en autismo (TEA). En base al reporte de actividades y emociones del niño que se te proporciona, realiza un análisis personalizado de máximo 3 párrafos cortos (100 palabras en total). Sugiere recomendaciones empáticas y concretas para mejorar su bienestar y rutinas. Sé empático, claro y estructurado."
                    },
                    {
                        "role": "user",
                        "content": $promptEscapado
                    }
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error de API: ${response.code}")
            }
            val jsonResponse = response.body?.string() ?: ""
            val jsonObj = JSONObject(jsonResponse)

            if (jsonObj.has("error")) {
                val errorMsg = jsonObj.getJSONObject("error").optString("message", "Error desconocido")
                throw IOException(errorMsg)
            }

            jsonObj
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }

    suspend fun getCustomRoutineAndReinforcements(need: String): Pair<List<String>, List<String>> = withContext(Dispatchers.IO) {
        val promptEscapado = JSONObject.quote(need)
        val body = """
            {
                "model": "llama-3.3-70b-versatile",
                "max_tokens": 500,
                "messages": [
                    {
                        "role": "system",
                        "content": "Eres un psicopedagogo experto en autismo (TEA). Diseña una rutina de máximo 5 actividades extremadamente cortas, puntuales y simples (máximo de 3 palabras por actividad) para que un niño con autismo aprenda o logre lo solicitado por el padre. Cada actividad debe ser una acción física directa y fácil de entender. Genera también 3 mensajes muy cortos de refuerzo positivo dirigidos directamente al niño al completar las tareas. Responde estrictamente con el siguiente formato, sin saludos ni comentarios:\nRUTINA:\n1. Tarea muy corta\n2. Tarea muy corta\n3. Tarea muy corta\nREFUERZOS:\n- Mensaje 1\n- Mensaje 2\n- Mensaje 3"
                    },
                    {
                        "role": "user",
                        "content": $promptEscapado
                    }
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error de API: ${response.code}")
            }
            val jsonResponse = response.body?.string() ?: ""
            val jsonObj = JSONObject(jsonResponse)

            if (jsonObj.has("error")) {
                val errorMsg = jsonObj.getJSONObject("error").optString("message", "Error desconocido")
                throw IOException(errorMsg)
            }

            val rawText = jsonObj
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            val routineList = mutableListOf<String>()
            val reinforcementsList = mutableListOf<String>()
            var readingRoutine = false
            var readingReinforcements = false

            if (rawText.isNotEmpty()) {
                val lines = rawText.split("\n")
                for (line in lines) {
                    val upperLine = line.uppercase().trim()
                    if (upperLine.startsWith("RUTINA")) {
                        readingRoutine = true
                        readingReinforcements = false
                        continue
                    }
                    if (upperLine.startsWith("REFUERZO")) {
                        readingRoutine = false
                        readingReinforcements = true
                        continue
                    }

                    if (readingRoutine) {
                        val cleaned = line.replace(Regex("^[0-9]+\\.\\s*"), "").trim()
                        if (cleaned.isNotEmpty()) routineList.add(cleaned)
                    } else if (readingReinforcements) {
                        val cleaned = line.replace(Regex("^[-*•]\\s*"), "").trim()
                        if (cleaned.isNotEmpty()) reinforcementsList.add(cleaned)
                    }
                }
            }
            Pair(routineList, reinforcementsList)
        }
    }
}
