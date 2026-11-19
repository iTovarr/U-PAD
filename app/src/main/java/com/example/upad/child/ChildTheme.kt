package com.example.upad.child

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.upad.data.ColorIntensity
import com.example.upad.data.SensoryProfile
import com.example.upad.ui.theme.UPadTheme
import com.example.upad.utils.Turnos

/** Perfil sensorial activo en las pantallas del niño. */
val LocalSensoryProfile = staticCompositionLocalOf { SensoryProfile() }

/**
 * Tema de las pantallas del niño: la paleta y el modo claro u oscuro que eligió
 * el tutor en el perfil sensorial. Con el tema automático, la rutina de noche se
 * ve en oscuro para no deslumbrar antes de dormir.
 */
@Composable
fun ChildTheme(
    perfil: SensoryProfile,
    turno: String = Turnos.actual(),
    content: @Composable () -> Unit
) {
    UPadTheme(
        darkTheme = perfil.usaModoOscuro(esTurnoNoche = turno == Turnos.NOCHE),
        palette = perfil.paleta
    ) {
        CompositionLocalProvider(LocalSensoryProfile provides perfil, content = content)
    }
}

/** Colores de acento del modo niño según la intensidad elegida por el tutor. */
object ChildColors {

    /** Fondo de un elemento destacado: el contenedor primario en suave, el primario en normal. */
    val acento: Color
        @Composable @ReadOnlyComposable
        get() = if (suave()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary

    /** Texto o icono sobre [acento]. */
    val sobreAcento: Color
        @Composable @ReadOnlyComposable
        get() = if (suave()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary

    /** Colores del botón principal de cada pantalla del niño. */
    @Composable
    fun botonPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = acento,
        contentColor = sobreAcento,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    @Composable
    @ReadOnlyComposable
    private fun suave(): Boolean = LocalSensoryProfile.current.intensidadColor == ColorIntensity.SUAVE
}
