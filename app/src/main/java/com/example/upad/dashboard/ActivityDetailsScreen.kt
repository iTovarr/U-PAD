package com.example.upad.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.viewmodel.RoutineViewModel
import java.util.Calendar

@Composable
fun ActivityDetailsScreen(
    viewModel: RoutineViewModel,
    onBack: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    // Forzamos la recarga de las rutinas de la base de datos al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarRutinasDesdeFirebase("PADRE_TEST")
    }

    // --- 📅 DETECTOR AUTOMÁTICO DEL DÍA DE HOY ---
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

    // Recolectamos todas las listas de turnos en tiempo real
    val tasksManana by viewModel.tasksManana.collectAsState()
    val tasksTarde by viewModel.tasksTarde.collectAsState()
    val tasksNoche by viewModel.tasksNoche.collectAsState()

    // Unimos todas las tareas en una sola lista para el reporte global
    val todasLasTareas = tasksManana + tasksTarde + tasksNoche

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ESTILO U-PAD ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 24.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAzulTEA)
            }
            Text(
                text = "MONITOR EN TIEMPO REAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = "Progreso de Actividades",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // --- CUERPO PRINCIPAL ---
        if (todasLasTareas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Aún no hay actividades guardadas o asignadas.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "ESTADO DE LA AGENDA HOY (${diaActualTexto})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                items(todasLasTareas) { task ->
                    // 🔥 REEMPLAZADO: Usamos la función del modelo basada en el mapa de días
                    val estaCompletada = task.estaCompletadaHoy(diaActualTexto)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.actividad.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.DarkGray,
                                    modifier = Modifier.weight(1f)
                                )

                                // Indicador visual de estado para el padre
                                if (estaCompletada) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("¡HECHO! ✅", fontWeight = FontWeight.Bold) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = Color(0xFFE8F5E9),
                                            labelColor = Color(0xFF2E7D32)
                                        )
                                    )
                                } else {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Pendiente ⏳", fontWeight = FontWeight.Bold) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = Color(0xFFFFF3E0),
                                            labelColor = Color(0xFFE65100)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = colorFondoBase, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Fila de tiempo recomendada o registrada
                            DetailRow(
                                icon = Icons.Default.Timer,
                                label = "Tiempo asignado",
                                value = "${task.duration} minutos",
                                color = colorAzulTEA
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Fila dinámica con icono interactivo
                            DetailRow(
                                icon = if (estaCompletada) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                                label = "Estado del paso",
                                value = if (estaCompletada) "El niño completó la actividad con éxito" else "Esperando a que el niño termine",
                                color = if (estaCompletada) Color(0xFF81C784) else Color(0xFFFFB74D)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}