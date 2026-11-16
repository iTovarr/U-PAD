package com.example.upad.dashboard

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
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HelpTutorialScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // 🎯 Estado de control de pestañas
    var pestañaSeleccionada by remember { mutableStateOf(0) }

    // 🌗 ESTADOS GLOBALES DE DISEÑO
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    // --- NUEVOS ESTADOS Y CONFIGURACIÓN REQUERIDA ---
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
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

    val fuentePremium = FontFamily.SansSerif
    val colorFondoSolido = if (isPremiumUser) {
        if (isDarkMode) Color(0xFF0F0C29) else Color(0xFFFBE9E7)
    } else {
        if (isDarkMode) Color(0xFF121212) else Color.White
    }

    // 🎨 PALETA ADAPTATIVA COMPATIBLE
    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    }

    // 🌸 GRADIENTE O FONDO PREMIUM INTEGRADO
    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = "Guía y Tutorial",
                    imageUri = imageUri,
                    colorFondo = colorFondoSolido,
                    colorIconos = colorTextoPrincipal,
                    fuentePremium = fuentePremium,
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
                    text = "¿Cómo usar U-Pad? 🚀",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = colorTextoPrincipal
                )
                Text(
                    text = "Bienvenido a la guía de usuario. Aquí aprenderás a sacarle el máximo provecho a la gestión de rutinas.",
                    fontSize = 14.sp,
                    color = colorTextoSecundario
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
                            .clickable { pestañaSeleccionada = 0 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Instrucciones",
                            fontSize = 16.sp,
                            fontWeight = if (pestañaSeleccionada == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (pestañaSeleccionada == 0) colorAcabadoPrincipal else colorTextoSecundario.copy(alpha = 0.6f)
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
                            .clickable { pestañaSeleccionada = 1 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Video Tutorial",
                            fontSize = 16.sp,
                            fontWeight = if (pestañaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (pestañaSeleccionada == 1) colorAcabadoPrincipal else colorTextoSecundario.copy(alpha = 0.6f)
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
                        title = "1. Crear Rutinas",
                        description = "Usa el botón '+' en la esquina inferior para asignar tareas divididas en los turnos de Mañana, Tarde y Noche.",
                        containerColor = colorSuperficieTarjetas,
                        colorPrincipal = colorAcabadoPrincipal,
                        colorTexto = colorTextoPrincipal,
                        colorSecundario = colorTextoSecundario
                    )

                    TutorialStepItem(
                        icon = Icons.Default.CompassCalibration,
                        title = "2. Monitoreo en Vivo",
                        description = "Revisa las tarjetas de progreso global en el inicio para ver cuántas tareas del día han sido completadas por tu hijo.",
                        containerColor = colorSuperficieTarjetas,
                        colorPrincipal = colorAcabadoPrincipal,
                        colorTexto = colorTextoPrincipal,
                        colorSecundario = colorTextoSecundario
                    )

                    TutorialStepItem(
                        icon = Icons.Default.Star,
                        title = "3. Funciones Premium",
                        description = "Si cuentas con el plan Pro, podrás acceder al posicionamiento satelital en tiempo real usando el botón de mapa.",
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
                            .clickable {
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
                                    contentDescription = "Reproducir",
                                    tint = colorAcabadoPrincipal,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Reproducir Video Tutorial",
                                    color = colorTextoPrincipal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorTexto
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = colorSecundario
                )
            }
        }
    }
}