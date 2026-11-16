package com.example.upad.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PairingCodeTest {

    private val ahora = 1_000_000_000L

    @Test
    fun `genera siempre codigos de 6 digitos`() {
        repeat(1_000) {
            val codigo = PairingCode.generar()
            assertTrue("Código inesperado: $codigo", codigo.matches(Regex("^[1-9][0-9]{5}$")))
        }
    }

    @Test
    fun `un codigo reciente no ha expirado`() {
        assertFalse(PairingCode.haExpirado(ahora - 60_000L, ahora))
        assertFalse(PairingCode.haExpirado(ahora - PairingCode.VALIDEZ_MS, ahora))
    }

    @Test
    fun `un codigo expira al superar el tiempo de validez`() {
        assertTrue(PairingCode.haExpirado(ahora - PairingCode.VALIDEZ_MS - 1, ahora))
    }

    @Test
    fun `un codigo sin fecha de creacion se considera expirado`() {
        assertTrue(PairingCode.haExpirado(null, ahora))
    }

    @Test
    fun `solo es canjeable un codigo vigente en estado esperando`() {
        val reciente = ahora - 1_000L
        assertTrue(PairingCode.esCanjeable(PairingCode.ESTADO_ESPERANDO, reciente, ahora))
        assertFalse(PairingCode.esCanjeable(PairingCode.ESTADO_ENLAZADO, reciente, ahora))
        assertFalse(PairingCode.esCanjeable(null, reciente, ahora))
        assertFalse(PairingCode.esCanjeable(PairingCode.ESTADO_ESPERANDO, null, ahora))
    }
}
