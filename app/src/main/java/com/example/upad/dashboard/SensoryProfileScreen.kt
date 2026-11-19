package com.example.upad.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.upad.R
import com.example.upad.components.SectionTitle
import com.example.upad.components.SettingSwitchRow
import com.example.upad.components.UPadCard
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.data.CelebrationStyle
import com.example.upad.data.ChildThemeMode
import com.example.upad.data.ColorIntensity
import com.example.upad.data.SensoryProfile
import com.example.upad.data.TimerStyle
import com.example.upad.ui.theme.AppPalette
import com.example.upad.viewmodel.SensoryProfileViewModel

/**
 * Pantalla en la que el tutor adapta el modo niño a la sensibilidad del niño:
 * colores, tema, animaciones, sonido, voz, temporizador y celebraciones.
 */
@Composable
fun SensoryProfileScreen(
    viewModel: SensoryProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val perfil by viewModel.perfil.collectAsState()

    SensoryProfileContent(
        perfil = perfil,
        onCambio = viewModel::actualizar,
        onNavigateBack = onNavigateBack,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToProfile = onNavigateToProfile
    )
}

/**
 * Contenido del perfil sensorial sin estado: muestra el perfil y avisa de cada
 * cambio. Se puede previsualizar y capturar en tests sin ViewModel ni Firebase.
 */
@Composable
internal fun SensoryProfileContent(
    perfil: SensoryProfile,
    onCambio: ((SensoryProfile) -> SensoryProfile) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UpadTopAppBar(
                title = stringResource(R.string.sensory_title),
                showBackButton = true,
                onBackPressed = onNavigateBack,
                onNavigateToHelp = onNavigateToHelp,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.sensory_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.sensory_specialist_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Vista ───────────────────────────────────────────────────────
            Spacer(modifier = Modifier.size(8.dp))
            SectionTitle(stringResource(R.string.sensory_section_visual))
            UPadCard {
                OptionGroup(
                    title = stringResource(R.string.sensory_child_palette),
                    options = AppPalette.entries,
                    selected = perfil.paleta,
                    label = {
                        when (it) {
                            AppPalette.CALMA -> stringResource(R.string.palette_calma)
                            AppPalette.TIERRA -> stringResource(R.string.palette_tierra)
                        }
                    },
                    onSelect = { opcion -> onCambio { it.copy(paleta = opcion) } }
                )
                Divider()
                OptionGroup(
                    title = stringResource(R.string.sensory_child_theme),
                    options = ChildThemeMode.entries,
                    selected = perfil.tema,
                    label = {
                        when (it) {
                            ChildThemeMode.AUTOMATICO -> stringResource(R.string.sensory_theme_auto)
                            ChildThemeMode.CLARO -> stringResource(R.string.sensory_theme_light)
                            ChildThemeMode.OSCURO -> stringResource(R.string.sensory_theme_dark)
                        }
                    },
                    onSelect = { opcion -> onCambio { it.copy(tema = opcion) } }
                )
                Divider()
                OptionGroup(
                    title = stringResource(R.string.sensory_color_intensity),
                    options = ColorIntensity.entries,
                    selected = perfil.intensidadColor,
                    label = {
                        when (it) {
                            ColorIntensity.SUAVE -> stringResource(R.string.sensory_intensity_soft)
                            ColorIntensity.NORMAL -> stringResource(R.string.sensory_intensity_normal)
                        }
                    },
                    onSelect = { opcion -> onCambio { it.copy(intensidadColor = opcion) } }
                )
                Divider()
                SettingSwitchRow(
                    icon = Icons.Default.Animation,
                    title = stringResource(R.string.sensory_animations),
                    subtitle = stringResource(R.string.sensory_animations_desc),
                    checked = perfil.animaciones,
                    onCheckedChange = { valor -> onCambio { it.copy(animaciones = valor) } }
                )
            }

            // ── Sonido y voz ────────────────────────────────────────────────
            Spacer(modifier = Modifier.size(8.dp))
            SectionTitle(stringResource(R.string.sensory_section_sound))
            UPadCard {
                SettingSwitchRow(
                    icon = Icons.Default.MusicNote,
                    title = stringResource(R.string.sensory_sound),
                    subtitle = stringResource(R.string.sensory_sound_desc),
                    checked = perfil.sonido,
                    onCheckedChange = { valor -> onCambio { it.copy(sonido = valor) } }
                )
                Divider()
                SettingSwitchRow(
                    icon = Icons.Default.RecordVoiceOver,
                    title = stringResource(R.string.sensory_voice),
                    subtitle = stringResource(R.string.sensory_voice_desc),
                    checked = perfil.voz,
                    onCheckedChange = { valor -> onCambio { it.copy(voz = valor) } }
                )
                Divider()
                val tituloVolumen = stringResource(R.string.sensory_volume)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = tituloVolumen,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Slider(
                    value = perfil.volumen,
                    onValueChange = { valor -> onCambio { it.copy(volumen = valor) } },
                    enabled = perfil.sonido || perfil.voz,
                    modifier = Modifier.semantics { contentDescription = tituloVolumen }
                )
            }

            // ── Tiempo y celebraciones ──────────────────────────────────────
            Spacer(modifier = Modifier.size(8.dp))
            SectionTitle(stringResource(R.string.sensory_section_rhythm))
            UPadCard {
                OptionGroup(
                    title = stringResource(R.string.sensory_timer),
                    options = TimerStyle.entries,
                    selected = perfil.temporizador,
                    label = {
                        when (it) {
                            TimerStyle.CIRCULO -> stringResource(R.string.sensory_timer_circle)
                            TimerStyle.BARRA -> stringResource(R.string.sensory_timer_bar)
                            TimerStyle.NINGUNO -> stringResource(R.string.sensory_timer_none)
                        }
                    },
                    onSelect = { opcion -> onCambio { it.copy(temporizador = opcion) } }
                )
                Divider()
                OptionGroup(
                    title = stringResource(R.string.sensory_celebration),
                    options = CelebrationStyle.entries,
                    selected = perfil.celebracion,
                    label = {
                        when (it) {
                            CelebrationStyle.DISCRETA -> stringResource(R.string.sensory_celebration_discreet)
                            CelebrationStyle.NORMAL -> stringResource(R.string.sensory_celebration_normal)
                        }
                    },
                    onSelect = { opcion -> onCambio { it.copy(celebracion = opcion) } }
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/** Grupo de opciones excluyentes con un título, como botones segmentados. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionGroup(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.size(12.dp))
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { indice, opcion ->
            SegmentedButton(
                selected = opcion == selected,
                onClick = { onSelect(opcion) },
                shape = SegmentedButtonDefaults.itemShape(index = indice, count = options.size)
            ) {
                Text(
                    text = label(opcion),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2
                )
            }
        }
    }
}
