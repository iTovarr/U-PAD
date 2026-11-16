package com.example.upad.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRepositoryParsingTest {

    @Test
    fun `quita la numeracion de las sugerencias`() {
        val respuesta = "1. Lavarse las manos\n2. Desayunar\n3.Cepillarse los dientes"

        assertEquals(
            listOf("Lavarse las manos", "Desayunar", "Cepillarse los dientes"),
            AiRepository.parsearSugerencias(respuesta)
        )
    }

    @Test
    fun `ignora lineas vacias y espacios sobrantes`() {
        val respuesta = "\n  1. Vestirse  \n\n2. Peinarse\n"

        assertEquals(listOf("Vestirse", "Peinarse"), AiRepository.parsearSugerencias(respuesta))
    }

    @Test
    fun `una respuesta vacia no produce sugerencias`() {
        assertTrue(AiRepository.parsearSugerencias("").isEmpty())
    }

    @Test
    fun `en español no se añade ninguna instruccion de idioma`() {
        assertEquals("", AiRepository.instruccionIdioma("es"))
        assertEquals("", AiRepository.instruccionIdioma("xx"))
    }

    @Test
    fun `en otro idioma pide responder en ese idioma`() {
        assertEquals(" Responde en inglés.", AiRepository.instruccionIdioma("en"))
        assertEquals(" Responde en portugués.", AiRepository.instruccionIdioma("pt-BR"))
    }

    @Test
    fun `la rutina a medida conserva sus etiquetas en español`() {
        val instruccion = AiRepository.instruccionIdioma("de", conservarEtiquetas = true)

        assertTrue(instruccion.startsWith(" Responde en alemán."))
        assertTrue(instruccion.contains("RUTINA:") && instruccion.contains("REFUERZOS:"))
    }

    @Test
    fun `separa la rutina de los mensajes de refuerzo`() {
        val respuesta = """
            RUTINA:
            1. Abrir el grifo
            2. Mojar las manos
            REFUERZOS:
            - ¡Muy bien!
            * ¡Lo lograste!
            • ¡Eres genial!
        """.trimIndent()

        val (rutina, refuerzos) = AiRepository.parsearRutinaYRefuerzos(respuesta)

        assertEquals(listOf("Abrir el grifo", "Mojar las manos"), rutina)
        assertEquals(listOf("¡Muy bien!", "¡Lo lograste!", "¡Eres genial!"), refuerzos)
    }

    @Test
    fun `ignora el texto anterior a la primera seccion`() {
        val respuesta = "Claro, aquí tienes:\nRUTINA:\n1. Sentarse\nREFUERZOS:\n- ¡Bravo!"

        val (rutina, refuerzos) = AiRepository.parsearRutinaYRefuerzos(respuesta)

        assertEquals(listOf("Sentarse"), rutina)
        assertEquals(listOf("¡Bravo!"), refuerzos)
    }
}
