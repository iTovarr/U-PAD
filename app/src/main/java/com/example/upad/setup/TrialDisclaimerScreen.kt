package com.example.upad.setup

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
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
    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background

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
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(top = 60.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.trial_overline),
                color = colorAzulTEA,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.trial_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
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
                    contentDescription = stringResource(id = R.string.cd_start_illustration),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(id = R.string.trial_description),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // --- PANEL DE ACCIÓN INFERIOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
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
                    text = stringResource(id = R.string.trial_start),
color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.trial_more_info),
                color = colorAzulTEA,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(role = Role.Button) { onMoreInfoClick() }
                    .padding(8.dp),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}