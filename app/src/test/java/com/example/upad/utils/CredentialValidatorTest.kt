package com.example.upad.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialValidatorTest {

    @Test
    fun `acepta emails con formato valido`() {
        assertTrue(CredentialValidator.esEmailValido("tutor@example.com"))
        assertTrue(CredentialValidator.esEmailValido("nombre.apellido+upad@correo.edu.pe"))
    }

    @Test
    fun `ignora espacios alrededor del email`() {
        assertTrue(CredentialValidator.esEmailValido("  tutor@example.com "))
    }

    @Test
    fun `rechaza emails mal formados`() {
        assertFalse(CredentialValidator.esEmailValido(""))
        assertFalse(CredentialValidator.esEmailValido("tutor"))
        assertFalse(CredentialValidator.esEmailValido("tutor@"))
        assertFalse(CredentialValidator.esEmailValido("tutor@example"))
        assertFalse(CredentialValidator.esEmailValido("tutor@example..com"))
        assertFalse(CredentialValidator.esEmailValido("tu tor@example.com"))
    }

    @Test
    fun `exige la longitud minima de contrasena`() {
        assertFalse(CredentialValidator.esPasswordValida("1234567"))
        assertTrue(CredentialValidator.esPasswordValida("12345678"))
    }

    @Test
    fun `los espacios cuentan como caracteres de la contrasena`() {
        assertTrue(CredentialValidator.esPasswordValida(" clave  "))
    }
}
