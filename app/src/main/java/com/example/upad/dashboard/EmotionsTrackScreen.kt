package com.example.upad.dashboard

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.TaskItem
import com.example.upad.utils.RoutineProgressCalculator
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EmotionsTrackScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

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

    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    }

    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()

    val diaDeHoy = remember { RoutineProgressCalculator.obtenerDiaDeHoy() }

    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = "Bitácora de Emociones ✨",
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Monitorea los sentimientos expresados por tu hijo al finalizar cada actividad del día ($diaDeHoy).",
                    fontSize = 14.sp,
                    color = colorTextoSecundario
                )

                if (tasksManana.isEmpty() && tasksTarde.isEmpty() && tasksNoche.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay actividades registradas para el día de hoy.",
                            color = colorTextoSecundario,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    if (tasksManana.isNotEmpty()) {
                        TurnEmotionsBlock(
                            title = "🌅 Mañana",
                            tasks = tasksManana,
                            diaDeHoy = diaDeHoy,
                            colorSuperficie = colorSuperficieTarjetas,
                            colorTexto = colorTextoPrincipal,
                            colorSubtexto = colorTextoSecundario
                        )
                    }

                    if (tasksTarde.isNotEmpty()) {
                        TurnEmotionsBlock(
                            title = "☀️ Tarde",
                            tasks = tasksTarde,
                            diaDeHoy = diaDeHoy,
                            colorSuperficie = colorSuperficieTarjetas,
                            colorTexto = colorTextoPrincipal,
                            colorSubtexto = colorTextoSecundario
                        )
                    }

                    if (tasksNoche.isNotEmpty()) {
                        TurnEmotionsBlock(
                            title = "🌙 Noche",
                            tasks = tasksNoche,
                            diaDeHoy = diaDeHoy,
                            colorSuperficie = colorSuperficieTarjetas,
                            colorTexto = colorTextoPrincipal,
                            colorSubtexto = colorTextoSecundario
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TurnEmotionsBlock(
    title: String,
    tasks: List<TaskItem>,
    diaDeHoy: String,
    colorSuperficie: Color,
    colorTexto: Color,
    colorSubtexto: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
        tasks.forEach { task ->
            val emocion = task.obtenerEmocionHoy(diaDeHoy)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficie),
                border = BorderStroke(1.dp, colorSubtexto.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = task.imageUrl,
                                contentDescription = task.actividad,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "⭐", fontSize = 24.sp)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = task.actividad,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colorTexto
                        )
                        Text(
                            text = "${task.duration} minutos",
                            fontSize = 12.sp,
                            color = colorSubtexto
                        )
                    }

                    EmotionBadge(emocion = emocion)
                }
            }
        }
    }
}

@Composable
fun EmotionBadge(emocion: String) {
    val config = when (emocion.lowercase().trim()) {
        "feliz" -> BadgeConfig("😊 Feliz", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "neutral" -> BadgeConfig("😐 Neutral", Color(0xFFFFFDE7), Color(0xFFF57F17))
        "triste" -> BadgeConfig("🙁 Triste", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> BadgeConfig("⏳ Sin registrar", Color(0xFFECEFF1), Color(0xFF455A64))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = config.bgColor,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = config.text,
            color = config.textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

data class BadgeConfig(
    val text: String,
    val bgColor: Color,
    val textColor: Color
)