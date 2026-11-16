package com.example.upad.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ArasaacRepositoryTest {

    @Test
    fun `usa el idioma de la app cuando ARASAAC lo soporta`() {
        listOf("es", "en", "fr", "de", "pt", "ru").forEach { idioma ->
            assertEquals(idioma, ArasaacRepository.localeFor(idioma))
        }
    }

    @Test
    fun `ignora la region y las mayusculas`() {
        assertEquals("pt", ArasaacRepository.localeFor("pt-BR"))
        assertEquals("en", ArasaacRepository.localeFor("EN_us"))
        assertEquals("fr", ArasaacRepository.localeFor(" fr "))
    }

    @Test
    fun `un idioma no soportado o vacio usa español`() {
        assertEquals("es", ArasaacRepository.localeFor("ja"))
        assertEquals("es", ArasaacRepository.localeFor(""))
    }
}
