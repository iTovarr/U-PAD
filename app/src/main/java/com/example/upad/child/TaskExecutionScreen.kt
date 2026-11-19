package com.example.upad.child

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.upad.R
import com.example.upad.data.CelebrationStyle
import com.example.upad.data.SensoryProfile
import com.example.upad.data.TaskItemMapper
import com.example.upad.di.LocalAppContainer
import com.example.upad.ui.theme.extendedColors
import com.example.upad.utils.RoutineProgressCalculator
import com.example.upad.utils.Turnos
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.TaskItem
import kotlinx.coroutines.delay

/**
 * Pantalla en la que el niño realiza una tarea: pictograma grande, nombre de la
 * tarea, temporizador como guía y un único botón para terminar, siempre abajo.
 */
@Composable
fun TaskExecutionScreen(
    viewModel: RoutineViewModel,
    activityName: String,
    turn: String,
    padreId: String,
    onFinishRoutine: (String) -> Unit
) {
    val session = LocalAppContainer.current.sessionRepository
    val currentUserId = remember { padreId.ifEmpty { session.currentUserId } }

    // Tareas del turno de esta tarea
    val tasks by when (turn.uppercase()) {
        Turnos.MANANA -> viewModel.tasksManana.collectAsState()
        Turnos.TARDE -> viewModel.tasksTarde.collectAsState()
        else -> viewModel.tasksNoche.collectAsState()
    }
    val currentTask = remember(tasks, activityName) {
        tasks.firstOrNull { it.actividad.uppercase().trim() == activityName.uppercase().trim() }
    }

    var tareaTerminada by remember { mutableStateOf(false) }

    // Voz y sonido según el perfil sensorial que eligió el tutor.
    val perfil = LocalSensoryProfile.current
    val audio = rememberChildAudio()
    LaunchedEffect(currentTask?.actividad) {
        currentTask?.let { audio.leer(it.actividad, perfil) }
    }

    // Temporizador: dura lo que el tutor asignó a la tarea (en minutos). Es una guía
    // visual: el niño puede terminar cuando acabe la tarea.
    val tiempoInicialSegundos = remember(currentTask) {
        (currentTask?.duration ?: TaskItemMapper.DURACION_POR_DEFECTO).coerceAtLeast(1) * 60
    }
    var tiempoRestanteSegundos by remember(tiempoInicialSegundos) { mutableStateOf(tiempoInicialSegundos) }
    LaunchedEffect(currentTask, tareaTerminada) {
        if (currentTask != null && !tareaTerminada) {
            while (tiempoRestanteSegundos > 0) {
                delay(1000)
                tiempoRestanteSegundos -= 1
            }
        }
    }
    val locale = LocalConfiguration.current.locales[0]
    val textoTemporizador = String.format(
        locale, "%02d:%02d", tiempoRestanteSegundos / 60, tiempoRestanteSegundos % 60
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            currentTask == null -> Cargando()

            tareaTerminada -> Celebracion(
                perfil = perfil,
                onContinuar = { onFinishRoutine(activityName) }
            )

            else -> TareaEnCurso(
                tarea = currentTask,
                perfil = perfil,
                restante = tiempoRestanteSegundos.toFloat() / tiempoInicialSegundos,
                textoTiempo = textoTemporizador,
                tiempoCumplido = tiempoRestanteSegundos == 0,
                onEscuchar = { audio.leer(currentTask.actividad, perfil) },
                onTerminar = {
                    audio.sonarTareaCompletada(perfil)
                    viewModel.completeTaskPorNombre(
                        userId = currentUserId,
                        turn = turn,
                        actividadTexto = currentTask.actividad,
                        diaActual = RoutineProgressCalculator.obtenerDiaDeHoy()
                    )
                    tareaTerminada = true
                }
            )
        }
    }
}

/**
 * Contenido de la tarea en curso, sin estado: recibe los datos ya calculados y
 * avisa de las acciones. Se puede previsualizar y probar sin ViewModel ni Firebase.
 */
@Composable
internal fun TareaEnCurso(
    tarea: TaskItem,
    perfil: SensoryProfile,
    restante: Float,
    textoTiempo: String,
    tiempoCumplido: Boolean,
    onEscuchar: () -> Unit,
    onTerminar: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        // Contenido desplazable por si la pantalla es pequeña; el botón queda fijo abajo.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Pictograma(nombre = tarea.actividad, imageUrl = tarea.imageUrl)

            Text(
                text = tarea.actividad.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )

            if (perfil.voz) {
                ChildSecondaryButton(
                    text = stringResource(R.string.child_listen_again),
                    onClick = onEscuchar,
                    icon = Icons.AutoMirrored.Filled.VolumeUp
                )
            }

            ChildTimer(
                estilo = perfil.temporizador,
                restante = restante,
                textoTiempo = textoTiempo,
                terminado = tiempoCumplido
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        ChildPrimaryButton(
            text = stringResource(R.string.child_finished),
            icon = Icons.Default.Check,
            onClick = onTerminar
        )
    }
}

@Composable
private fun Cargando() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.loading_activity),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** Pictograma de la tarea, o su inicial si no tiene imagen. */
@Composable
private fun Pictograma(nombre: String, imageUrl: String) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.extendedColors.fondoPictograma,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.size(240.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(24.dp)
                )
            } else {
                Text(
                    text = nombre.take(1).uppercase(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.extendedColors.onFondoPictograma
                )
            }
        }
    }
}

/**
 * Felicitación al terminar la tarea. Discreta: un visto y un texto. Normal: además
 * la ilustración de celebración. Solo se anima si el tutor dejó activadas las
 * animaciones, y no avanza sola: el niño pulsa Continuar.
 */
@Composable
private fun Celebracion(perfil: SensoryProfile, onContinuar: () -> Unit) {
    val visible = remember { MutableTransitionState(!perfil.animaciones).apply { targetState = true } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            AnimatedVisibility(visibleState = visible, enter = fadeIn() + scaleIn(initialScale = 0.9f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                    Text(
                        text = stringResource(R.string.child_well_done),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = stringResource(R.string.focused_great_job),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ChildPrimaryButton(
            text = stringResource(R.string.child_mode_continue),
            onClick = onContinuar
        )
    }
}
