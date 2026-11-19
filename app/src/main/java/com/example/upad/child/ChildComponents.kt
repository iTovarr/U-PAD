package com.example.upad.child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.upad.R
import com.example.upad.data.TimerStyle
import com.example.upad.ui.theme.extendedColors

/**
 * Altura mínima de los botones del modo niño: bastante más que los 48 dp que pide
 * Android, para que sean fáciles de pulsar con poca precisión motora.
 */
val ChildButtonMinHeight = 72.dp

private val ChildButtonShape = RoundedCornerShape(24.dp)

/**
 * Botón principal de una pantalla del niño. Ocupa todo el ancho y cada pantalla lo
 * coloca siempre abajo, para que el niño sepa dónde buscarlo.
 */
@Composable
fun ChildPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ChildButtonShape,
        colors = ChildColors.botonPrincipal(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ChildButtonMinHeight)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
    }
}

/** Botón secundario del modo niño, con borde y sin relleno. */
@Composable
fun ChildSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        shape = ChildButtonShape,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

/**
 * Temporizador visual de la tarea en el estilo que eligió el tutor.
 *
 * Usa el color primario de la paleta (azul o terracota apagados) y pasa al verde de
 * "completado" al terminar: nunca rojo, que suele asociarse a una alarma.
 *
 * @param restante Fracción del tiempo que queda, de 1 (recién empezada) a 0.
 * @param textoTiempo Tiempo restante en formato mm:ss.
 */
@Composable
fun ChildTimer(
    estilo: TimerStyle,
    restante: Float,
    textoTiempo: String,
    terminado: Boolean,
    modifier: Modifier = Modifier
) {
    if (estilo == TimerStyle.NINGUNO) return

    val color = if (terminado) MaterialTheme.extendedColors.success else MaterialTheme.colorScheme.primary
    val pista = MaterialTheme.colorScheme.surfaceVariant
    val descripcion = stringResource(R.string.child_time_left, textoTiempo)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        // El lector de pantalla anuncia el tiempo una sola vez, no cada dibujo del círculo.
        modifier = modifier.clearAndSetSemantics { contentDescription = descripcion }
    ) {
        when (estilo) {
            TimerStyle.CIRCULO -> Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val grosor = 14.dp.toPx()
                    drawArc(pista, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(grosor))
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * restante.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(grosor, cap = StrokeCap.Round)
                    )
                }
                if (terminado) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(56.dp))
                } else {
                    Text(
                        text = textoTiempo,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            TimerStyle.BARRA -> {
                LinearProgressIndicator(
                    progress = { restante.coerceIn(0f, 1f) },
                    color = color,
                    trackColor = pista,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (terminado) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                    } else {
                        Text(
                            text = textoTiempo,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            TimerStyle.NINGUNO -> Unit
        }
    }
}
