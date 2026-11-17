package com.example.upad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Colores del panel del tutor que dependen del modo oscuro y del plan premium.
 *
 * Cada pantalla repetía estas mismas expresiones con los valores escritos a mano;
 * aquí están una sola vez, con los mismos valores.
 */
object UPadColors {

    fun textoPrincipal(isDarkMode: Boolean): Color =
        if (isDarkMode) Color.White else Color(0xFF111111)

    fun textoSecundario(isDarkMode: Boolean): Color =
        if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    /** Color de acento: dorado en premium, el primario del tema en el plan básico. */
    @Composable
    @ReadOnlyComposable
    fun acabadoPrincipal(isPremium: Boolean): Color =
        if (isPremium) ColorPremiumGold else MaterialTheme.colorScheme.primary

    /** Fondo liso de la pantalla, bajo el degradado premium si lo hay. */
    fun fondoSolido(isPremium: Boolean, isDarkMode: Boolean): Color =
        if (isPremium) {
            if (isDarkMode) Color(0xFF0F0C29) else Color(0xFFFBE9E7)
        } else {
            if (isDarkMode) DarkBackground else Color.White
        }

    /** Superficie de las tarjetas: translúcida en premium, opaca en el plan básico. */
    fun superficieTarjetas(isPremium: Boolean, isDarkMode: Boolean): Color =
        if (isPremium) {
            if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
        } else {
            if (isDarkMode) DarkSurface else Color.White
        }
}
