package com.example.upad.setup

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
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceSetupScreen(
    routineViewModel: RoutineViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val prefs = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    val pantallaPequeña = configuration.screenHeightDp < 650

    // 🎨 RECOLECCIÓN DINÁMICA IGUAL QUE EN SETTINGSSCREEN
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    // --- CARGA DE ESTADOS CON PERSISTENCIA ---
    var selectedTheme by remember { mutableStateOf(prefs.getString("APP_THEME", "Modo claro") ?: "Modo claro") }
    var audioVolume by remember { mutableFloatStateOf(prefs.getFloat("AUDIO_VOLUME", 0.7f)) }
    var voiceEnabled by remember { mutableStateOf(prefs.getBoolean("VOICE_ENABLED", true)) }

    // --- ADAPTACIÓN DE TEXTOS Y CONTRASTES SEGÚN TU CONFIGURACIÓN DE SETTINGS ---
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    // 🌸 EXTRACTO FIEL DE SETTINGSSCREEN: Configuración translúcida sobre el Gradiente o Sólida en Básico
    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF7F7F7)
    }

    // Acabado Oro (0xFFC5A059) para Premium (combina con el Rosita/Azul profundo) o Primario para Básico
    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorSuperficieSeleccionada = if (isDarkMode) Color.White.copy(alpha = 0.12f) else colorAcabadoPrincipal.copy(alpha = 0.15f)

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorTextoPrincipal)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "PERSONALIZACIÓN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = colorTextoSecundario,
                        modifier = Modifier.padding(start = 12.dp),
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Prepara la experiencia",
                        fontSize = if (pantallaPequeña) 24.sp else 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colorTextoPrincipal,
                        modifier = Modifier.padding(horizontal = 12.dp)
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
                    text = "APARIENCIA VISUAL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTextoSecundario
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
                        val themes = listOf("Modo claro", "Modo oscuro", "Según sistema")
                        themes.forEach { theme ->
                            val estaSeleccionado = selectedTheme == theme

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (estaSeleccionado) colorSuperficieSeleccionada else Color.Transparent)
                                    .clickable {
                                        selectedTheme = theme
                                        val cambiarAOscuro = theme == "Modo oscuro"
                                        routineViewModel.setDarkMode(cambiarAOscuro)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = estaSeleccionado,
                                    onClick = {
                                        selectedTheme = theme
                                        routineViewModel.setDarkMode(theme == "Modo oscuro")
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colorAcabadoPrincipal,
                                        unselectedColor = colorTextoSecundario
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = theme,
                                    fontSize = 15.sp,
                                    color = colorTextoPrincipal,
                                    fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "AUDIO Y VOZ (ENTORNO DEL NIÑO)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTextoSecundario
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
                                Text("Voz en pictogramas", fontWeight = FontWeight.Bold, color = colorTextoPrincipal)
                                Text("Lee las tareas en voz alta", fontSize = 12.sp, color = colorTextoSecundario)
                            }
                            Switch(
                                checked = voiceEnabled,
                                onCheckedChange = { voiceEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
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
                            prefs.edit().apply {
                                putString("APP_THEME", selectedTheme)
                                putFloat("AUDIO_VOLUME", audioVolume)
                                putBoolean("VOICE_ENABLED", voiceEnabled)
                                apply()
                            }

                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                val configuracionExperiencia = mapOf(
                                    "temaSeleccionado" to selectedTheme,
                                    "volumenAudio" to audioVolume.toDouble(),
                                    "vozHabilitada" to voiceEnabled
                                )
                                firestore.collection("usuarios").document(uid)
                                    .collection("configuracion").document("experiencia")
                                    .set(configuracionExperiencia, com.google.firebase.firestore.SetOptions.merge())
                            }

                            onNextClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (pantallaPequeña) 54.dp else 60.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "GUARDAR Y CONTINUAR",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}