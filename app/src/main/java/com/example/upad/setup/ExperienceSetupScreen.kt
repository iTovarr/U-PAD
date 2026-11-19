package com.example.upad.setup

import androidx.compose.foundation.selection.selectable
import com.example.upad.ui.theme.AppPalette
import com.example.upad.data.SensoryProfile
import androidx.compose.ui.semantics.Role
import com.example.upad.R
import androidx.compose.ui.res.stringResource
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.di.LocalAppContainer
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceSetupScreen(
    routineViewModel: RoutineViewModel,
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val container = LocalAppContainer.current
    val session = container.sessionRepository
    val parentProfileRepository = container.parentProfileRepository

    val pantallaPequeña = configuration.screenHeightDp < 650

    // 🎨 RECOLECCIÓN DINÁMICA IGUAL QUE EN SETTINGSSCREEN
    val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    // --- Elecciones del tutor; se guardan en el perfil sensorial al continuar ---
    val paletaActual by settingsViewModel.palette.collectAsState()
    var audioVolume by remember { mutableFloatStateOf(SensoryProfile.VOLUMEN_POR_DEFECTO) }
    var voiceEnabled by remember { mutableStateOf(true) }

    // --- Colores de la paleta activa ---
    val colorTextoPrincipal = MaterialTheme.colorScheme.onSurface
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant

    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surfaceContainerLow

    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorSuperficieSeleccionada = MaterialTheme.colorScheme.secondaryContainer

    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // --- CABECERA TRASLÚCIDA/SÓLIDA ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                color = colorSuperficieTarjetas,
                border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        top = if (pantallaPequeña) 30.dp else 40.dp,
                        bottom = if (pantallaPequeña) 16.dp else 24.dp,
                        start = 16.dp,
                        end = 24.dp
                    )
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back), tint = colorTextoPrincipal)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.experience_overline),
                        color = colorTextoSecundario,
                        modifier = Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = stringResource(id = R.string.experience_title),
                        color = colorTextoPrincipal,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // --- CUERPO RESPONSIVO EN EL GRADIENTE ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(id = R.string.experience_appearance),
                    color = colorTextoSecundario,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Temas Interactivos
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colorSuperficieTarjetas,
                    border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // La paleta se aplica al momento, para que el tutor vea cómo queda.
                        AppPalette.entries.forEach { paleta ->
                            val estaSeleccionado = paletaActual == paleta
                            val (nombre, descripcion) = when (paleta) {
                                AppPalette.CALMA -> R.string.palette_calma to R.string.palette_calma_desc
                                AppPalette.TIERRA -> R.string.palette_tierra to R.string.palette_tierra_desc
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (estaSeleccionado) colorSuperficieSeleccionada else Color.Transparent)
                                    .selectable(
                                        selected = estaSeleccionado,
                                        role = Role.RadioButton,
                                        onClick = { settingsViewModel.setPalette(paleta) }
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                RadioButton(selected = estaSeleccionado, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(nombre),
                                        color = colorTextoPrincipal,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = stringResource(descripcion),
                                        color = colorTextoSecundario,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(id = R.string.experience_audio),
                    color = colorTextoSecundario,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Contenedor de Audio Adaptivo
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colorSuperficieTarjetas,
                    border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                    tonalElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(id = R.string.experience_voice), style = MaterialTheme.typography.titleSmall, color = colorTextoPrincipal)
                                Text(stringResource(id = R.string.experience_voice_hint), color = colorTextoSecundario, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = voiceEnabled,
                                onCheckedChange = { voiceEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = colorAcabadoPrincipal,
                                    uncheckedTrackColor = colorTextoSecundario.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = colorTextoPrincipal.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (voiceEnabled) colorAcabadoPrincipal else colorTextoSecundario
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Slider(
                                value = audioVolume,
                                onValueChange = { audioVolume = it },
                                enabled = voiceEnabled,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = colorAcabadoPrincipal,
                                    activeTrackColor = colorAcabadoPrincipal,
                                    inactiveTrackColor = colorAcabadoPrincipal.copy(alpha = 0.24f),
                                    disabledThumbColor = colorTextoSecundario.copy(alpha = 0.5f),
                                    disabledActiveTrackColor = colorTextoSecundario.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- BOTÓN INFERIOR ADAPTATIVO ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = colorSuperficieTarjetas,
                border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = if (pantallaPequeña) 12.dp else 16.dp)
                ) {
                    Button(
                        onClick = {
                            // Se guarda en el perfil sensorial, que es lo que lee el dispositivo del niño.
                            val uid = session.currentUser?.uid
                            if (uid != null) {
                                parentProfileRepository.saveSensoryProfile(
                                    uid,
                                    SensoryProfile(paleta = paletaActual, voz = voiceEnabled, volumen = audioVolume)
                                )
                            }

                            onNextClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (pantallaPequeña) 54.dp else 60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.experience_save),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}