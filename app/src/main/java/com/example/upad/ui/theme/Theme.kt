package com.example.upad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun UPadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isPremium: Boolean = false,
    content: @Composable () -> Unit
) {
    val primaryColor = if (isPremium) ColorPremiumGold else ColorAzulTEA
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            background = DarkBackground,
            surface = DarkSurface,
            onBackground = Color.White,
            onSurface = Color(0xFFE0E0E0)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            background = LightBackground,
            surface = LightSurface,
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF757575)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}