package com.example.upad.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TurnosTest {

    @Test
    fun `la manana va de medianoche a las 12 59`() {
        assertEquals(Turnos.MANANA, Turnos.paraHora(0))
        assertEquals(Turnos.MANANA, Turnos.paraHora(12))
    }

    @Test
    fun `la tarde va de las 13 00 a las 17 59`() {
        assertEquals(Turnos.TARDE, Turnos.paraHora(13))
        assertEquals(Turnos.TARDE, Turnos.paraHora(17))
    }

    @Test
    fun `la noche empieza a las 18 00`() {
        assertEquals(Turnos.NOCHE, Turnos.paraHora(18))
        assertEquals(Turnos.NOCHE, Turnos.paraHora(23))
    }
}
