package com.example.upad.data

/**
 * Operaciones de IA que usa la app. Se define como interfaz para poder sustituir
 * la implementación real ([AiRepository]) por un doble en los tests.
 *
 * `languageCode` es el idioma de la app ("es", "en"...): la respuesta llega en ese idioma.
 */
interface AiService {
    /** Tres actividades cortas sugeridas para el turno indicado. */
    suspend fun getRoutineSuggestions(routineTurn: String, languageCode: String): List<String>

    /** Análisis y recomendaciones a partir de un reporte de tareas y emociones. */
    suspend fun getPersonalizedAnalysis(tasksReport: String, languageCode: String): String

    /** Rutina a medida para una necesidad concreta, junto con sus mensajes de refuerzo. */
    suspend fun getCustomRoutineAndReinforcements(need: String, languageCode: String): Pair<List<String>, List<String>>
}
