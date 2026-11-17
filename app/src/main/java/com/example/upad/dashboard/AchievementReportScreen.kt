package com.example.upad.dashboard

import com.example.upad.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.dashboard.components.UpadTopAppBar

@Composable
fun AchievementReportScreen(
    onBackClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    isPremiumUser: Boolean = false,
    isDarkMode: Boolean = false,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)
    val colorVerdeLogro = Color(0xFF81C784)

    // Lógica obligatoria para calcular el fondo sólido del componente
    val colorFondoSolido = if (isPremiumUser) {
        if (isDarkMode) Color(0xFF0F0C29) else Color(0xFFFBE9E7)
    } else {
        if (isDarkMode) Color(0xFF121212) else Color.White
    }

    Scaffold(
        topBar = {
            UpadTopAppBar(
                title = stringResource(id = R.string.report_title),
                imageUri = null,
                colorFondo = colorFondoSolido,
                showBackButton = true,
                onBackPressed = onBackClick,
                onNavigateToHelp = onNavigateToHelp,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorFondoBase)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- CONTENEDOR DEL GRÁFICO ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.report_weekly_activity),
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Icon(Icons.Default.Insights, contentDescription = null, tint = colorAzulTEA)
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Aquí integrarás tu librería de gráficos más adelante
                        Text(
                            stringResource(id = R.string.report_chart_placeholder),
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem(label = stringResource(id = R.string.report_autonomous), color = colorVerdeLogro)
                        LegendItem(label = stringResource(id = R.string.report_with_help), color = Color(0xFFFFD54F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TARJETA DE RESUMEN DE IMPACTO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colorVerdeLogro.copy(alpha = 0.1f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorVerdeLogro.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = colorVerdeLogro,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.report_autonomy_sample),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = stringResource(id = R.string.report_weekly_praise),
                            fontSize = 14.sp,
                            color = Color(0xFF455A64),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTÓN DE ACCIÓN ---
            Button(
                onClick = onViewDetailsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    stringResource(id = R.string.report_details_button),
                    color = colorAzulTEA,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}