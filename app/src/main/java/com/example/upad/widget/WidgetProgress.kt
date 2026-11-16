package com.example.upad.widget

import com.example.upad.utils.RoutineProgressCalculator
import com.example.upad.viewmodel.TaskItem

/**
 * Resumen del progreso del día que muestran los widgets de escritorio.
 * Los porcentajes van de 0 a 100.
 */
data class WidgetProgress(
    val porcentajeManana: Int,
    val porcentajeTarde: Int,
    val porcentajeNoche: Int,
    val totalTareas: Int,
    val completadas: Int,
    val isPremium: Boolean
) {
    val pendientes: Int get() = totalTareas - completadas
    val porcentajeGlobal: Int get() = porcentaje(completadas, totalTareas)

    companion object {
        /** Calcula el resumen de un día a partir de las tareas de cada turno. */
        fun calcular(
            tareasManana: List<TaskItem>,
            tareasTarde: List<TaskItem>,
            tareasNoche: List<TaskItem>,
            prefijoDia: String,
            isPremium: Boolean
        ): WidgetProgress {
            val (totalManana, hechasManana) = RoutineProgressCalculator.calcularProgreso(tareasManana, prefijoDia)
            val (totalTarde, hechasTarde) = RoutineProgressCalculator.calcularProgreso(tareasTarde, prefijoDia)
            val (totalNoche, hechasNoche) = RoutineProgressCalculator.calcularProgreso(tareasNoche, prefijoDia)

            return WidgetProgress(
                porcentajeManana = porcentaje(hechasManana, totalManana),
                porcentajeTarde = porcentaje(hechasTarde, totalTarde),
                porcentajeNoche = porcentaje(hechasNoche, totalNoche),
                totalTareas = totalManana + totalTarde + totalNoche,
                completadas = hechasManana + hechasTarde + hechasNoche,
                isPremium = isPremium
            )
        }

        private fun porcentaje(completadas: Int, total: Int): Int =
            if (total > 0) (completadas * 100) / total else 0
    }
}
