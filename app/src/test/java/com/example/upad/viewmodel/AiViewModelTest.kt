package com.example.upad.viewmodel

import com.example.upad.data.AiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AiViewModelTest {

    /** Doble de [AiService]: devuelve respuestas fijas o lanza el error configurado. */
    private class FakeAiService(var error: Exception? = null) : AiService {
        var ultimoReporte: String? = null

        override suspend fun getRoutineSuggestions(routineTurn: String): List<String> {
            error?.let { throw it }
            return listOf("Desayunar", "Vestirse", "Peinarse")
        }

        override suspend fun getPersonalizedAnalysis(tasksReport: String): String {
            error?.let { throw it }
            ultimoReporte = tasksReport
            return "Buen progreso hoy."
        }

        override suspend fun getCustomRoutineAndReinforcements(need: String): Pair<List<String>, List<String>> {
            error?.let { throw it }
            return listOf("Abrir el grifo") to listOf("¡Muy bien!")
        }
    }

    private val servicio = FakeAiService()
    private lateinit var viewModel: AiViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AiViewModel(servicio)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `publica las sugerencias y deja de cargar`() {
        viewModel.obtenerSugerenciasIA("MAÑANA")

        assertEquals(listOf("Desayunar", "Vestirse", "Peinarse"), viewModel.aiSuggestions.value)
        assertFalse(viewModel.isAiLoading.value)
        assertNull(viewModel.aiError.value)
    }

    @Test
    fun `un fallo de la IA se publica como error y no deja la carga activa`() {
        servicio.error = IOException("Sin conexión")

        viewModel.obtenerSugerenciasIA("MAÑANA")

        assertEquals("Sin conexión", viewModel.aiError.value)
        assertTrue(viewModel.aiSuggestions.value.isEmpty())
        assertFalse(viewModel.isAiLoading.value)
    }

    @Test
    fun `un error sin mensaje usa el texto por defecto`() {
        servicio.error = IllegalStateException()

        viewModel.obtenerRutinaYRefuerzosPersonalizados("lavarse las manos")

        assertEquals("Error al generar la rutina personalizada", viewModel.aiError.value)
        assertFalse(viewModel.isCustomRoutineLoading.value)
    }

    @Test
    fun `limpiar el error lo deja en nulo`() {
        servicio.error = IOException("Sin conexión")
        viewModel.obtenerSugerenciasIA("TARDE")

        viewModel.clearAiError()

        assertNull(viewModel.aiError.value)
    }

    @Test
    fun `el analisis envia el reporte del dia y publica la respuesta`() {
        val tareas = listOf(TaskItem(actividad = "Desayunar"))

        viewModel.obtenerAnalisisPersonalizado(tareas, emptyList(), emptyList())

        assertEquals("Buen progreso hoy.", viewModel.aiAnalysis.value)
        assertTrue(servicio.ultimoReporte!!.contains("- Tarea: Desayunar | Estado: Pendiente | Emoción: no registrada"))
        assertFalse(viewModel.isAnalyzing.value)
    }

    @Test
    fun `publica la rutina a medida con sus refuerzos y permite limpiarla`() {
        viewModel.obtenerRutinaYRefuerzosPersonalizados("lavarse las manos")

        assertEquals(listOf("Abrir el grifo"), viewModel.aiCustomRoutine.value)
        assertEquals(listOf("¡Muy bien!"), viewModel.aiRefuerzos.value)

        viewModel.clearCustomRoutine()

        assertTrue(viewModel.aiCustomRoutine.value.isEmpty())
        assertTrue(viewModel.aiRefuerzos.value.isEmpty())
    }

    @Test
    fun `el reporte incluye estado y emocion de cada tarea por turno`() {
        val reporte = AiViewModel.construirReporte(
            dia = "LUNES",
            tasksManana = listOf(
                TaskItem(
                    actividad = "Desayunar",
                    estadosPorDia = mapOf("LUN" to true),
                    emocionesPorDia = mapOf("LUN" to "feliz")
                )
            ),
            tasksTarde = emptyList(),
            tasksNoche = listOf(TaskItem(actividad = "Cenar"))
        )

        assertEquals(
            """
            Reporte de hoy (LUNES):
            Mañana:
            - Tarea: Desayunar | Estado: Completada | Emoción: feliz
            Tarde:
            - Sin tareas
            Noche:
            - Tarea: Cenar | Estado: Pendiente | Emoción: no registrada

            """.trimIndent(),
            reporte
        )
    }
}
