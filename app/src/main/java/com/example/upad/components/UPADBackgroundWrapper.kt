package com.example.upad.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun UPADBackgroundWrapper(
    isPremium: Boolean,       // Recibe directamente tus variables sin cambiar nada
    isDarkMode: Boolean,      // Recibe directamente tus variables sin cambiar nada
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 🌸 EL GRADIENTE EXACTO DEL DASHBOARD:
    // Rosita/Púrpura en Claro Premium | Azul Profundo/Negro en Oscuro Premium
    val gradienteFondoPremium = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F0C29),
                Color(0xFF302B63),
                Color(0xFF121212)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFBE9E7), // Tu Rosita pastel base
                Color(0xFFF3E5F5), // Púrpura sutil
                Color(0xFFE8EAF6)  // Azulino suave
            )
        )
    }

    // Color plano si el usuario está en el plan básico
    val colorFondoBase = if (isDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isPremium) {
                    Modifier.background(gradienteFondoPremium)
                } else {
                    Modifier.background(colorFondoBase)
                }
            )
    ) {
        content()
    }
}