package com.example.upad.data

import com.example.upad.viewmodel.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskItemMapperTest {

    @Test
    fun `convierte un mapa completo de Firestore`() {
        val mapa = mapOf(
            "actividad" to "Lavarse los dientes",
            "palabraClave" to "dientes",
            "imageUrl" to "https://static.arasaac.org/pictogramas/2326/2326_500.png",
            "dias" to listOf("LUNES", "MIERCOLES"),
            "duration" to 10L,
            "estadosPorDia" to mapOf("LUN" to true, "MIE" to false),
            "emocionesPorDia" to mapOf("LUN" to "feliz")
        )

        assertEquals(
            TaskItem(
                actividad = "Lavarse los dientes",
                palabraClave = "dientes",
                imageUrl = "https://static.arasaac.org/pictogramas/2326/2326_500.png",
                dias = listOf("LUNES", "MIERCOLES"),
                duration = 10,
                estadosPorDia = mapOf("LUN" to true, "MIE" to false),
                emocionesPorDia = mapOf("LUN" to "feliz")
            ),
            TaskItemMapper.fromMap(mapa)
        )
    }

    @Test
    fun `los campos que faltan toman su valor por defecto`() {
        assertEquals(TaskItem(), TaskItemMapper.fromMap(emptyMap<String, Any>()))
        assertEquals(TaskItemMapper.DURACION_POR_DEFECTO, TaskItemMapper.fromMap(emptyMap<String, Any>()).duration)
    }

    @Test
    fun `los campos con tipos inesperados no rompen la conversion`() {
        val mapa = mapOf(
            "actividad" to 42,
            "dias" to "LUNES",
            "duration" to "mucho",
            "estadosPorDia" to mapOf("LUN" to "si"),
            "emocionesPorDia" to mapOf("LUN" to 7)
        )

        val tarea = TaskItemMapper.fromMap(mapa)

        assertEquals("", tarea.actividad)
        assertTrue(tarea.dias.isEmpty())
        assertEquals(TaskItemMapper.DURACION_POR_DEFECTO, tarea.duration)
        assertEquals(mapOf("LUN" to false), tarea.estadosPorDia)
        assertEquals(mapOf("LUN" to ""), tarea.emocionesPorDia)
    }

    @Test
    fun `descarta los dias nulos y acepta duraciones decimales`() {
        val tarea = TaskItemMapper.fromMap(mapOf("dias" to listOf("LUNES", null), "duration" to 12.0))

        assertEquals(listOf("LUNES"), tarea.dias)
        assertEquals(12, tarea.duration)
    }

    @Test
    fun `convierte una lista ignorando los elementos que no son mapas`() {
        val lista = listOf(
            mapOf("actividad" to "Desayunar"),
            "basura",
            null,
            mapOf("actividad" to "Vestirse")
        )

        assertEquals(
            listOf("Desayunar", "Vestirse"),
            TaskItemMapper.fromFirestoreList(lista).map { it.actividad }
        )
    }

    @Test
    fun `un campo tasks ausente o de otro tipo da una lista vacia`() {
        assertTrue(TaskItemMapper.fromFirestoreList(null).isEmpty())
        assertTrue(TaskItemMapper.fromFirestoreList("no es una lista").isEmpty())
    }
}
