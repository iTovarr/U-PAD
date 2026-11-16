package com.example.upad.child

import android.provider.Settings
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.upad.R
import com.example.upad.viewmodel.RoutineViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import java.util.Calendar

@Composable
fun ChildStartScreen(
    routineViewModel: RoutineViewModel,
    onNavigateToTask: (actividadNombre: String, turno: String) -> Unit,
    onNavigateToCompleted: () -> Unit,
    onPadreIdObtenido: (String) -> Unit = {}  // FIX: nuevo parámetro para propagar el padreId real
) {
    val colorFondoNiño = Color(0xFFF4F9FC)
    val context = LocalContext.current

    val deviceId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) }

    val codigoNiño by routineViewModel.codigoNiño.collectAsState()
    val estaVinculado by routineViewModel.estaVinculado.collectAsState()
    val cargando by routineViewModel.cargandoChild.collectAsState()
    val padreIdAsociado by routineViewModel.padreIdAsociado.collectAsState()
    val esPremiumPorPadre by routineViewModel.esPremiumPorPadre.collectAsState()
    var verTareasModoBasico by remember { mutableStateOf(false) }

    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { aceptado ->
        tienePermisoUbicacion = aceptado
    }

    LaunchedEffect(estaVinculado, esPremiumPorPadre) {
        if (estaVinculado && esPremiumPorPadre && !tienePermisoUbicacion) {
            launcherPermisos.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(estaVinculado, esPremiumPorPadre, tienePermisoUbicacion) {
        var callback: LocationCallback? = null
        var client: com.google.android.gms.location.FusedLocationProviderClient? = null

        if (estaVinculado && esPremiumPorPadre && tienePermisoUbicacion) {
            try {
                client = LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 10000L
                ).setMinUpdateIntervalMillis(5000L).build()

                callback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        if (location != null) {
                            routineViewModel.updateLocation(deviceId, location.latitude, location.longitude)
                        }
                    }
                }

                client.requestLocationUpdates(
                    locationRequest,
                    callback,
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        onDispose {
            if (client != null && callback != null) {
                client.removeLocationUpdates(callback)
            }
        }
    }

    var mostrarDialogoDesvincular by remember { mutableStateOf(false) }
    var emailPadre by remember { mutableStateOf("") }
    var passwordPadre by remember { mutableStateOf("") }
    val cargandoDesvinculacion by routineViewModel.cargandoDesvinculacion.collectAsState()
    val errorDesvinculacion by routineViewModel.errorDesvinculacion.collectAsState()

    DisposableEffect(deviceId) {
        routineViewModel.iniciarEscuchaDispositivoNiño(deviceId)
        onDispose {
            routineViewModel.detenerEscuchaDispositivoNiño()
        }
    }

    LaunchedEffect(padreIdAsociado) {
        if (padreIdAsociado.isNotEmpty()) {
            onPadreIdObtenido(padreIdAsociado)
        }
    }

    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()

    val calendar = Calendar.getInstance()
    val numeroDia = calendar.get(Calendar.DAY_OF_WEEK)
    val horaActual = calendar.get(Calendar.HOUR_OF_DAY)

    val diaActualTexto = when (numeroDia) {
        Calendar.MONDAY -> "LUN"
        Calendar.TUESDAY -> "MAR"
        Calendar.WEDNESDAY -> "MIÉ"
        Calendar.THURSDAY -> "JUE"
        Calendar.FRIDAY -> "VIE"
        Calendar.SATURDAY -> "SÁB"
        Calendar.SUNDAY -> "DOM"
        else -> "LUN"
    }

    val listaVariacionesDia = when (numeroDia) {
        Calendar.MONDAY -> listOf("LUN", "LUNES")
        Calendar.TUESDAY -> listOf("MAR", "MARTES")
        Calendar.WEDNESDAY -> listOf("MIE", "MIÉ", "MIERCOLES", "MIÉRCOLES")
        Calendar.THURSDAY -> listOf("JUE", "JUEVES")
        Calendar.FRIDAY -> listOf("VIE", "VIERNES")
        Calendar.SATURDAY -> listOf("SAB", "SÁB", "SABADO", "SÁBADO")
        Calendar.SUNDAY -> listOf("DOM", "DOMINGO")
        else -> emptyList()
    }

    val filtradasManana = remember(tasksManana, listaVariacionesDia) {
        tasksManana.filter { item ->
            try {
                val listaDias = item.javaClass.getMethod("getDias").invoke(item) as? List<*> ?: emptyList<String>()
                listaDias.isEmpty() || listaDias.any { d -> listaVariacionesDia.contains(d.toString().uppercase().trim()) }
            } catch (e: Exception) { true }
        }
    }

    val filtradasTarde = remember(tasksTarde, listaVariacionesDia) {
        tasksTarde.filter { item ->
            try {
                val listaDias = item.javaClass.getMethod("getDias").invoke(item) as? List<*> ?: emptyList<String>()
                listaDias.isEmpty() || listaDias.any { d -> listaVariacionesDia.contains(d.toString().uppercase().trim()) }
            } catch (e: Exception) { true }
        }
    }

    val filtradasNoche = remember(tasksNoche, listaVariacionesDia) {
        tasksNoche.filter { item ->
            try {
                val listaDias = item.javaClass.getMethod("getDias").invoke(item) as? List<*> ?: emptyList<String>()
                listaDias.isEmpty() || listaDias.any { d -> listaVariacionesDia.contains(d.toString().uppercase().trim()) }
            } catch (e: Exception) { true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (esPremiumPorPadre)
                    Brush.verticalGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFBC02D).copy(alpha = 0.3f)))
                else
                    Brush.verticalGradient(listOf(colorFondoNiño, colorFondoNiño))
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(color = Color(0xFF0D47A1))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                if (!estaVinculado) {
                    Text(
                        text = stringResource(R.string.child_pairing_instructions),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        val codeStr = codigoNiño
                        for (i in 0 until 6) {
                            val digit = codeStr.getOrNull(i)?.toString() ?: "-"
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
                                border = BorderStroke(2.5.dp, Color(0xFF0D47A1)),
                                modifier = Modifier.size(width = 44.dp, height = 62.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = digit,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0D47A1),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            if (i == 2) {
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(color = Color(0xFF0D47A1), strokeWidth = 5.dp)
                } else {
                    if (esPremiumPorPadre || verTareasModoBasico) {
                        Text(
                            text = stringResource(R.string.my_activities_today),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        )

                        val turnoMananaActivo = horaActual < 13
                        val turnoTardeActivo = horaActual in 13..17
                        val turnoNocheActivo = horaActual >= 18

                        var yaSeEncontroLaActivaGlobal = false

                        SeccionTurnoTitulo(titulo = stringResource(R.string.morning_activities), activo = turnoMananaActivo, horasTexto = "(12:00 AM - 1:00 PM)")
                        if (turnoMananaActivo) {
                            BloqueListaTareas(
                                tareas = filtradasManana,
                                turnoNombre = "MAÑANA",
                                diaActualTexto = diaActualTexto,
                                yaSeEncontroActiva = yaSeEncontroLaActivaGlobal,
                                onNavigateToTask = onNavigateToTask,
                                marcarActivaEncontrada = { yaSeEncontroLaActivaGlobal = true }
                            )
                        } else {
                            CardBloqueadoPorHorario(mensaje = stringResource(R.string.available_morning_only))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        SeccionTurnoTitulo(titulo = stringResource(R.string.afternoon_activities), activo = turnoTardeActivo, horasTexto = "(1:00 PM - 6:00 PM)")
                        if (turnoTardeActivo) {
                            BloqueListaTareas(
                                tareas = filtradasTarde,
                                turnoNombre = "TARDE",
                                diaActualTexto = diaActualTexto,
                                yaSeEncontroActiva = yaSeEncontroLaActivaGlobal,
                                onNavigateToTask = onNavigateToTask,
                                marcarActivaEncontrada = { yaSeEncontroLaActivaGlobal = true }
                            )
                        } else {
                            CardBloqueadoPorHorario(mensaje = stringResource(R.string.available_afternoon_only))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        SeccionTurnoTitulo(titulo = stringResource(R.string.evening_activities), activo = turnoNocheActivo, horasTexto = "(6:00 PM - 12:00 AM)")
                        if (turnoNocheActivo) {
                            BloqueListaTareas(
                                tareas = filtradasNoche,
                                turnoNombre = "NOCHE",
                                diaActualTexto = diaActualTexto,
                                yaSeEncontroActiva = yaSeEncontroLaActivaGlobal,
                                onNavigateToTask = onNavigateToTask,
                                marcarActivaEncontrada = { yaSeEncontroLaActivaGlobal = true }
                            )
                        } else {
                            CardBloqueadoPorHorario(mensaje = stringResource(R.string.available_evening_only))
                        }

                    } else {
                        Text(
                            text = stringResource(R.string.ready_to_start_activities),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 60.dp)
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = { verTareasModoBasico = true },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(85.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(stringResource(R.string.view_my_tasks), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoDesvincular) {
        AlertDialog(
            onDismissRequest = {
                if (!cargandoDesvinculacion) {
                    mostrarDialogoDesvincular = false
                    emailPadre = ""
                    passwordPadre = ""
                    routineViewModel.clearUnlinkError()
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.parent_confirmation_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A1A)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.parent_confirmation_desc),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = emailPadre,
                        onValueChange = { emailPadre = it },
                        label = { Text(stringResource(R.string.parent_email_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargandoDesvinculacion
                    )
                    OutlinedTextField(
                        value = passwordPadre,
                        onValueChange = { passwordPadre = it },
                        label = { Text(stringResource(R.string.parent_password_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargandoDesvinculacion
                    )
                    if (errorDesvinculacion.isNotEmpty()) {
                        Text(
                            text = errorDesvinculacion,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
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
                            onSuccess = {
                                mostrarDialogoDesvincular = false
                                emailPadre = ""
                                passwordPadre = ""
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    enabled = !cargandoDesvinculacion
                ) {
                    if (cargandoDesvinculacion) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.unlink_action_btn), color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoDesvincular = false
                        emailPadre = ""
                        passwordPadre = ""
                        routineViewModel.clearUnlinkError()
                    },
                    enabled = !cargandoDesvinculacion
                ) {
                    Text(stringResource(R.string.cancel_action_btn))
                }
            }
        )
    }

    if (estaVinculado && !cargando) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                mostrarDialogoDesvincular = true
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙️",
                    fontSize = 20.sp,
                    modifier = Modifier.alpha(0.25f)
                )
            }
        }
    }
}

@Composable
fun SeccionTurnoTitulo(titulo: String, activo: Boolean, horasTexto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = titulo,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = if (activo) Color(0xFF1A1A1A) else Color(0xFF90A4AE)
            )
            Text(text = horasTexto, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
        if (activo) {
            SuggestionChip(
                onClick = {},
                label = { Text(stringResource(R.string.active), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    labelColor = Color(0xFF2E7D32),
                    containerColor = Color(0xFFE8F5E9)
                )
            )
        }
    }
}

@Composable
fun CardBloqueadoPorHorario(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
        border = BorderStroke(1.dp, Color(0xFFCFD8DC))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF78909C))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = mensaje, fontSize = 13.sp, color = Color(0xFF607D8B), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun BloqueListaTareas(
    tareas: List<Any>,
    turnoNombre: String,
    diaActualTexto: String,
    yaSeEncontroActiva: Boolean,
    onNavigateToTask: (String, String) -> Unit,
    marcarActivaEncontrada: () -> Unit
) {
    if (tareas.isEmpty()) {
        Text(
            stringResource(R.string.no_activities_for_turn),
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(8.dp)
        )
    } else {
        var localActivaEncontrada = yaSeEncontroActiva
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            tareas.forEachIndexed { index, task ->
                val clase = task.javaClass
                val nombreActividad = try {
                    clase.getMethod("getActividad").invoke(task)?.toString() ?: ""
                } catch (e: Exception) { "" }

                var duracionText = try {
                    clase.getMethod("getDuration").invoke(task)?.toString() ?: "5"
                } catch (e: Exception) {
                    try { clase.getMethod("getDuracion").invoke(task)?.toString() ?: "5" } catch (ex: Exception) { "5" }
                }

                if (duracionText.trim() == "55" || duracionText.trim() == "0") {
                    duracionText = "5"
                }

                val imageUrlText = try {
                    clase.getMethod("getImageUrl").invoke(task)?.toString() ?: ""
                } catch (e: Exception) { "" }

                val completada = try {
                    clase.getMethod("estaCompletadaHoy", String::class.java).invoke(task, diaActualTexto) as Boolean
                } catch (e: Exception) { false }

                val estaHabilitada = if (completada) {
                    false
                } else if (!localActivaEncontrada) {
                    localActivaEncontrada = true
                    marcarActivaEncontrada()
                    true
                } else {
                    false
                }

                if (nombreActividad.isNotEmpty()) {
                    ItemActividadContenedor(
                        nombre = nombreActividad,
                        duracion = duracionText,
                        imageUrl = imageUrlText,
                        turno = turnoNombre,
                        isCompletada = completada,
                        isHabilitada = estaHabilitada,
                        index = index,
                        onClick = {
                            if (estaHabilitada && !completada) {
                                onNavigateToTask(nombreActividad, turnoNombre)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemActividadContenedor(
    nombre: String,
    duracion: String,
    imageUrl: String,
    turno: String,
    isCompletada: Boolean,
    isHabilitada: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    val colorBaseDinamico = when (index % 4) {
        0 -> Color(0xFFE3F2FD)
        1 -> Color(0xFFE8F5E9)
        2 -> Color(0xFFF3E5F5)
        else -> Color(0xFFFFF3E0)
    }

    val colorBordeDinamico = when (index % 4) {
        0 -> Color(0xFF1E88E5)
        1 -> Color(0xFF4CAF50)
        2 -> Color(0xFFAB47BC)
        else -> Color(0xFFFF9800)
    }

    val colorContenedor = when {
        isCompletada -> Color(0xFFECEFF1)
        !isHabilitada -> Color(0xFFF5F5F5)
        else -> colorBaseDinamico
    }

    val colorTexto = if (isCompletada || !isHabilitada) Color(0xFF78909C) else Color(0xFF1A1A1A)

    val colorBorde = when {
        isCompletada -> Color(0xFFCFD8DC)
        !isHabilitada -> Color(0xFFE0E0E0)
        else -> colorBordeDinamico
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isHabilitada && !isCompletada) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorContenedor),
        border = BorderStroke(2.5.dp, colorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHabilitada && !isCompletada) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Card(
                    modifier = Modifier.size(68.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = nombre,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                contentScale = ContentScale.Fit,
                                alpha = if (isHabilitada && !isCompletada) 1f else 0.35f
                            )
                        } else {
                            Text(
                                text = nombre.take(1).uppercase(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = nombre.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = colorTexto
                    )
                    Text(
                        text = stringResource(R.string.activity_duration_turn, duracion, getLocalizedTurnName(turno)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHabilitada && !isCompletada) Color.DarkGray else Color(0xFF90A4AE)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompletada -> Color.Transparent
                            !isHabilitada -> Color.Transparent
                            else -> colorBordeDinamico
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompletada -> Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF90A4AE))
                    !isHabilitada -> Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF90A4AE))
                    else -> Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun getLocalizedTurnName(turn: String): String {
    val resId = when (turn.uppercase()) {
        "MAÑANA" -> R.string.morning
        "TARDE" -> R.string.afternoon
        "NOCHE" -> R.string.evening
        else -> return turn
    }
    return stringResource(resId)
}