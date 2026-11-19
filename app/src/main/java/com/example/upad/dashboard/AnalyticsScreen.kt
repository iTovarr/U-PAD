package com.example.upad.dashboard

import com.example.upad.ui.theme.extendedColors
import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.upad.di.LocalAppContainer
import com.example.upad.R
import coil.request.ImageRequest
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.AiViewModel
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.SettingsViewModel
import java.util.Calendar

@Composable
fun AnalyticsScreen(
    routineViewModel: RoutineViewModel,
    settingsViewModel: SettingsViewModel,
    aiViewModel: AiViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // 📨 1. ESTADOS LOCALES Y CONFIGURACIÓN EN LA CABECERA
    val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val aiAnalysis by aiViewModel.aiAnalysis.collectAsState()
    val isAnalyzing by aiViewModel.isAnalyzing.collectAsState()

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

    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = MaterialTheme.colorScheme.onSurface
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant

    // Superficie de tarjetas purificada: Translúcidas en Premium, sólidas en Básico
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surfaceContainerLow

    // Un color de la paleta por turno para las barras del gráfico.
    val colorTurnoManana = MaterialTheme.extendedColors.premium
    val colorTurnoTarde = MaterialTheme.colorScheme.secondary
    val colorTurnoNoche = MaterialTheme.colorScheme.tertiary

    val colorFondoIAActiva = MaterialTheme.colorScheme.secondaryContainer
    val colorTextoIAActiva = MaterialTheme.colorScheme.onSecondaryContainer

    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()

    val diasSemana = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")

    val diaActualTexto = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "LUN"
            Calendar.TUESDAY -> "MAR"
            Calendar.WEDNESDAY -> "MIÉ"
            Calendar.THURSDAY -> "JUE"
            Calendar.FRIDAY -> "VIE"
            Calendar.SATURDAY -> "SÁB"
            Calendar.SUNDAY -> "DOM"
            else -> "LUN"
        }
    }

    // --- CÁLCULO GENERAL DE LA JORNADA ---
    val completedManana = tasksManana.count { it.estaCompletadaHoy(diaActualTexto) }
    val completedTarde = tasksTarde.count { it.estaCompletadaHoy(diaActualTexto) }
    val completedNoche = tasksNoche.count { it.estaCompletadaHoy(diaActualTexto) }

    val totalTasksAsignadas = tasksManana.size + tasksTarde.size + tasksNoche.size
    val totalTasksCompletadas = completedManana + completedTarde + completedNoche

    val generalProgress = if (totalTasksAsignadas > 0) totalTasksCompletadas.toFloat() / totalTasksAsignadas.toFloat() else 0f
    val generalPercentage = (generalProgress * 100).toInt()

    // 🚀 2. CONTENEDOR BASE UNIFICADO
    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = stringResource(id = R.string.weekly_report),
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- TARJETA DE RESUMEN DE RENDIMIENTO (HOY) ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colorAcabadoPrincipal,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val localizedDiaActual = when (diaActualTexto) {
                                "LUN" -> stringResource(id = R.string.day_mon)
                                "MAR" -> stringResource(id = R.string.day_tue)
                                "MIÉ", "MIE" -> stringResource(id = R.string.day_wed)
                                "JUE" -> stringResource(id = R.string.day_thu)
                                "VIE" -> stringResource(id = R.string.day_fri)
                                "SÁB", "SAB" -> stringResource(id = R.string.day_sat)
                                else -> stringResource(id = R.string.day_sun)
                            }
                            Text(text = stringResource(id = R.string.effectiveness_today, localizedDiaActual), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalTasksAsignadas > 0) stringResource(id = R.string.achieved_percentage, generalPercentage) else stringResource(id = R.string.no_activity),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.progress_desc),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(26.dp))
                        }
                    }
                }

                // --- LEYENDA DEL GRÁFICO DE BARRAS ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colorSuperficieTarjetas,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(text = stringResource(id = R.string.morning), color = colorTurnoManana, icon = Icons.Default.LightMode, textColor = colorTextoPrincipal)
                        LegendItem(text = stringResource(id = R.string.afternoon), color = colorTurnoTarde, icon = Icons.Default.WbTwilight, textColor = colorTextoPrincipal)
                        LegendItem(text = stringResource(id = R.string.evening), color = colorTurnoNoche, icon = Icons.Default.NightsStay, textColor = colorTextoPrincipal)
                    }
                }

                Text(
                    text = stringResource(id = R.string.weekly_turn_compliance),
                    color = colorTextoSecundario.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )

                // --- PANEL PRINCIPAL DEL GRÁFICO ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colorSuperficieTarjetas,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        diasSemana.forEach { dia ->
                            val prefix = when(dia) {
                                "MIÉRCOLES" -> "MIÉ"
                                "SÁBADO" -> "SÁB"
                                else -> dia.take(3)
                            }

                            val mTasks = tasksManana.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }
                            val tTasks = tasksTarde.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }
                            val nTasks = tasksNoche.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }

                            val pManana = if (mTasks.isNotEmpty()) mTasks.count { it.estaCompletadaHoy(prefix) }.toFloat() / mTasks.size.toFloat() else -1f
                            val pTarde = if (tTasks.isNotEmpty()) tTasks.count { it.estaCompletadaHoy(prefix) }.toFloat() / tTasks.size.toFloat() else -1f
                            val pNoche = if (nTasks.isNotEmpty()) nTasks.count { it.estaCompletadaHoy(prefix) }.toFloat() / nTasks.size.toFloat() else -1f

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                val dayResId = when (dia) {
                                    "LUNES" -> R.string.monday
                                    "MARTES" -> R.string.tuesday
                                    "MIÉRCOLES", "MIERCOLES" -> R.string.wednesday
                                    "JUEVES" -> R.string.thursday
                                    "VIERNES" -> R.string.friday
                                    "SÁBADO", "SABADO" -> R.string.saturday
                                    else -> R.string.sunday
                                }
                                val dayText = stringResource(id = dayResId)
                                Text(
                                    text = dayText.lowercase().replaceFirstChar { it.uppercase() },
                                    modifier = Modifier
                                        .width(75.dp)
                                        .padding(top = 4.dp),
                                    color = colorTextoPrincipal,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BarChartRow(progress = pManana, color = colorTurnoManana, icon = Icons.Default.LightMode, textColor = colorTextoPrincipal)
                                    BarChartRow(progress = pTarde, color = colorTurnoTarde, icon = Icons.Default.WbTwilight, textColor = colorTextoPrincipal)
                                    BarChartRow(progress = pNoche, color = colorTurnoNoche, icon = Icons.Default.NightsStay, textColor = colorTextoPrincipal)
                                }
                            }

                            if (dia != "DOMINGO") {
                                HorizontalDivider(color = colorTextoSecundario.copy(alpha = 0.1f), thickness = 1.dp)
                            }
                        }
                    }
                }

                // --- 🤖 SECCIÓN MÓDULO DE IA ---
                Text(
                    text = stringResource(id = R.string.behavioral_recommendations),
                    color = colorTextoSecundario.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                    style = MaterialTheme.typography.labelSmall
                )

                if (isPremiumUser) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = colorFondoIAActiva,
                        border = BorderStroke(1.dp, colorTextoIAActiva.copy(alpha = 0.25f)),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colorTextoIAActiva, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(id = R.string.upad_ai_mind_active), color = colorTextoIAActiva, style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (isAnalyzing) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = colorTextoIAActiva, modifier = Modifier.size(30.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(id = R.string.analytics_analyzing),
                                        color = colorTextoIAActiva,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            } else if (aiAnalysis != null) {
                                Column {
                                    Text(
                                        text = aiAnalysis ?: "",
                                        color = colorTextoPrincipal,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            aiViewModel.obtenerAnalisisPersonalizado(tasksManana, tasksTarde, tasksNoche)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(stringResource(id = R.string.analytics_refresh), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            } else {
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.analytics_ai_intro),
                                        color = colorTextoPrincipal,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            aiViewModel.obtenerAnalisisPersonalizado(tasksManana, tasksTarde, tasksNoche)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(stringResource(id = R.string.analytics_generate), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = colorSuperficieTarjetas,
                        border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f)),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = colorTextoSecundario.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(id = R.string.unlock_ai_desc),
                                    color = colorTextoSecundario,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = onNavigateToPremium) {
                                Text(stringResource(id = R.string.pro_badge), color = colorAcabadoPrincipal, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BarChartRow(progress: Float, color: Color, icon: ImageVector, textColor: Color) {
    val tieneTareas = progress >= 0f
    val barraProgresoAnimada by animateFloatAsState(
        targetValue = if (tieneTareas) progress else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "BarraAnimada"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (tieneTareas) color else textColor.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(end = 8.dp)
                .size(16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .background(textColor.copy(alpha = 0.08f), CircleShape)
        ) {
            if (tieneTareas && barraProgresoAnimada > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = barraProgresoAnimada)
                        .background(color, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = if (tieneTareas) "${(progress * 100).toInt()}%" else "--",
            color = if (tieneTareas) textColor else textColor.copy(alpha = 0.3f),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun LegendItem(text: String, color: Color, icon: ImageVector, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
    }
}