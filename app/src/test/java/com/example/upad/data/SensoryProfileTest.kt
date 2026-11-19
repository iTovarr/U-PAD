package com.example.upad.data

import com.example.upad.ui.theme.AppPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensoryProfileTest {

    @Test
    fun `por defecto usa la configuracion mas tranquila`() {
        val perfil = SensoryProfile()

        assertEquals(ColorIntensity.SUAVE, perfil.intensidadColor)
        assertEquals(CelebrationStyle.DISCRETA, perfil.celebracion)
        assertEquals(TimerStyle.CIRCULO, perfil.temporizador)
        assertEquals(ChildThemeMode.AUTOMATICO, perfil.tema)
    }

    @Test
    fun `se conserva al guardarlo y leerlo de Firestore`() {
        val perfil = SensoryProfile(
            animaciones = false,
            sonido = false,
            voz = true,
            volumen = 0.4f,
            intensidadColor = ColorIntensity.NORMAL,
            temporizador = TimerStyle.BARRA,
            celebracion = CelebrationStyle.NORMAL,
            paleta = AppPalette.TIERRA,
            tema = ChildThemeMode.OSCURO
        )

        assertEquals(perfil, SensoryProfile.fromMap(perfil.toMap()))
    }

    @Test
    fun `sin datos guardados devuelve el perfil por defecto`() {
        assertEquals(SensoryProfile(), SensoryProfile.fromMap(null))
        assertEquals(SensoryProfile(), SensoryProfile.fromMap(emptyMap<String, Any>()))
    }

    @Test
    fun `los valores no reconocidos toman su valor por defecto`() {
        val perfil = SensoryProfile.fromMap(
            mapOf(
                "temporizador" to "RELOJ_DE_ARENA",
                "paleta" to 3,
                "animaciones" to "no",
                "volumen" to 5.0
            )
        )

        assertEquals(TimerStyle.CIRCULO, perfil.temporizador)
        assertEquals(AppPalette.CALMA, perfil.paleta)
        assertTrue(perfil.animaciones)
        assertEquals(1f, perfil.volumen)
    }

    @Test
    fun `Firestore devuelve el volumen como Double y se lee igual`() {
        val perfil = SensoryProfile.fromMap(mapOf("volumen" to 0.25))

        assertEquals(0.25f, perfil.volumen)
    }

    @Test
    fun `el modo automatico solo oscurece la rutina de noche`() {
        val automatico = SensoryProfile(tema = ChildThemeMode.AUTOMATICO)
        assertTrue(automatico.usaModoOscuro(esTurnoNoche = true))
        assertFalse(automatico.usaModoOscuro(esTurnoNoche = false))

        assertFalse(SensoryProfile(tema = ChildThemeMode.CLARO).usaModoOscuro(esTurnoNoche = true))
        assertTrue(SensoryProfile(tema = ChildThemeMode.OSCURO).usaModoOscuro(esTurnoNoche = false))
    }
}
