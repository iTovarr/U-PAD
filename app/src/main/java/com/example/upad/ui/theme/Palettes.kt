package com.example.upad.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Archivo generado a partir de la tabla de paletas revisada con WCAG: todas las
// combinaciones de texto y fondo superan 5:1 (el mínimo AA es 4,5:1).

/** Paletas de color que el tutor puede elegir en Ajustes. */
enum class AppPalette {
    /** Azul grisáceo, verde salvia y blanco roto: sereno y frío. */
    CALMA,

    /** Arena, terracota apagada y verde oliva: cálido y hogareño. */
    TIERRA;

    companion object {
        val PorDefecto = CALMA

        /** Convierte el valor guardado en una paleta; si no se reconoce, devuelve la de por defecto. */
        fun desdeClave(clave: String?): AppPalette = entries.firstOrNull { it.name == clave } ?: PorDefecto
    }
}

/**
 * Colores de la app que Material 3 no cubre: el acento premium, el estado
 * "completado", los turnos del día y las emociones del niño.
 */
data class ExtendedColors(
    val premium: Color,
    val premiumContainer: Color,
    val onPremiumContainer: Color,
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val turnoManana: Color,
    val onTurnoManana: Color,
    val turnoTarde: Color,
    val onTurnoTarde: Color,
    val turnoNoche: Color,
    val onTurnoNoche: Color,
    val emocionFeliz: Color,
    val emocionNeutral: Color,
    val emocionTriste: Color,
    val onEmocion: Color,
    /**
     * Fondo sobre el que se dibujan los pictogramas. Siempre claro, también en modo
     * oscuro: los pictogramas de ARASAAC tienen contorno negro y se perderían sobre
     * un fondo oscuro. En oscuro es algo menos blanco para no deslumbrar.
     */
    val fondoPictograma: Color,
    /** Texto o inicial sobre [fondoPictograma]; oscuro en ambos modos. */
    val onFondoPictograma: Color
)

private val CalmaClaro = lightColorScheme(
    primary = Color(0xFF3F6A86),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E5F1),
    onPrimaryContainer = Color(0xFF12324A),
    secondary = Color(0xFF56715F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCEADF),
    onSecondaryContainer = Color(0xFF1C3326),
    tertiary = Color(0xFF5F5B8C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6E3F5),
    onTertiaryContainer = Color(0xFF262346),
    background = Color(0xFFF6F8F9),
    onBackground = Color(0xFF1C2329),
    surface = Color(0xFFFBFCFD),
    onSurface = Color(0xFF1C2329),
    surfaceVariant = Color(0xFFE3E9ED),
    onSurfaceVariant = Color(0xFF4A5862),
    outline = Color(0xFF7A8A94),
    outlineVariant = Color(0xFFC9D3D9),
    error = Color(0xFFB3403A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF7DEDB),
    onErrorContainer = Color(0xFF4A1310),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F6F8),
    surfaceContainer = Color(0xFFEDF1F4),
    surfaceContainerHigh = Color(0xFFE7ECEF),
    surfaceContainerHighest = Color(0xFFE1E7EB),
    surfaceBright = Color(0xFFFBFCFD),
    surfaceDim = Color(0xFFE1E7EB),
    inverseSurface = Color(0xFF1C2329),
    inverseOnSurface = Color(0xFFFBFCFD),
    scrim = Color(0xFF000000)
)

private val CalmaOscuro = darkColorScheme(
    primary = Color(0xFF9CC3DD),
    onPrimary = Color(0xFF0F3149),
    primaryContainer = Color(0xFF284D66),
    onPrimaryContainer = Color(0xFFD3E5F1),
    secondary = Color(0xFFA8C8B5),
    onSecondary = Color(0xFF193325),
    secondaryContainer = Color(0xFF3C5446),
    onSecondaryContainer = Color(0xFFDCEADF),
    tertiary = Color(0xFFC6C2EC),
    onTertiary = Color(0xFF2E2B57),
    tertiaryContainer = Color(0xFF45426F),
    onTertiaryContainer = Color(0xFFE6E3F5),
    background = Color(0xFF12171B),
    onBackground = Color(0xFFDDE4E9),
    surface = Color(0xFF12171B),
    onSurface = Color(0xFFDDE4E9),
    surfaceVariant = Color(0xFF3A454D),
    onSurfaceVariant = Color(0xFFBCC7CF),
    outline = Color(0xFF87929A),
    outlineVariant = Color(0xFF3A454D),
    error = Color(0xFFF0A7A0),
    onError = Color(0xFF5E1612),
    errorContainer = Color(0xFF7A2A24),
    onErrorContainer = Color(0xFFF7DEDB),
    surfaceContainerLowest = Color(0xFF0D1215),
    surfaceContainerLow = Color(0xFF192024),
    surfaceContainer = Color(0xFF1D2429),
    surfaceContainerHigh = Color(0xFF262E33),
    surfaceContainerHighest = Color(0xFF31393E),
    surfaceBright = Color(0xFF31393E),
    surfaceDim = Color(0xFF0D1215),
    inverseSurface = Color(0xFFDDE4E9),
    inverseOnSurface = Color(0xFF12171B),
    scrim = Color(0xFF000000)
)

