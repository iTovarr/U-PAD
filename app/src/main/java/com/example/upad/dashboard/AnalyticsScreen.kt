package com.example.upad.dashboard

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
import com.example.upad.R
import coil.request.ImageRequest
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun AnalyticsScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // 📨 1. ESTADOS LOCALES Y CONFIGURACIÓN EN LA CABECERA
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()
    val aiAnalysis by routineViewModel.aiAnalysis.collectAsState()
    val isAnalyzing by routineViewModel.isAnalyzing.collectAsState()

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

    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    // Superficie de tarjetas purificada: Translúcidas en Premium, sólidas en Básico
    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    }

    val colorTurnoManana = Color(0xFFFFB74D)
    val colorTurnoTarde = Color(0xFF81C784)
    val colorTurnoNoche = Color(0xFF9575CD)

    val colorFondoIAActiva = if (isDarkMode) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFE8F5E9).copy(alpha = 0.75f)
    val colorTextoIAActiva = if (isDarkMode) Color(0xFF81C784) else Color(0xFF2E7D32)

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
                    shadowElevation = if (isPremiumUser) 0.dp else 2.dp
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
                            Text(text = stringResource(id = R.string.effectiveness_today, localizedDiaActual), color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalTasksAsignadas > 0) stringResource(id = R.string.achieved_percentage, generalPercentage) else stringResource(id = R.string.no_activity),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.progress_desc),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }

                // --- LEYENDA DEL GRÁFICO DE BARRAS ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colorSuperficieTarjetas,
                    border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f)),
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colorTextoSecundario.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                // --- PANEL PRINCIPAL DEL GRÁFICO ---
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colorSuperficieTarjetas,
                    border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f)),
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
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colorTextoPrincipal
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colorTextoSecundario.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
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
                                Text(stringResource(id = R.string.upad_ai_mind_active), fontWeight = FontWeight.Black, fontSize = 14.sp, color = colorTextoIAActiva)
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
                                        text = "Analizando el comportamiento y registro emocional de hoy...",
                                        fontSize = 13.sp,
                                        color = colorTextoIAActiva,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else if (aiAnalysis != null) {
                                Column {
                                    Text(
                                        text = aiAnalysis ?: "",
                                        fontSize = 13.sp,
                                        color = colorTextoPrincipal,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            routineViewModel.obtenerAnalisisPersonalizado(tasksManana, tasksTarde, tasksNoche)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colorTextoIAActiva),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Actualizar Análisis", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Column {
                                    Text(
                                        text = "U-Pad AI puede analizar el comportamiento y estado emocional registrado por tu hijo el día de hoy para darte recomendaciones psicopedagógicas a medida.",
                                        fontSize = 13.sp,
                                        color = colorTextoPrincipal,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            routineViewModel.obtenerAnalisisPersonalizado(tasksManana, tasksTarde, tasksNoche)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colorTextoIAActiva),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Generar Análisis Inteligente", color = Color.White, fontSize = 12.sp)
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
                                    fontSize = 13.sp,
                                    color = colorTextoSecundario,
                                    lineHeight = 18.sp
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
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = if (tieneTareas) textColor else textColor.copy(alpha = 0.3f),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun LegendItem(text: String, color: Color, icon: ImageVector, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.8f))
    }
}