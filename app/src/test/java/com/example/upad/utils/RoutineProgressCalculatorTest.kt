package com.example.upad.utils

import com.example.upad.viewmodel.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineProgressCalculatorTest {

    private fun tarea(
        nombre: String,
        dias: List<String> = emptyList(),
        completadaEn: List<String> = emptyList()
    ) = TaskItem(
        actividad = nombre,
        dias = dias,
        estadosPorDia = completadaEn.associateWith { true }
    )

    @Test
    fun `el prefijo de un dia son sus tres primeras letras sin tildes`() {
        assertEquals("LUN", RoutineProgressCalculator.obtenerPrefijoDia("LUNES"))
        assertEquals("MIE", RoutineProgressCalculator.obtenerPrefijoDia("Miércoles"))
        assertEquals("SAB", RoutineProgressCalculator.obtenerPrefijoDia(" sábado "))
        assertEquals("DOM", RoutineProgressCalculator.obtenerPrefijoDia("DOM"))
    }

    @Test
    fun `el dia de hoy es siempre uno de los siete dias conocidos`() {
        assertTrue(RoutineProgressCalculator.obtenerDiaDeHoy() in RoutineProgressCalculator.diasSemana)
    }

    @Test
    fun `una tarea sin dias asignados aplica a cualquier dia`() {
        val tareas = listOf(tarea("Desayunar"))

        assertEquals(tareas, RoutineProgressCalculator.filtrarTareasPorDia(tareas, "JUEVES"))
    }

    @Test
    fun `filtra las tareas por dia aceptando nombre completo o prefijo`() {
        val lunes = tarea("Vestirse", dias = listOf("LUNES"))
        val martes = tarea("Peinarse", dias = listOf("MAR"))
        val tareas = listOf(lunes, martes)

        assertEquals(listOf(lunes), RoutineProgressCalculator.filtrarTareasPorDia(tareas, "LUN"))
        assertEquals(listOf(martes), RoutineProgressCalculator.filtrarTareasPorDia(tareas, "MARTES"))
        assertTrue(RoutineProgressCalculator.filtrarTareasPorDia(tareas, "DOMINGO").isEmpty())
    }

    @Test
    fun `calcula total y completadas solo con las tareas del dia`() {
        val tareas = listOf(
            tarea("Desayunar", completadaEn = listOf("LUN")),
            tarea("Vestirse", dias = listOf("LUNES")),
            tarea("Nadar", dias = listOf("SABADO"), completadaEn = listOf("SAB"))
        )

        assertEquals(Pair(2, 1), RoutineProgressCalculator.calcularProgreso(tareas, "LUNES"))
        assertEquals(Pair(2, 1), RoutineProgressCalculator.calcularProgreso(tareas, "SABADO"))
        assertEquals(Pair(1, 0), RoutineProgressCalculator.calcularProgreso(tareas, "MARTES"))
    }

    @Test
    fun `sin tareas el progreso es cero de cero`() {
        assertEquals(Pair(0, 0), RoutineProgressCalculator.calcularProgreso(emptyList(), "LUNES"))
    }

    @Test
    fun `una tarea completada un dia no lo esta en los demas`() {
        val tarea = tarea("Desayunar", completadaEn = listOf("LUN"))

        assertTrue(tarea.estaCompletadaHoy("LUNES"))
        assertFalse(tarea.estaCompletadaHoy("MARTES"))
    }

    @Test
    fun `la emocion se guarda por dia`() {
        val tarea = TaskItem(actividad = "Desayunar", emocionesPorDia = mapOf("LUN" to "feliz"))

        assertEquals("feliz", tarea.obtenerEmocionHoy("lunes"))
        assertEquals("", tarea.obtenerEmocionHoy("MARTES"))
    }

    @Test
    fun `la siguiente pendiente es la primera de hoy sin completar`() {
        val tareas = listOf(
            tarea("Desayunar", completadaEn = listOf("LUN")),
            tarea("Nadar", dias = listOf("SABADO")),
            tarea("Vestirse"),
            tarea("Peinarse")
        )

        assertEquals("Vestirse", RoutineProgressCalculator.siguientePendiente(tareas, "LUNES")?.actividad)
        assertEquals("Desayunar", RoutineProgressCalculator.siguientePendiente(tareas, "MARTES")?.actividad)
    }

    @Test
    fun `sin tareas pendientes no hay siguiente`() {
        val tareas = listOf(tarea("Desayunar", completadaEn = listOf("LUN")))

        assertEquals(null, RoutineProgressCalculator.siguientePendiente(tareas, "LUNES"))
        assertEquals(null, RoutineProgressCalculator.siguientePendiente(emptyList(), "LUNES"))
    }
}
