package com.example.upad.setup

import com.example.upad.ui.theme.TextoTenueClaro
import com.example.upad.ui.theme.TextoSecundarioClaro
import androidx.compose.ui.semantics.Role
import com.example.upad.R
import androidx.compose.ui.res.stringResource
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TutorialsScreen(
    onBackClick: () -> Unit,
    onFinishSetupClick: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)
    val uriHandler = LocalUriHandler.current // Manejador para abrir YouTube

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
                .padding(top = 40.dp, bottom = 30.dp, start = 16.dp, end = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back), tint = colorAzulTEA)
            }
            Text(
                text = stringResource(id = R.string.tutorials_overline),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = TextoTenueClaro,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = stringResource(id = R.string.tutorials_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // --- LISTA DE TUTORIALES ---
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Tutorial 1: Crear Actividad
            TutorialCard(
                title = stringResource(id = R.string.tutorials_create_title),
                description = stringResource(id = R.string.tutorials_create_text),
                onWatchClick = { uriHandler.openUri("https://www.youtube.com/watch?v=xvFZjo5PgG0") }
            )

            // Tutorial 2: Modificar Pasos
            TutorialCard(
                title = stringResource(id = R.string.tutorials_edit_title),
                description = stringResource(id = R.string.tutorials_edit_text),
                onWatchClick = { uriHandler.openUri("https://www.youtube.com/watch?v=xvFZjo5PgG0") }
            )
        }

        // --- BOTÓN FINALIZAR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(30.dp)
        ) {
            Button(
                onClick = onFinishSetupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
            ) {
                Text(stringResource(id = R.string.tutorials_finish), fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun TutorialCard(
    title: String,
    description: String,
    onWatchClick: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Miniatura" del video
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorAzulTEA.copy(alpha = 0.1f))
                    .clickable(role = Role.Button) { onWatchClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = stringResource(id = R.string.cd_play),
                    tint = colorAzulTEA,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextoSecundarioClaro,
                    lineHeight = 16.sp
                )
                TextButton(
                    onClick = onWatchClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(stringResource(id = R.string.tutorials_watch_video), color = colorAzulTEA, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}