package com.example.upad.child

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import com.example.upad.R

@Composable
fun TaskFeedbackScreen(
    activityName: String,
    imageUrl: String = "", // ✨ Nueva variable opcional para pasar la imagen de la actividad completada
    onFeedbackSelected: (String) -> Unit
) {
    val colorAzulTEA = Color(0xFF0288D1)
    // Degradado suave y feliz para el fondo de la app
    val fondoDegradado = Brush.verticalGradient(
        colors = listOf(Color(0xFFE1F5FE), Color(0xFFF0F4F8))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoDegradado)
            .verticalScroll(rememberScrollState()) // Previene recortes en pantallas chicas
    ) {
        // --- 🏆 CABECERA REFORZADA CON ILUSTRACIÓN ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.feedback_great_job),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32), // Verde motivacional
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.feedback_question),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = activityName.uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = colorAzulTEA,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🖼️ CONTENEDOR NUEVO PARA LA IMAGEN / PICTOGRAMA DE LOGRO
                Card(
                    modifier = Modifier.size(130.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                    border = BorderStroke(3.dp, colorAzulTEA.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Actividad realizada",
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // Imagen predeterminada feliz si no viene ninguna URL de Firebase
                            Text(text = "⭐", fontSize = 65.sp)
                        }
                    }
                }
            }
        }

        // --- 🎭 CONTENEDOR DE EMOCIONES OPTIMIZADO ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fila para Feliz y Neutral
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeedbackCard(
                    modifier = Modifier.weight(1f),
                    emoji = "😊",
                    label = stringResource(R.string.feedback_happy),
                    colorBase = Color(0xFFE8F5E9),  // Fondo verde pastel
                    colorBorde = Color(0xFF4CAF50), // Borde verde fuerte
                    onClick = { onFeedbackSelected("feliz") }
                )
                FeedbackCard(
                    modifier = Modifier.weight(1f),
                    emoji = "😐",
                    label = stringResource(R.string.feedback_neutral),
                    colorBase = Color(0xFFFFFDE7),  // Fondo amarillo pastel
                    colorBorde = Color(0xFFFFC107), // Borde amarillo fuerte
                    onClick = { onFeedbackSelected("neutral") }
                )
            }

            // Fila para Triste (Centrado e imponente)
            FeedbackCard(
                modifier = Modifier.fillMaxWidth(0.85f),
                emoji = "🙁",
                label = stringResource(R.string.feedback_sad),
                colorBase = Color(0xFFFFEBEE),  // Fondo rojo pastel
                colorBorde = Color(0xFFE57373), // Borde rojo fuerte
                onClick = { onFeedbackSelected("triste") }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun FeedbackCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    colorBase: Color,
    colorBorde: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colorBase),
        border = BorderStroke(4.dp, colorBorde), // Bordes resaltados para estimulación visual correcta
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Efecto gigante en el emoji
            Text(
                text = emoji,
                fontSize = 58.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorBorde, // Combinación cromática perfecta
                letterSpacing = 1.sp
            )
        }
    }
}