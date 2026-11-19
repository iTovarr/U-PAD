package com.example.upad.dashboard

import androidx.compose.ui.semantics.Role
import com.example.upad.R
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import com.example.upad.di.LocalAppContainer
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.SettingsViewModel

@Composable
fun HelpTutorialScreen(
    routineViewModel: RoutineViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // 🎯 Estado de control de pestañas
    var pestañaSeleccionada by remember { mutableStateOf(0) }

    // 🌗 ESTADOS GLOBALES DE DISEÑO
    val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    // --- NUEVOS ESTADOS Y CONFIGURACIÓN REQUERIDA ---
    val context = LocalContext.current
    val session = LocalAppContainer.current.sessionRepository
    val currentUser = remember { session.currentUser }
    val sharedPreferences = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        val saved = sharedPreferences.getString("PROFILE_IMAGE_URI", null)
        imageUri = when {
            saved != null -> Uri.parse(saved)
            currentUser?.photoUrl != null -> currentUser.photoUrl
            else -> null
        }
    }

    val colorFondoSolido = MaterialTheme.colorScheme.background

    // 🎨 PALETA ADAPTATIVA COMPATIBLE
    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = MaterialTheme.colorScheme.onSurface
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant

    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surfaceContainerLow

    // 🌸 GRADIENTE O FONDO PREMIUM INTEGRADO
    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = stringResource(id = R.string.help_title),
                    imageUri = imageUri,
                    showBackButton = true,
                    onBackPressed = onNavigateBack,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToHelp = onNavigateToHelp
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.help_how_to_use),
                    color = colorTextoPrincipal,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(id = R.string.help_intro),
                    color = colorTextoSecundario,
                    style = MaterialTheme.typography.bodyMedium
                )

                // 🔄 FILA DE SUBTÍTULOS ADAPTATIVOS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Subtítulo: Instrucciones
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Tab) { pestañaSeleccionada = 0 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.help_tab_instructions),
                            fontWeight = if (pestañaSeleccionada == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (pestañaSeleccionada == 0) colorAcabadoPrincipal else colorTextoSecundario.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(3.dp)
                                .background(
                                    if (pestañaSeleccionada == 0) colorAcabadoPrincipal else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // Subtítulo: Video Tutorial
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Tab) { pestañaSeleccionada = 1 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.help_tab_video),
                            fontWeight = if (pestañaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (pestañaSeleccionada == 1) colorAcabadoPrincipal else colorTextoSecundario.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(3.dp)
                                .background(
                                    if (pestañaSeleccionada == 1) colorAcabadoPrincipal else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = colorTextoSecundario.copy(alpha = 0.15f)
                )

                // 🔀 CONTROL DE CONTENIDO DINÁMICO CON ESTILOS PREMIUM
                if (pestañaSeleccionada == 0) {
                    TutorialStepItem(
                        icon = Icons.Default.LibraryAdd,
                        title = stringResource(id = R.string.help_step1_title),
                        description = stringResource(id = R.string.help_step1_text),
                        containerColor = colorSuperficieTarjetas,
                        colorPrincipal = colorAcabadoPrincipal,
                        colorTexto = colorTextoPrincipal,
                        colorSecundario = colorTextoSecundario
                    )

                    TutorialStepItem(
                        icon = Icons.Default.CompassCalibration,
                        title = stringResource(id = R.string.help_step2_title),
                        description = stringResource(id = R.string.help_step2_text),
                        containerColor = colorSuperficieTarjetas,
                        colorPrincipal = colorAcabadoPrincipal,
                        colorTexto = colorTextoPrincipal,
                        colorSecundario = colorTextoSecundario
                    )

                    TutorialStepItem(
                        icon = Icons.Default.Star,
                        title = stringResource(id = R.string.help_step3_title),
                        description = stringResource(id = R.string.help_step3_text),
                        containerColor = colorSuperficieTarjetas,
                        colorPrincipal = colorAcabadoPrincipal,
                        colorTexto = colorTextoPrincipal,
                        colorSecundario = colorTextoSecundario
                    )
                } else {
                    // Contenedor de Video adaptado al Modo Oscuro / Claro / Premium
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clickable(role = Role.Button) {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://youtu.be/fVQU3RGt8KI")
                                )
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                        border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = stringResource(id = R.string.cd_play),
                                    tint = colorAcabadoPrincipal,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(id = R.string.help_play_video),
                                    color = colorTextoPrincipal,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialStepItem(
    icon: ImageVector,
    title: String,
    description: String,
    containerColor: Color,
    colorPrincipal: Color,
    colorTexto: Color,
    colorSecundario: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(1.dp, colorSecundario.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colorPrincipal.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorPrincipal,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = colorTexto,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = colorSecundario,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}