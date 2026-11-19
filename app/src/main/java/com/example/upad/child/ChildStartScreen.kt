package com.example.upad.child

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.upad.R
import com.example.upad.data.ColorIntensity
import com.example.upad.location.LocationTrackingService
import com.example.upad.ui.theme.extendedColors
import com.example.upad.utils.RoutineProgressCalculator
import com.example.upad.utils.Turnos
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.TaskItem

/**
 * Pantalla de inicio del dispositivo del niño. Si no está vinculado, muestra el
 * código que el tutor debe introducir; si lo está, las tareas del día por turnos.
 * Solo el turno en curso está disponible, y dentro de él solo la siguiente tarea
 * pendiente, para que el niño siga la rutina en orden.
 */
@Composable
fun ChildStartScreen(
    routineViewModel: RoutineViewModel,
    onNavigateToTask: (actividadNombre: String, turno: String) -> Unit,
    onNavigateToCompleted: () -> Unit,
    onPadreIdObtenido: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val deviceId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) }

    val codigoNiño by routineViewModel.codigoNiño.collectAsState()
    val estaVinculado by routineViewModel.estaVinculado.collectAsState()
    val cargando by routineViewModel.cargandoChild.collectAsState()
    val padreIdAsociado by routineViewModel.padreIdAsociado.collectAsState()
    val esPremiumPorPadre by routineViewModel.esPremiumPorPadre.collectAsState()
    var verTareasModoBasico by remember { mutableStateOf(false) }

    // ── Ubicación (plan premium) ────────────────────────────────────────────
    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcherPermisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { aceptado ->
        tienePermisoUbicacion = aceptado
    }
    LaunchedEffect(estaVinculado, esPremiumPorPadre) {
        if (estaVinculado && esPremiumPorPadre && !tienePermisoUbicacion) {
            launcherPermisos.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    // El rastreo lo hace un servicio en primer plano, así que continúa mientras el niño
    // realiza sus tareas o con la app en segundo plano. Se detiene al desvincular el
    // dispositivo, si el tutor deja de ser premium o al volver a la selección de rol.
    LaunchedEffect(estaVinculado, esPremiumPorPadre, tienePermisoUbicacion) {
        if (estaVinculado && esPremiumPorPadre && tienePermisoUbicacion) {
            LocationTrackingService.start(context)
        } else {
            LocationTrackingService.stop(context)
        }
    }

    // ── Vinculación con el tutor ────────────────────────────────────────────
    var mostrarDialogoDesvincular by remember { mutableStateOf(false) }
    DisposableEffect(deviceId) {
        routineViewModel.iniciarEscuchaDispositivoNiño(deviceId)
        onDispose { routineViewModel.detenerEscuchaDispositivoNiño() }
    }
    LaunchedEffect(padreIdAsociado) {
        if (padreIdAsociado.isNotEmpty()) onPadreIdObtenido(padreIdAsociado)
    }

    // ── Tareas de hoy por turno ─────────────────────────────────────────────
    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()
    val diaDeHoy = remember { RoutineProgressCalculator.obtenerDiaDeHoy() }
    val turnoActual = remember { Turnos.actual() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            when {
                cargando -> CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )

                !estaVinculado -> CodigoVinculacion(codigo = codigoNiño)

                esPremiumPorPadre || verTareasModoBasico -> TareasDelDia(
                    tasksManana = tasksManana,
                    tasksTarde = tasksTarde,
                    tasksNoche = tasksNoche,
                    turnoActual = turnoActual,
                    diaDeHoy = diaDeHoy,
                    onNavigateToTask = onNavigateToTask
                )

                else -> ListoParaEmpezar(onVerTareas = { verTareasModoBasico = true })
            }

            // Acceso oculto para el tutor: una pulsación larga abre la desvinculación.
            if (estaVinculado && !cargando) {
                AccesoTutor(
                    onLongPress = { mostrarDialogoDesvincular = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }

    if (mostrarDialogoDesvincular) {
        DialogoDesvincular(
            routineViewModel = routineViewModel,
            deviceId = deviceId,
            onCerrar = { mostrarDialogoDesvincular = false }
        )
    }
}

/**
 * Lista de las tareas de hoy por turno, sin estado. Se puede previsualizar y
 * capturar en tests sin ViewModel ni Firebase.
 */
@Composable
internal fun TareasDelDia(
    tasksManana: List<TaskItem>,
    tasksTarde: List<TaskItem>,
    tasksNoche: List<TaskItem>,
    turnoActual: String,
    diaDeHoy: String,
    onNavigateToTask: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.my_activities_today),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(end = 48.dp)
                .semantics { heading() }
        )
        listOf(
            Triple(Turnos.MANANA, tasksManana, R.string.morning_activities),
            Triple(Turnos.TARDE, tasksTarde, R.string.afternoon_activities),
            Triple(Turnos.NOCHE, tasksNoche, R.string.evening_activities)
        ).forEach { (turno, tareas, titulo) ->
            Spacer(modifier = Modifier.height(4.dp))
            SeccionTurno(
                turno = turno,
                titulo = stringResource(titulo),
                activo = turno == turnoActual,
                tareas = RoutineProgressCalculator.filtrarTareasPorDia(tareas, diaDeHoy),
                diaDeHoy = diaDeHoy,
                onNavigateToTask = onNavigateToTask
            )
        }
    }
}

