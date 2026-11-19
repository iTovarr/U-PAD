package com.example.upad.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Fondo común de las pantallas del tutor: el color de fondo de la paleta activa.
 *
 * El plan premium ya no cambia el fondo; `isPremium` e `isDarkMode` se mantienen
 * para no tocar a la vez todas las pantallas que lo llaman y se retirarán al
 * migrarlas al tema.
 */
@Composable
fun UPADBackgroundWrapper(
    @Suppress("UNUSED_PARAMETER") isPremium: Boolean,
    @Suppress("UNUSED_PARAMETER") isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}
