package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upad.data.AiService
import com.example.upad.utils.RoutineProgressCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estado y acciones del asistente de IA: sugerencias, análisis del día y rutinas a medida. */
class AiViewModel(
    private val aiService: AiService,
    languageFlow: Flow<String>
) : ViewModel() {

    /** Idioma de la app: la IA responde en este idioma. */
    private var idioma = "es"

    init {
        viewModelScope.launch {
            languageFlow.collect { idioma = it }
        }
    }

    // Sugerencias rápidas por turno
    private val _aiSuggestions = MutableStateFlow<List<String>>(emptyList())
    val aiSuggestions: StateFlow<List<String>> = _aiSuggestions.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // Análisis personalizado (Premium)
    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Rutina a medida con mensajes de refuerzo (Premium)
    private val _aiCustomRoutine = MutableStateFlow<List<String>>(emptyList())
    val aiCustomRoutine: StateFlow<List<String>> = _aiCustomRoutine.asStateFlow()

    private val _aiRefuerzos = MutableStateFlow<List<String>>(emptyList())
    val aiRefuerzos: StateFlow<List<String>> = _aiRefuerzos.asStateFlow()

    private val _isCustomRoutineLoading = MutableStateFlow(false)
    val isCustomRoutineLoading: StateFlow<Boolean> = _isCustomRoutineLoading.asStateFlow()

    fun clearAiError() {
        _aiError.value = null
    }

    fun clearAiSuggestions() {
        _aiSuggestions.value = emptyList()
    }

    fun clearAiAnalysis() {
        _aiAnalysis.value = null
    }

    fun clearCustomRoutine() {
        _aiCustomRoutine.value = emptyList()
        _aiRefuerzos.value = emptyList()
    }

    fun obtenerSugerenciasIA(routineTurn: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiError.value = null
            _aiSuggestions.value = emptyList()
            try {
                _aiSuggestions.value = aiService.getRoutineSuggestions(routineTurn, idioma)
            } catch (e: Exception) {
                _aiError.value = e.localizedMessage ?: "Error al obtener sugerencias de la IA"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun obtenerAnalisisPersonalizado(
        tasksManana: List<TaskItem>,
        tasksTarde: List<TaskItem>,
        tasksNoche: List<TaskItem>
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _aiError.value = null
            _aiAnalysis.value = null
            try {
                val reporte = construirReporte(
                    RoutineProgressCalculator.obtenerDiaDeHoy(), tasksManana, tasksTarde, tasksNoche
                )
                _aiAnalysis.value = aiService.getPersonalizedAnalysis(reporte, idioma)
            } catch (e: Exception) {
                _aiError.value = e.localizedMessage ?: "Error al generar análisis personalizado de la IA"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun obtenerRutinaYRefuerzosPersonalizados(need: String) {
        viewModelScope.launch {
            _isCustomRoutineLoading.value = true
            _aiError.value = null
            _aiCustomRoutine.value = emptyList()
            _aiRefuerzos.value = emptyList()
            try {
                val (rutina, refuerzos) = aiService.getCustomRoutineAndReinforcements(need, idioma)
                _aiCustomRoutine.value = rutina
                _aiRefuerzos.value = refuerzos
            } catch (e: Exception) {
                _aiError.value = e.localizedMessage ?: "Error al generar la rutina personalizada"
            } finally {
                _isCustomRoutineLoading.value = false
            }
        }
    }

    companion object {
        /** Texto que se envía a la IA con el estado y la emoción de cada tarea del día. */
        internal fun construirReporte(
            dia: String,
            tasksManana: List<TaskItem>,
            tasksTarde: List<TaskItem>,
            tasksNoche: List<TaskItem>
        ): String = buildString {
            append("Reporte de hoy ($dia):\n")
            listOf("Mañana" to tasksManana, "Tarde" to tasksTarde, "Noche" to tasksNoche).forEach { (turno, tareas) ->
                append("$turno:\n")
                if (tareas.isEmpty()) {
                    append("- Sin tareas\n")
                } else {
                    tareas.forEach { tarea ->
                        val estado = if (tarea.estaCompletadaHoy(dia)) "Completada" else "Pendiente"
                        val emocion = tarea.obtenerEmocionHoy(dia).ifEmpty { "no registrada" }
                        append("- Tarea: ${tarea.actividad} | Estado: $estado | Emoción: $emocion\n")
                    }
                }
            }
        }
    }
}