/** Código de 6 dígitos que el tutor introduce en su teléfono para vincular el dispositivo. */
@Composable
private fun CodigoVinculacion(codigo: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.child_pairing_instructions),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        val descripcion = codigo.toList().joinToString(" ")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            // TalkBack lee el código dígito a dígito.
            modifier = Modifier.semantics { contentDescription = descripcion }
        ) {
            for (i in 0 until 6) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(width = 44.dp, height = 60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = codigo.getOrNull(i)?.toString() ?: "–",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (i == 2) Spacer(modifier = Modifier.width(8.dp))
            }
        }
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/** Plan básico: el niño entra a sus tareas pulsando un botón. */
@Composable
private fun ListoParaEmpezar(onVerTareas: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ready_to_start_activities),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        ChildPrimaryButton(
            text = stringResource(R.string.view_my_tasks),
            icon = Icons.Default.PlayArrow,
            onClick = onVerTareas
        )
    }
}

/** Colores del turno: fondo suave y texto con contraste suficiente. */
@Composable
private fun coloresTurno(turno: String): Pair<Color, Color> {
    val colores = MaterialTheme.extendedColors
    return when (turno) {
        Turnos.MANANA -> colores.turnoManana to colores.onTurnoManana
        Turnos.TARDE -> colores.turnoTarde to colores.onTurnoTarde
        else -> colores.turnoNoche to colores.onTurnoNoche
    }
}

/** Horario de cada turno, igual en todos los idiomas. */
private fun horarioTurno(turno: String): String = when (turno) {
    Turnos.MANANA -> "0:00 – 13:00"
    Turnos.TARDE -> "13:00 – 18:00"
    else -> "18:00 – 24:00"
}

