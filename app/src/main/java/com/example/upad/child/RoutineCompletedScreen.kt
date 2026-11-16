package com.example.upad.child

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R

import androidx.compose.ui.res.stringResource

@Composable
fun RoutineCompletedScreen(
    nextActivityPreview: String = "",
    reinforcementMessages: List<String> = emptyList(),
    onFinishClick: () -> Unit
) {
    val colorAzulTEA = Color(0xFF0288D1)     // Azul más vivo y con mejor contraste
    val colorVerdeExito = Color(0xFF2E7D32)  // Verde sólido de logro cumplido

    // ✨ Un degradado mágico, alegre y luminoso digno de una celebración
    val fondoDegradadoMagico = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE1F5FE), // Celeste cielo pastel
            Color(0xFFFFF9C4)  // Oro/Amarillo radiante muy sutil abajo
        )
    )

    val esFinDeRutina = nextActivityPreview.isEmpty() || nextActivityPreview == "¡Felicidades! Completaste todo"
    val chosenMessage = remember(reinforcementMessages) {
        if (reinforcementMessages.isNotEmpty()) {
            reinforcementMessages.random()
        } else {
            null
        }
    }
    val textToShow = if (esFinDeRutina) {
        chosenMessage ?: stringResource(R.string.routine_completed_default_preview)
    } else {
        nextActivityPreview
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoDegradadoMagico)
            .verticalScroll(rememberScrollState()) // Protege pantallas pequeñas de recortes
    ) {
        // --- 🏆 CABECERA DE ÉXITO ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 55.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.routine_completed_title),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = colorVerdeExito,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (esFinDeRutina) stringResource(R.string.no_pending_tasks) else stringResource(R.string.activity_completed_success),
                    fontSize = 17.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- 🖼️ CONTENEDOR DE LA ILUSTRACIÓN DE CELEBRACIÓN ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Contenedor blanco estilizado semi-flotante para que la imagen destaque
            Card(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.celebracion),
                        contentDescription = "Celebración",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // --- 📥 PANEL DE PRÓXIMA ACTIVIDAD DE ALTA VISIBILIDAD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (esFinDeRutina) stringResource(R.string.today_status_label) else stringResource(R.string.next_activity_label),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.DarkGray,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contenedor destacado para el texto dinámico
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textToShow.uppercase(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colorAzulTEA,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (esFinDeRutina) stringResource(R.string.time_to_rest_or_play) else stringResource(R.string.keep_it_up),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🚀 BOTÓN ENTENDIDO PREMIUM CON EFECTO DE IMPACTO
                Button(
                    onClick = onFinishClick,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(72.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Cambiado a verde para asociar la acción con avanzar con éxito
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.understood_btn),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}