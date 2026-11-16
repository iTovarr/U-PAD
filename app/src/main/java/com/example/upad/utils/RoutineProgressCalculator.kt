package com.example.upad.utils

import com.example.upad.viewmodel.TaskItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object RoutineProgressCalculator {
    val diasSemana = listOf("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")

    fun obtenerDiaDeHoy(): String {
        val formato = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).uppercase()
        val formateado = formato.replace("É", "E").replace("Í", "I").replace("Á", "A")
        return if (diasSemana.contains(formateado)) formateado else "LUNES"
    }

    fun obtenerPrefijoDia(dia: String): String {
        val limpio = dia.uppercase()
            .replace("Á", "A")
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")
            .trim()

        return when (limpio) {
            "MIERCOLES" -> "MIE"
            "SABADO" -> "SAB"
            else -> limpio.take(3)
        }
    }

    fun filtrarTareasPorDia(tareas: List<TaskItem>, prefijoDia: String): List<TaskItem> {
        val prefijoLimpio = obtenerPrefijoDia(prefijoDia)
        return tareas.filter { task ->
            task.dias.isEmpty() || task.dias.any { diaTarea ->
                obtenerPrefijoDia(diaTarea) == prefijoLimpio
            }
        }
    }

    fun calcularProgreso(tareas: List<TaskItem>, prefijoDia: String): Pair<Int, Int> {
        val tareasFiltradas = filtrarTareasPorDia(tareas, prefijoDia)
        val total = tareasFiltradas.size
        val prefijoLimpio = obtenerPrefijoDia(prefijoDia)
        val completadas = tareasFiltradas.count { it.estaCompletadaHoy(prefijoLimpio) }
        return Pair(total, completadas)
    }
}