package com.example.upad.child

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.upad.R
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.ui.res.stringResource

@Composable
fun TaskExecutionScreen(
    viewModel: RoutineViewModel,
    activityName: String,
    turn: String,
    padreId: String,
    onFinishRoutine: (String) -> Unit
) {
    val colorFondoNiño = Color(0xFFE1F5FE)
    val colorVerdeExito = Color(0xFF4CAF50)
    val colorGrisBloqueado = Color(0xFFB0BEC5)
    val colorNaranjaPomodoro = Color(0xFFFF5722)

    val currentUserId = remember {
        padreId.ifEmpty {
            FirebaseAuth.getInstance().currentUser?.uid ?: "PADRE_TEST"
        }
    }

    // Recolectamos las tareas del turno actual desde el ViewModel
    val tasks by when (turn.uppercase()) {
        "MAÑANA" -> viewModel.tasksManana.collectAsState()
        "TARDE" -> viewModel.tasksTarde.collectAsState()
        else -> viewModel.tasksNoche.collectAsState()
    }

    val currentTask = remember(tasks, activityName) {
        tasks.firstOrNull { it.actividad.uppercase().trim() == activityName.uppercase().trim() }
    }

    var mostrandoCelebracionIdivual by remember { mutableStateOf(false) }

    // --- ⏱️ CONFIGURACIÓN DEL TEMPORIZADOR POMODORO (5 MINUTOS = 300 SEGUNDOS) ---
    val tiempoInicialSegundos = remember(currentTask) { 1 * 60 }
    var tiempoRestanteSegundos by remember { mutableStateOf(tiempoInicialSegundos) }
    var tiempoCumplido by remember { mutableStateOf(false) }

    // Corrutina que gestiona el paso de los segundos
    // Corrutina que gestiona el paso de los segundos (Estructura corregida para Compose)
    LaunchedEffect(currentTask, mostrandoCelebracionIdivual) {
        if (currentTask != null && !mostrandoCelebracionIdivual) {
            while (tiempoRestanteSegundos > 0) {
                delay(1000)
                tiempoRestanteSegundos -= 1
            }
            tiempoCumplido = true
        }
    }

    // Formateador visual para el reloj (MM:SS)
    val textoTemporizador = remember(tiempoRestanteSegundos) {
        val minutos = tiempoRestanteSegundos / 60
        val segundos = tiempoRestanteSegundos % 60
        String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
    }

    // --- MANEJADOR DE TRANSICIÓN INTEGRADO ---
    if (mostrandoCelebracionIdivual) {
        LaunchedEffect(Unit) {
            delay(2500)
            mostrandoCelebracionIdivual = false
            onFinishRoutine(activityName)
        }
    }

    val diaActualTexto = remember {
        when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "LUN"
            java.util.Calendar.TUESDAY -> "MAR"
            java.util.Calendar.WEDNESDAY -> "MIÉ"
            java.util.Calendar.THURSDAY -> "JUE"
            java.util.Calendar.FRIDAY -> "VIE"
            java.util.Calendar.SATURDAY -> "SÁB"
            java.util.Calendar.SUNDAY -> "DOM"
            else -> "LUN"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colorFondoNiño, Color.White))),
        contentAlignment = Alignment.Center
    ) {
        if (currentTask == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF0288D1), strokeWidth = 5.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.loading_activity), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))
            }
        }
        else if (mostrandoCelebracionIdivual) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = "✨ ⭐ 🏆 ⭐ ✨", fontSize = 60.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = stringResource(R.string.mission_accomplished), fontSize = 36.sp, fontWeight = FontWeight.Black, color = colorVerdeExito, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(R.string.focused_great_job), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1), textAlign = TextAlign.Center)
            }
        }
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                // Cabecera indicadora
                Text(
                    text = stringResource(R.string.active_focus),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0288D1),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 🖼️ CONTENEDOR CENTRAL DEL PICTOGRAMA
                Card(
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(4.dp, if (tiempoCumplido) colorVerdeExito else colorNaranjaPomodoro),
                    modifier = Modifier.size(260.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (currentTask.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = currentTask.imageUrl,
                                contentDescription = currentTask.actividad,
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = currentTask.actividad.take(1).uppercase(),
                                fontSize = 100.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFCFD8DC)
                            )
                        }
                    }
                }

                // Nombre Gigante de la Actividad
                Text(
                    text = currentTask.actividad.uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // ⏱️ RELOJ VISUAL TIME TIMER (CIRCULO ROJO PASTEL DECRECIENTE) PARA NIÑOS CON TEA
                val progreso = if (tiempoInicialSegundos > 0) tiempoRestanteSegundos.toFloat() / tiempoInicialSegundos else 0f
                val colorRojoPastel = Color(0xFFFF8A80)
                val colorVerdePastel = Color(0xFF81C784)
                val colorFondoGris = Color(0xFFECEFF1)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(150.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Círculo de fondo gris
                            drawCircle(
                                color = colorFondoGris,
                                radius = size.minDimension / 2
                            )
                            // Arco de tiempo restante (se reduce hacia 0)
                            val sweepAngle = 360f * progreso
                            drawArc(
                                color = if (tiempoCumplido) colorVerdePastel else colorRojoPastel,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                        }

                        // Reloj digital o indicador en el centro
                        Text(
                            text = if (tiempoCumplido) "🎉" else textoTemporizador,
                            fontSize = if (tiempoCumplido) 44.sp else 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF37474F)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (tiempoCumplido) stringResource(R.string.time_completed) else stringResource(R.string.watch_clock_run),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tiempoCumplido) Color(0xFF2E7D32) else Color(0xFF546E7A)
                    )
                }

                // 🚨 BOTÓN DE FINALIZACIÓN CONTROLADO
                Button(
                    onClick = {
                        if (tiempoCumplido) {
                            viewModel.completeTaskPorNombre(
                                userId = currentUserId,
                                turn = turn,
                                actividadTexto = currentTask.actividad,
                                diaActual = diaActualTexto
                            )
                            mostrandoCelebracionIdivual = true
                        }
                    },
                    // Deshabilitado por completo a nivel de interacción si no han pasado los 5 minutos
                    enabled = tiempoCumplido,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(75.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorVerdeExito,
                        disabledContainerColor = colorGrisBloqueado.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (tiempoCumplido) 6.dp else 0.dp)
                ) {
                    Text(
                        text = if (tiempoCumplido) stringResource(R.string.finish_btn) else stringResource(R.string.wait_clock),
                        fontSize = if (tiempoCumplido) 24.sp else 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (tiempoCumplido) Color.White else Color(0xFF546E7A),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}