@Composable
private fun SeccionTurno(
    turno: String,
    titulo: String,
    activo: Boolean,
    tareas: List<TaskItem>,
    diaDeHoy: String,
    onNavigateToTask: (String, String) -> Unit
) {
    val (fondoTurno, textoTurno) = coloresTurno(turno)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = if (activo) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = horarioTurno(turno),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (activo) {
            Surface(shape = RoundedCornerShape(12.dp), color = fondoTurno) {
                Text(
                    text = stringResource(R.string.active),
                    style = MaterialTheme.typography.labelMedium,
                    color = textoTurno,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    when {
        !activo -> TurnoBloqueado(
            mensaje = stringResource(
                when (turno) {
                    Turnos.MANANA -> R.string.available_morning_only
                    Turnos.TARDE -> R.string.available_afternoon_only
                    else -> R.string.available_evening_only
                }
            )
        )

        tareas.isEmpty() -> Text(
            text = stringResource(R.string.no_activities_for_turn),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        else -> {
            // Solo la primera tarea pendiente está disponible, para seguir la rutina en orden.
            val siguiente = RoutineProgressCalculator.siguientePendiente(tareas, diaDeHoy)
            tareas.forEach { tarea ->
                TareaItem(
                    tarea = tarea,
                    turno = turno,
                    completada = tarea.estaCompletadaHoy(diaDeHoy),
                    disponible = tarea == siguiente,
                    fondoDisponible = fondoTurno,
                    textoDisponible = textoTurno,
                    onClick = { onNavigateToTask(tarea.actividad, turno) }
                )
            }
        }
    }
}

@Composable
private fun TurnoBloqueado(mensaje: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Una tarea de la lista. La disponible usa el color del turno y un botón de jugar;
 * las completadas muestran un visto y las que aún no tocan, un candado.
 */
@Composable
private fun TareaItem(
    tarea: TaskItem,
    turno: String,
    completada: Boolean,
    disponible: Boolean,
    fondoDisponible: Color,
    textoDisponible: Color,
    onClick: () -> Unit
) {
    val perfil = LocalSensoryProfile.current
    val fondo = when {
        disponible -> fondoDisponible
        completada -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val texto = if (disponible) textoDisponible else MaterialTheme.colorScheme.onSurfaceVariant
    val borde = if (disponible && perfil.intensidadColor == ColorIntensity.NORMAL) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Surface(
        onClick = onClick,
        enabled = disponible,
        shape = RoundedCornerShape(24.dp),
        color = fondo,
        border = borde,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.extendedColors.fondoPictograma,
                modifier = Modifier.size(64.dp).alpha(if (disponible) 1f else 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (tarea.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = tarea.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(6.dp)
                        )
                    } else {
                        Text(
                            text = tarea.actividad.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.extendedColors.onFondoPictograma
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.actividad.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = texto
                )
                Text(
                    text = stringResource(R.string.activity_duration_turn, tarea.duration.toString(), getLocalizedTurnName(turno)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = texto
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            when {
                completada -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.success,
                    modifier = Modifier.size(36.dp)
                )

                disponible -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ChildColors.acento)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ChildColors.sobreAcento)
                }

                else -> Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Botón casi invisible para el tutor; se activa con una pulsación larga para que el niño no lo abra por accidente. */
@Composable
private fun AccesoTutor(onLongPress: () -> Unit, modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.parent_confirmation_title)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(8.dp)
            .size(48.dp)
            .semantics { contentDescription = descripcion }
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp).alpha(0.3f)
        )
    }
}

/** El tutor confirma con su correo y contraseña antes de desvincular el dispositivo. */
@Composable
private fun DialogoDesvincular(
    routineViewModel: RoutineViewModel,
    deviceId: String,
    onCerrar: () -> Unit
) {
    var emailPadre by remember { mutableStateOf("") }
    var passwordPadre by remember { mutableStateOf("") }
    val cargandoDesvinculacion by routineViewModel.cargandoDesvinculacion.collectAsState()
    val errorDesvinculacion by routineViewModel.errorDesvinculacion.collectAsState()
    val cerrar = {
        routineViewModel.clearUnlinkError()
        onCerrar()
    }

    AlertDialog(
        onDismissRequest = { if (!cargandoDesvinculacion) cerrar() },
        title = { Text(stringResource(R.string.parent_confirmation_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.parent_confirmation_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = emailPadre,
                    onValueChange = { emailPadre = it },
                    label = { Text(stringResource(R.string.parent_email_label)) },
                    singleLine = true,
                    enabled = !cargandoDesvinculacion,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = passwordPadre,
                    onValueChange = { passwordPadre = it },
                    label = { Text(stringResource(R.string.parent_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !cargandoDesvinculacion,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorDesvinculacion.isNotEmpty()) {
                    Text(
                        text = errorDesvinculacion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    routineViewModel.desvincularDispositivo(
                        deviceId = deviceId,
                        email = emailPadre,
                        pass = passwordPadre,
                        onSuccess = onCerrar
                    )
                },
                enabled = !cargandoDesvinculacion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                if (cargandoDesvinculacion) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(stringResource(R.string.unlink_action_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = cerrar, enabled = !cargandoDesvinculacion) {
                Text(stringResource(R.string.cancel_action_btn))
            }
        }
    )
}

@Composable
fun getLocalizedTurnName(turn: String): String {
    val resId = when (turn.uppercase()) {
        Turnos.MANANA -> R.string.morning
        Turnos.TARDE -> R.string.afternoon
        Turnos.NOCHE -> R.string.evening
        else -> return turn
    }
    return stringResource(resId)
}