private val TierraClaro = lightColorScheme(
    primary = Color(0xFF8A5A3E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3DFD2),
    onPrimaryContainer = Color(0xFF3A2010),
    secondary = Color(0xFF5F6640),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5E8CF),
    onSecondaryContainer = Color(0xFF262A10),
    tertiary = Color(0xFF45696A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD5E8E5),
    onTertiaryContainer = Color(0xFF10302D),
    background = Color(0xFFF8F4EE),
    onBackground = Color(0xFF2A231E),
    surface = Color(0xFFFCF9F5),
    onSurface = Color(0xFF2A231E),
    surfaceVariant = Color(0xFFEDE4DA),
    onSurfaceVariant = Color(0xFF5A4F46),
    outline = Color(0xFF8C7E72),
    outlineVariant = Color(0xFFD8CCC0),
    error = Color(0xFFA83E36),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF7DCD7),
    onErrorContainer = Color(0xFF45120D),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F0E9),
    surfaceContainer = Color(0xFFF1EAE2),
    surfaceContainerHigh = Color(0xFFEBE3DA),
    surfaceContainerHighest = Color(0xFFE5DCD2),
    surfaceBright = Color(0xFFFCF9F5),
    surfaceDim = Color(0xFFE5DCD2),
    inverseSurface = Color(0xFF2A231E),
    inverseOnSurface = Color(0xFFFCF9F5),
    scrim = Color(0xFF000000)
)

private val TierraOscuro = darkColorScheme(
    primary = Color(0xFFE5B597),
    onPrimary = Color(0xFF4A2817),
    primaryContainer = Color(0xFF6A4129),
    onPrimaryContainer = Color(0xFFF3DFD2),
    secondary = Color(0xFFC6CC9F),
    onSecondary = Color(0xFF2F3315),
    secondaryContainer = Color(0xFF474C29),
    onSecondaryContainer = Color(0xFFE5E8CF),
    tertiary = Color(0xFFA9CEC9),
    onTertiary = Color(0xFF173733),
    tertiaryContainer = Color(0xFF34544F),
    onTertiaryContainer = Color(0xFFD5E8E5),
    background = Color(0xFF1A1613),
    onBackground = Color(0xFFEDE3DA),
    surface = Color(0xFF1A1613),
    onSurface = Color(0xFFEDE3DA),
    surfaceVariant = Color(0xFF4A413A),
    onSurfaceVariant = Color(0xFFD3C6BB),
    outline = Color(0xFFA0948A),
    outlineVariant = Color(0xFF4A413A),
    error = Color(0xFFF2A99F),
    onError = Color(0xFF5C1810),
    errorContainer = Color(0xFF7A2C22),
    onErrorContainer = Color(0xFFF7DCD7),
    surfaceContainerLowest = Color(0xFF14110E),
    surfaceContainerLow = Color(0xFF221D19),
    surfaceContainer = Color(0xFF27211D),
    surfaceContainerHigh = Color(0xFF312A25),
    surfaceContainerHighest = Color(0xFF3C342E),
    surfaceBright = Color(0xFF3C342E),
    surfaceDim = Color(0xFF14110E),
    inverseSurface = Color(0xFFEDE3DA),
    inverseOnSurface = Color(0xFF1A1613),
    scrim = Color(0xFF000000)
)

