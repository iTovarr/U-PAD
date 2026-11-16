package com.example.upad.data

/**
 * Operaciones de IA que usa la app. Se define como interfaz para poder sustituir
 * la implementación real ([AiRepository]) por un doble en los tests.
 */
interface AiService {
    /** Tres actividades cortas sugeridas para el turno indicado. */
    suspend fun getRoutineSuggestions(routineTurn: String): List<String>

    /** Análisis y recomendaciones a partir de un reporte de tareas y emociones. */
    suspend fun getPersonalizedAnalysis(tasksReport: String): String

    /** Rutina a medida para una necesidad concreta, junto con sus mensajes de refuerzo. */
    suspend fun getCustomRoutineAndReinforcements(need: String): Pair<List<String>, List<String>>
}
