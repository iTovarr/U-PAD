package com.example.upad.child

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R
import com.example.upad.data.ColorIntensity
import com.example.upad.ui.theme.extendedColors

/**
 * Tras terminar una tarea, el niño indica cómo se sintió. Tres tarjetas grandes,
 * una debajo de otra y siempre en el mismo orden, con colores suaves de la paleta.
 */
@Composable
fun TaskFeedbackScreen(
    activityName: String,
    onFeedbackSelected: (String) -> Unit
) {
    val perfil = LocalSensoryProfile.current
    val audio = rememberChildAudio()
    val pregunta = stringResource(R.string.feedback_question)
    LaunchedEffect(Unit) {
        audio.leer("$pregunta $activityName", perfil)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = pregunta,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = activityName.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.heightIn(min = 8.dp))

            val colores = MaterialTheme.extendedColors
            EmotionCard(
                emoji = "😊",
                label = stringResource(R.string.feedback_happy),
                fondo = colores.emocionFeliz,
                onClick = { onFeedbackSelected("feliz") }
            )
            EmotionCard(
                emoji = "😐",
                label = stringResource(R.string.feedback_neutral),
                fondo = colores.emocionNeutral,
                onClick = { onFeedbackSelected("neutral") }
            )
            EmotionCard(
                emoji = "🙁",
                label = stringResource(R.string.feedback_sad),
                fondo = colores.emocionTriste,
                onClick = { onFeedbackSelected("triste") }
            )
        }
    }
}

/**
 * Tarjeta de una emoción. Con intensidad normal lleva un borde fino que la recorta
 * mejor; con intensidad suave, solo el color de fondo.
 */
@Composable
private fun EmotionCard(
    emoji: String,
    label: String,
    fondo: Color,
    onClick: () -> Unit
) {
    val perfil = LocalSensoryProfile.current
    val textoColor = MaterialTheme.extendedColors.onEmocion
    val borde = if (perfil.intensidadColor == ColorIntensity.NORMAL) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    } else {
        null
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = fondo,
        border = borde,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            // TalkBack lee solo el nombre de la emoción como un botón, no el emoji.
            .clearAndSetSemantics {
                contentDescription = label
                role = Role.Button
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                color = textoColor
            )
        }
    }
}
