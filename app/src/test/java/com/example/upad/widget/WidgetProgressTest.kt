package com.example.upad.widget

import com.example.upad.viewmodel.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetProgressTest {

    private fun tarea(nombre: String, completada: Boolean) = TaskItem(
        actividad = nombre,
        estadosPorDia = if (completada) mapOf("LUN" to true) else emptyMap()
    )

    @Test
    fun `calcula el porcentaje de cada turno y el global`() {
        val progreso = WidgetProgress.calcular(
            tareasManana = listOf(tarea("Desayunar", true), tarea("Vestirse", false)),
            tareasTarde = listOf(tarea("Almorzar", true)),
            tareasNoche = listOf(tarea("Cenar", false)),
            prefijoDia = "LUN",
            isPremium = true
        )

        assertEquals(50, progreso.porcentajeManana)
        assertEquals(100, progreso.porcentajeTarde)
        assertEquals(0, progreso.porcentajeNoche)
        assertEquals(4, progreso.totalTareas)
        assertEquals(2, progreso.completadas)
        assertEquals(2, progreso.pendientes)
        assertEquals(50, progreso.porcentajeGlobal)
        assertEquals(true, progreso.isPremium)
    }

    @Test
    fun `sin tareas todos los porcentajes son cero`() {
        val progreso = WidgetProgress.calcular(emptyList(), emptyList(), emptyList(), "LUN", isPremium = false)

        assertEquals(0, progreso.porcentajeManana)
        assertEquals(0, progreso.porcentajeGlobal)
        assertEquals(0, progreso.totalTareas)
        assertEquals(0, progreso.pendientes)
    }

    @Test
    fun `el porcentaje se redondea hacia abajo`() {
        val progreso = WidgetProgress.calcular(
            tareasManana = listOf(tarea("A", true), tarea("B", false), tarea("C", false)),
            tareasTarde = emptyList(),
            tareasNoche = emptyList(),
            prefijoDia = "LUN",
            isPremium = false
        )

        assertEquals(33, progreso.porcentajeManana)
        assertEquals(33, progreso.porcentajeGlobal)
    }
}
