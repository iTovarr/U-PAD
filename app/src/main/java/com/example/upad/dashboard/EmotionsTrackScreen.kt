package com.example.upad.dashboard

import com.example.upad.ui.theme.extendedColors
import com.example.upad.R
import androidx.compose.ui.res.stringResource
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
import com.example.upad.di.LocalAppContainer
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.SettingsViewModel
import com.example.upad.viewmodel.TaskItem
import com.example.upad.utils.RoutineProgressCalculator

@Composable
fun EmotionsTrackScreen(
    routineViewModel: RoutineViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

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

    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = MaterialTheme.colorScheme.onSurface
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant

    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surfaceContainerLow

    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()

    val diaDeHoy = remember { RoutineProgressCalculator.obtenerDiaDeHoy() }

    UPADBackgroundWrapper() {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = stringResource(id = R.string.emotions_title),
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Monitorea los sentimientos expresados por tu hijo al finalizar cada actividad del día ($diaDeHoy).",
                    color = colorTextoSecundario,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (tasksManana.isEmpty() && tasksTarde.isEmpty() && tasksNoche.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_activities_today),
                            color = colorTextoSecundario,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    if (tasksManana.isNotEmpty()) {
                        TurnEmotionsBlock(
                            title = stringResource(id = R.string.turn_morning_emoji),
                            tasks = tasksManana,
                            diaDeHoy = diaDeHoy,
                            colorSuperficie = colorSuperficieTarjetas,
                            colorTexto = colorTextoPrincipal,
                            colorSubtexto = colorTextoSecundario
                        )
                    }

                    if (tasksTarde.isNotEmpty()) {
                        TurnEmotionsBlock(
                            title = stringResource(id = R.string.turn_afternoon_emoji),
                            tasks = tasksTarde,
                            diaDeHoy = diaDeHoy,
                            colorSuperficie = colorSuperficieTarjetas,
                            colorTexto = colorTextoPrincipal,
                            colorSubtexto = colorTextoSecundario
                        )
                    }

                    if (tasksNoche.isNotEmpty()) {
                        TurnEmotionsBlock(
                            title = stringResource(id = R.string.turn_night_emoji),
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
            color = colorTexto,
            style = MaterialTheme.typography.titleSmall
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
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(12.dp))
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
                            Text(text = "⭐", style = MaterialTheme.typography.headlineSmall)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = task.actividad,
                            color = colorTexto,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${task.duration} minutos",
                            color = colorSubtexto,
                            style = MaterialTheme.typography.bodySmall
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
        // Colores suaves de la paleta, los mismos que ve el niño al elegir la emoción.
        "feliz" -> BadgeConfig("😊 " + stringResource(R.string.feedback_happy), MaterialTheme.extendedColors.emocionFeliz, MaterialTheme.extendedColors.onEmocion)
        "neutral" -> BadgeConfig("😐 " + stringResource(R.string.feedback_neutral), MaterialTheme.extendedColors.emocionNeutral, MaterialTheme.extendedColors.onEmocion)
        "triste" -> BadgeConfig("🙁 " + stringResource(R.string.feedback_sad), MaterialTheme.extendedColors.emocionTriste, MaterialTheme.extendedColors.onEmocion)
        else -> BadgeConfig("⏳ " + stringResource(R.string.emotion_not_recorded), MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = config.bgColor,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = config.text,
            color = config.textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

data class BadgeConfig(
    val text: String,
    val bgColor: Color,
    val textColor: Color
)