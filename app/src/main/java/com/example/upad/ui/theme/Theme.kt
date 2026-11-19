package com.example.upad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** Colores adicionales de la paleta activa. Se leen con `MaterialTheme.extendedColors`. */
val LocalExtendedColors = staticCompositionLocalOf { AppPalette.PorDefecto.extendedColors(darkTheme = false) }

/** Colores de la paleta activa que Material 3 no cubre (premium, completado, turnos y emociones). */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

/**
 * Tema de U-Pad: la paleta elegida en Ajustes, en su versión clara u oscura,
 * y la tipografía Atkinson Hyperlegible.
 *
 * El plan premium ya no cambia los colores del tema: su dorado está disponible
 * en `MaterialTheme.extendedColors.premium` para usarlo solo como acento.
 */
@Composable
fun UPadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AppPalette = AppPalette.PorDefecto,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalExtendedColors provides palette.extendedColors(darkTheme)) {
        MaterialTheme(
            colorScheme = palette.colorScheme(darkTheme),
            typography = Typography,
            content = content
        )
    }
}