private val CalmaClaroExtendido = ExtendedColors(
    premium = Color(0xFF85662A),
    premiumContainer = Color(0xFFF2E7CC),
    onPremiumContainer = Color(0xFF3D2E0F),
    success = Color(0xFF2F6B4B),
    successContainer = Color(0xFFD7EDDF),
    onSuccessContainer = Color(0xFF0F2D1D),
    turnoManana = Color(0xFFF6E6CC),
    onTurnoManana = Color(0xFF4A3412),
    turnoTarde = Color(0xFFDCEADF),
    onTurnoTarde = Color(0xFF1C3326),
    turnoNoche = Color(0xFFE6E3F5),
    onTurnoNoche = Color(0xFF262346),
    emocionFeliz = Color(0xFFDCEFE1),
    emocionNeutral = Color(0xFFF3EDD8),
    emocionTriste = Color(0xFFDCE6F2),
    onEmocion = Color(0xFF1C2329),
    fondoPictograma = Color(0xFFFFFFFF),
    onFondoPictograma = Color(0xFF4A5862)
)

private val CalmaOscuroExtendido = ExtendedColors(
    premium = Color(0xFFD9BC7A),
    premiumContainer = Color(0xFF4A3D1E),
    onPremiumContainer = Color(0xFFF2E7CC),
    success = Color(0xFF8FD0A8),
    successContainer = Color(0xFF24493A),
    onSuccessContainer = Color(0xFFD7EDDF),
    turnoManana = Color(0xFF4A3C26),
    onTurnoManana = Color(0xFFF6E6CC),
    turnoTarde = Color(0xFF2F4337),
    onTurnoTarde = Color(0xFFDCEADF),
    turnoNoche = Color(0xFF3A3858),
    onTurnoNoche = Color(0xFFE6E3F5),
    emocionFeliz = Color(0xFF2B4434),
    emocionNeutral = Color(0xFF453F2C),
    emocionTriste = Color(0xFF2C3B4D),
    onEmocion = Color(0xFFDDE4E9),
    fondoPictograma = Color(0xFFDCE2E6),
    onFondoPictograma = Color(0xFF3A454D)
)

private val TierraClaroExtendido = ExtendedColors(
    premium = Color(0xFF85662A),
    premiumContainer = Color(0xFFF2E7CC),
    onPremiumContainer = Color(0xFF3D2E0F),
    success = Color(0xFF3F6A3A),
    successContainer = Color(0xFFDCEBD5),
    onSuccessContainer = Color(0xFF132B10),
    turnoManana = Color(0xFFF6E2C7),
    onTurnoManana = Color(0xFF4A3010),
    turnoTarde = Color(0xFFE5E8CF),
    onTurnoTarde = Color(0xFF262A10),
    turnoNoche = Color(0xFFDDE5EC),
    onTurnoNoche = Color(0xFF1F2B36),
    emocionFeliz = Color(0xFFE2EDD6),
    emocionNeutral = Color(0xFFF4EBD4),
    emocionTriste = Color(0xFFDCE5EC),
    onEmocion = Color(0xFF2A231E),
    fondoPictograma = Color(0xFFFFFFFF),
    onFondoPictograma = Color(0xFF5A4F46)
)

private val TierraOscuroExtendido = ExtendedColors(
    premium = Color(0xFFD9BC7A),
    premiumContainer = Color(0xFF4A3D1E),
    onPremiumContainer = Color(0xFFF2E7CC),
    success = Color(0xFFA9CFA0),
    successContainer = Color(0xFF2B4426),
    onSuccessContainer = Color(0xFFDCEBD5),
    turnoManana = Color(0xFF4D3B24),
    onTurnoManana = Color(0xFFF6E2C7),
    turnoTarde = Color(0xFF3B3F24),
    onTurnoTarde = Color(0xFFE5E8CF),
    turnoNoche = Color(0xFF2E3A45),
    onTurnoNoche = Color(0xFFDDE5EC),
    emocionFeliz = Color(0xFF34432A),
    emocionNeutral = Color(0xFF4A402B),
    emocionTriste = Color(0xFF2E3B47),
    onEmocion = Color(0xFFEDE3DA),
    fondoPictograma = Color(0xFFE6DED6),
    onFondoPictograma = Color(0xFF4A413A)
)

/** Esquema de Material 3 de la paleta para el modo indicado. */
fun AppPalette.colorScheme(darkTheme: Boolean): ColorScheme = when (this) {
    AppPalette.CALMA -> if (darkTheme) CalmaOscuro else CalmaClaro
    AppPalette.TIERRA -> if (darkTheme) TierraOscuro else TierraClaro
}

/** Colores adicionales de la paleta para el modo indicado. */
fun AppPalette.extendedColors(darkTheme: Boolean): ExtendedColors = when (this) {
    AppPalette.CALMA -> if (darkTheme) CalmaOscuroExtendido else CalmaClaroExtendido
    AppPalette.TIERRA -> if (darkTheme) TierraOscuroExtendido else TierraClaroExtendido
}
