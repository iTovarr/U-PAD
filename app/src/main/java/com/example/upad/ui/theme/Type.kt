package com.example.upad.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.upad.R

/**
 * Atkinson Hyperlegible, diseñada por el Braille Institute para que cada carácter
 * se distinga sin esfuerzo. Solo tiene dos pesos: normal y negrita.
 */
val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

private fun estilo(
    tamano: Int,
    interlineado: Int,
    peso: FontWeight = FontWeight.Normal,
    espaciado: Double = 0.0
) = TextStyle(
    fontFamily = AtkinsonHyperlegible,
    fontWeight = peso,
    fontSize = tamano.sp,
    lineHeight = interlineado.sp,
    letterSpacing = espaciado.sp
)

/**
 * Escala tipográfica de la app (tamaños de Material 3).
 *
 * La negrita se reserva para títulos y etiquetas; el texto corrido va en peso
 * normal y con un interlineado algo mayor que el de Material para facilitar la lectura.
 */
val Typography = Typography(
    displayLarge = estilo(57, 64, FontWeight.Bold),
    displayMedium = estilo(45, 52, FontWeight.Bold),
    displaySmall = estilo(36, 44, FontWeight.Bold),
    headlineLarge = estilo(32, 40, FontWeight.Bold),
    headlineMedium = estilo(28, 36, FontWeight.Bold),
    headlineSmall = estilo(24, 32, FontWeight.Bold),
    titleLarge = estilo(22, 28, FontWeight.Bold),
    titleMedium = estilo(18, 26, FontWeight.Bold, 0.1),
    titleSmall = estilo(15, 22, FontWeight.Bold, 0.1),
    bodyLarge = estilo(17, 26, espaciado = 0.2),
    bodyMedium = estilo(15, 23, espaciado = 0.2),
    bodySmall = estilo(13, 19, espaciado = 0.3),
    labelLarge = estilo(15, 20, FontWeight.Bold, 0.2),
    labelMedium = estilo(13, 18, FontWeight.Bold, 0.4),
    labelSmall = estilo(12, 16, FontWeight.Bold, 0.5)
)
