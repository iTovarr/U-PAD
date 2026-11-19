package com.example.upad.child

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.upad.R
import com.example.upad.data.CelebrationStyle
import com.example.upad.ui.theme.extendedColors

/**
 * Pantalla que cierra cada tarea: felicita al niño, le dice qué viene después
 * (la siguiente actividad o que ya no le quedan) y muestra un mensaje de refuerzo
 * del tutor. No avanza sola.
 *
 * @param nextActivityPreview Nombre de la siguiente actividad pendiente, o vacío si no queda ninguna.
 */
@Composable
fun RoutineCompletedScreen(
    nextActivityPreview: String = "",
    reinforcementMessages: List<String> = emptyList(),
    onFinishClick: () -> Unit
) {
    val perfil = LocalSensoryProfile.current
    val audio = rememberChildAudio()

    val esFinDeRutina = nextActivityPreview.isBlank()
    val mensajeRefuerzo = remember(reinforcementMessages) { reinforcementMessages.randomOrNull() }
    val titulo = stringResource(R.string.routine_completed_title)

    LaunchedEffect(Unit) {
        audio.leer(listOfNotNull(titulo, mensajeRefuerzo).joinToString(". "), perfil)
    }

    val visible = remember { MutableTransitionState(!perfil.animaciones).apply { targetState = true } }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedVisibility(visibleState = visible, enter = fadeIn()) {
                    if (perfil.celebracion == CelebrationStyle.NORMAL) {
                        Image(
                            painter = painterResource(id = R.drawable.celebracion),
                            contentDescription = stringResource(R.string.cd_celebration),
                            modifier = Modifier.size(180.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.extendedColors.success,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )

                // Qué viene después: la siguiente actividad o que ya no queda ninguna.
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(if (esFinDeRutina) R.string.today_status_label else R.string.next_activity_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (esFinDeRutina) stringResource(R.string.no_pending_tasks) else nextActivityPreview.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Mensaje de refuerzo que escribió el tutor (o generó con la IA).
                if (mensajeRefuerzo != null) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mensajeRefuerzo,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(if (esFinDeRutina) R.string.time_to_rest_or_play else R.string.keep_it_up),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            ChildPrimaryButton(
                text = stringResource(R.string.understood_btn),
                onClick = onFinishClick
            )
        }
    }
}
