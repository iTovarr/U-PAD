package com.example.upad.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R

@Composable
fun TrialDisclaimerScreen(
    onStartTrialClick: () -> Unit,
    onMoreInfoClick: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "COMIENZA TU PRUEBA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu primer paso hacia una rutina exitosa",
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        // --- CONTENIDO CENTRAL (Ilustración y Texto) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Contenedor de la Ilustración
            Surface(
                modifier = Modifier
                    .size(200.dp),
                color = Color.Transparent
            ) {
                Image(
                    painter = painterResource(id = R.drawable.upad_icono), // Asegúrate de tener esta imagen
                    contentDescription = "Ilustración de inicio",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Esta prueba te dará un punto de partida para la intervención y el apoyo adecuado para las rutinas de tu hijo.",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                color = Color(0xFF555555),
                lineHeight = 26.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // --- PANEL DE ACCIÓN INFERIOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onStartTrialClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    text = "VALE, EMPECEMOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Más información",
                color = colorAzulTEA,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onMoreInfoClick() }
                    .padding(8.dp)
            )
        }
    }
}