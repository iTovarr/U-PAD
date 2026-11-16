package com.example.upad.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.viewmodel.TaskItem
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.Calendar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.upad.R
import coil.compose.AsyncImage
import com.example.upad.components.UPADBackgroundWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    routineTurn: String = "Mañana",
    childName: String = "tu hijo",
    pasosSeleccionados: List<TaskItem>,
    onBackClick: () -> Unit,
    onNavigateToPictogramSearch: () -> Unit,
    onSendRoutine: () -> Unit,
    onRemoveTaskClick: (Int) -> Unit,
    viewModel: RoutineViewModel,
    drawableId: Int,
    diaInicial: String = "LUNES"
) {
    val isPremiumUser by viewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    val aiCustomRoutine by viewModel.aiCustomRoutine.collectAsState()
    val aiRefuerzos by viewModel.aiRefuerzos.collectAsState()
    val isCustomRoutineLoading by viewModel.isCustomRoutineLoading.collectAsState()

    var customNeedText by remember { mutableStateOf("") }

    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)
    val colorFondoBase = Color.Transparent // 🔑 Deja ver el degradado del Wrapper trasero

    // Superficie limpia translúcida sin grises forzados automáticos de Material 3
    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    }

    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: "PADRE_TEST"
    }

    var routineName by remember(routineTurn) { mutableStateOf("Rutina de la $routineTurn") }
    var showSendingDialog by remember { mutableStateOf(false) }
    var nombreActividad by remember { mutableStateOf("") }

    var isLoadingAI by remember { mutableStateOf(false) }
    val sugerenciasIAByMenu = remember { mutableStateListOf<String>() }
    var mostrarPopUpSugerencias by remember { mutableStateOf(false) }

    LaunchedEffect(aiSuggestions) {
        if (aiSuggestions.isNotEmpty()) {
            sugerenciasIAByMenu.clear()
            sugerenciasIAByMenu.addAll(aiSuggestions)
            mostrarPopUpSugerencias = true
            viewModel.clearAiSuggestions()
        }
    }

    if (aiError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearAiError() },
            title = { Text("Error de la IA") },
            text = { Text(aiError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAiError() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    val diasDeLaSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val diasSeleccionados = remember { mutableStateListOf<String>() }

    val diaActualDelReloj = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lun"
            Calendar.TUESDAY -> "Mar"
            Calendar.WEDNESDAY -> "Mié"
            Calendar.THURSDAY -> "Jue"
            Calendar.FRIDAY -> "Vie"
            Calendar.SATURDAY -> "Sáb"
            Calendar.SUNDAY -> "Dom"
            else -> "Lun"
        }
    }

    val diaInicialFormateado = remember(diaInicial) {
        when (diaInicial.uppercase().trim().take(3)) {
            "LUN" -> "Lun"
            "MAR" -> "Mar"
            "MIE", "MIÉ" -> "Mié"
            "JUE" -> "Jue"
            "VIE" -> "Vie"
            "SAB", "SÁB" -> "Sáb"
            "DOM" -> "Dom"
            else -> "Lun"
        }
    }

    var diaFiltroSeleccionado by remember(diaInicialFormateado) { mutableStateOf(diaInicialFormateado) }

    val pasosFiltradosPorDia = remember(pasosSeleccionados, diaFiltroSeleccionado) {
        pasosSeleccionados.filter { tarea ->
            tarea.dias.isEmpty() || tarea.dias.any {
                it.uppercase().trim().startsWith(diaFiltroSeleccionado.uppercase())
            }
        }
    }

    // 💾 DIÁLOGO DE GUARDADO FINAL
    if (showSendingDialog) {
        SendingRoutineDialog(
            routineTurn = routineTurn,
            childName = childName,
            isPremiumUser = isPremiumUser,
            colorFondo = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
            colorTexto = colorTextoPrincipal,
            colorProgreso = colorAcabadoPrincipal,
            onDismiss = { showSendingDialog = false }
        )

        LaunchedEffect(Unit) {
            viewModel.saveAll(currentUserId, routineTurn)
            delay(2000)
            showSendingDialog = false
            onSendRoutine()
        }
    }

    // 🤖 DIÁLOGO FLOTANTE DE RECOMENDACIONES IA
    if (mostrarPopUpSugerencias) {
        AIOptionsDialog(
            opciones = sugerenciasIAByMenu,
            colorFondo = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
            colorTexto = colorTextoPrincipal,
            colorTextoSec = colorTextoSecundario,
            colorAccion = Color(0xFFC5A059),
            onDismiss = { mostrarPopUpSugerencias = false },
            onOpcionSelected = { actividadElegida ->
                mostrarPopUpSugerencias = false

                val diasAGuardar = if (diasSeleccionados.isEmpty()) {
                    listOf(diaFiltroSeleccionado)
                } else {
                    diasSeleccionados.toList()
                }

                viewModel.agregarActividadAutomatica(
                    userId = currentUserId,
                    turn = routineTurn,
                    textoCompleto = actividadElegida,
                    diasSeleccionados = diasAGuardar
                )

                diaFiltroSeleccionado = diasAGuardar.first()
                nombreActividad = ""
                diasSeleccionados.clear()
            }
        )
    }

    // 🚀 WRAPPER PRINCIPAL QUE PINTA EL DEGRADADO DE FONDO ADAPTATIVO
    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = colorFondoBase,
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = colorSuperficieTarjetas,
                    tonalElevation = 0.dp, // 🛠️ Matamos el plomo de Material 3
                    shadowElevation = if (isPremiumUser) 0.dp else 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorTextoSecundario
                            ),
                            border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.3f))
                        ) {
                            Text(stringResource(id = R.string.cancel), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showSendingDialog = true },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorAcabadoPrincipal,
                                contentColor = Color.White
                            )
                        ) {
                            Text(stringResource(id = R.string.save_action), fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(colorSuperficieTarjetas)
                            .padding(top = 20.dp, bottom = 24.dp, start = 16.dp, end = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(id = R.string.back),
                                    tint = colorAcabadoPrincipal
                                )
                            }

                            if (isPremiumUser) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                colorAcabadoPrincipal.copy(alpha = 0.12f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = colorAcabadoPrincipal,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            stringResource(id = R.string.premium_badge),
                                            color = colorAcabadoPrincipal,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { /* Navegar a pantalla de planes */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorAcabadoPrincipal.copy(alpha = 0.12f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = colorAcabadoPrincipal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        stringResource(id = R.string.basic_badge),
                                        color = colorAcabadoPrincipal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val localizedTurn = when (routineTurn.uppercase()) {
                            "MAÑANA" -> stringResource(id = R.string.morning)
                            "TARDE" -> stringResource(id = R.string.afternoon)
                            "NOCHE" -> stringResource(id = R.string.evening)
                            else -> routineTurn
                        }

                        Text(
                            text = stringResource(id = R.string.turn_label, localizedTurn.uppercase()),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colorTextoSecundario.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 12.dp),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stringResource(id = R.string.edit_activities_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = colorTextoPrincipal,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        OutlinedTextField(
                            value = routineName,
                            onValueChange = {
                                routineName = it
                                viewModel.updateName(it)
                            },
                            label = { Text(stringResource(id = R.string.routine_name_label), color = colorTextoSecundario) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorAcabadoPrincipal,
                                unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.3f),
                                focusedTextColor = colorTextoPrincipal,
                                unfocusedTextColor = colorTextoPrincipal
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = colorSuperficieTarjetas,
                            border = BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(id = R.string.add_activity_section),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorAcabadoPrincipal,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = nombreActividad,
                                    onValueChange = { nombreActividad = it },
                                    placeholder = {
                                        Text(
                                            stringResource(id = R.string.add_activity_placeholder),
                                            color = colorTextoSecundario.copy(alpha = 0.6f)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorAcabadoPrincipal,
                                        unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.2f),
                                        focusedTextColor = colorTextoPrincipal,
                                        unfocusedTextColor = colorTextoPrincipal
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = stringResource(id = R.string.scheduled_days_label),
                                    fontSize = 13.sp,
                                    color = colorTextoSecundario
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    diasDeLaSemana.forEach { dia ->
                                        val estaSeleccionado = diasSeleccionados.contains(dia)
                                        val dayResId = when (dia) {
                                            "Lun" -> R.string.day_mon
                                            "Mar" -> R.string.day_tue
                                            "Mié" -> R.string.day_wed
                                            "Jue" -> R.string.day_thu
                                            "Vie" -> R.string.day_fri
                                            "Sáb" -> R.string.day_sat
                                            else -> R.string.day_sun
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (estaSeleccionado) colorAcabadoPrincipal
                                                    else if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color(0xFFEEEEEE)
                                                )
                                                .clickable {
                                                    if (estaSeleccionado) diasSeleccionados.remove(dia)
                                                    else diasSeleccionados.add(dia)
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(id = dayResId),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (estaSeleccionado) Color.White
                                                else colorTextoSecundario
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (nombreActividad.isNotEmpty()) {
                                            viewModel.agregarActividadAutomatica(
                                                userId = currentUserId,
                                                turn = routineTurn,
                                                textoCompleto = nombreActividad,
                                                diasSeleccionados = if (diasSeleccionados.isEmpty()) listOf(diaFiltroSeleccionado) else diasSeleccionados.toList()
                                            )
                                            if (diasSeleccionados.isNotEmpty()) {
                                                diaFiltroSeleccionado = diasSeleccionados.first()
                                            }
                                            nombreActividad = ""
                                            diasSeleccionados.clear()
                                        }
                                    },
                                    enabled = nombreActividad.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth().height(45.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorAcabadoPrincipal,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(id = R.string.add_manually_btn), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                if (isPremiumUser) {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            viewModel.obtenerSugerenciasIA(routineTurn)
                                        },
                                        enabled = !isAiLoading,
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorAcabadoPrincipal,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        if (isAiLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                stringResource(id = R.string.processing_ai),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                stringResource(id = R.string.suggest_activity_ai),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = colorTextoSecundario.copy(alpha = 0.15f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Asistente de Rutinas y Refuerzos por IA ✨",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colorTextoPrincipal
                                    )
                                    Text(
                                        text = "Ingresa una meta o necesidad específica del niño para generar una rutina de tareas paso a paso y mensajes de refuerzo.",
                                        fontSize = 12.sp,
                                        color = colorTextoSecundario
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = customNeedText,
                                        onValueChange = { customNeedText = it },
                                        placeholder = { Text("Ej: preparar mochila, ordenar juguetes...", fontSize = 13.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colorAcabadoPrincipal,
                                            unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.3f),
                                            focusedTextColor = colorTextoPrincipal,
                                            unfocusedTextColor = colorTextoPrincipal
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            viewModel.obtenerRutinaYRefuerzosPersonalizados(customNeedText)
                                        },
                                        enabled = customNeedText.isNotEmpty() && !isCustomRoutineLoading,
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal)
                                    ) {
                                        if (isCustomRoutineLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                        } else {
                                            Text("Diseñar Actividades y Refuerzos", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (aiCustomRoutine.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = colorTextoSecundario.copy(alpha = 0.05f),
                                            border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Rutina Propuesta:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colorTextoPrincipal)
                                                aiCustomRoutine.forEachIndexed { i, act ->
                                                    Text("${i + 1}. $act", fontSize = 12.sp, color = colorTextoPrincipal)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Mensajes de Refuerzo para el niño:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colorTextoPrincipal)
                                                aiRefuerzos.forEach { ref ->
                                                    Text("• \"$ref\"", fontSize = 12.sp, color = colorTextoSecundario)
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Button(
                                                    onClick = {
                                                        aiCustomRoutine.forEach { actividad ->
                                                            viewModel.agregarActividadAutomatica(
                                                                userId = currentUserId,
                                                                turn = routineTurn,
                                                                textoCompleto = actividad,
                                                                diasSeleccionados = if (diasSeleccionados.isEmpty()) listOf(diaFiltroSeleccionado) else diasSeleccionados.toList()
                                                            )
                                                        }
                                                        viewModel.guardarMensajesRefuerzoPremium(
                                                            userId = currentUserId,
                                                            turn = routineTurn,
                                                            mensajes = aiRefuerzos
                                                        )
                                                        viewModel.clearCustomRoutine()
                                                        customNeedText = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Importar Actividades y Refuerzos", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = stringResource(id = R.string.view_weekly_schedule),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorTextoSecundario,
                            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(diasDeLaSemana) { dia ->
                                val esElDiaActivo = (dia == diaFiltroSeleccionado)
                                val esHoyDelSistema = (dia == diaActualDelReloj)

                                val dayResId = when (dia) {
                                    "Lun" -> R.string.day_mon
                                    "Mar" -> R.string.day_tue
                                    "Mié" -> R.string.day_wed
                                    "Jue" -> R.string.day_thu
                                    "Vie" -> R.string.day_fri
                                    "Sáb" -> R.string.day_sat
                                    else -> R.string.day_sun
                                }
                                val dayName = stringResource(id = dayResId)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                esElDiaActivo -> colorAcabadoPrincipal
                                                esHoyDelSistema -> colorAcabadoPrincipal.copy(alpha = 0.2f)
                                                else -> colorSuperficieTarjetas
                                            }
                                        )
                                        .clickable {
                                            diaFiltroSeleccionado = dia
                                            viewModel.cargarRutinasPorDia(currentUserId, dia)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (esHoyDelSistema) "$dayName ${stringResource(id = R.string.today_label_parenthesis)}" else dayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (esElDiaActivo || esHoyDelSistema)
                                            FontWeight.Bold else FontWeight.Medium,
                                        color = if (esElDiaActivo) Color.White
                                        else if (esHoyDelSistema) colorAcabadoPrincipal
                                        else colorTextoPrincipal
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    val activeDayResId = when (diaFiltroSeleccionado) {
                        "Lun" -> R.string.day_mon
                        "Mar" -> R.string.day_tue
                        "Mié" -> R.string.day_wed
                        "Jue" -> R.string.day_thu
                        "Vie" -> R.string.day_fri
                        "Sáb" -> R.string.day_sat
                        else -> R.string.day_sun
                    }
                    val activeDayName = stringResource(id = activeDayResId)

                    Text(
                        text = stringResource(
                            id = R.string.activities_of_day_count,
                            activeDayName.uppercase(),
                            pasosFiltradosPorDia.size
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTextoPrincipal,
                        modifier = Modifier.padding(
                            start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp
                        )
                    )
                }

                if (pasosFiltradosPorDia.isEmpty()) {
                    item {
                        val activeDayResId = when (diaFiltroSeleccionado) {
                            "Lun" -> R.string.day_mon
                            "Mar" -> R.string.day_tue
                            "Mié" -> R.string.day_wed
                            "Jue" -> R.string.day_thu
                            "Vie" -> R.string.day_fri
                            "Sáb" -> R.string.day_sat
                            else -> R.string.day_sun
                        }
                        val activeDayName = stringResource(id = activeDayResId)
                        Text(
                            text = stringResource(id = R.string.no_activities_for_day, activeDayName),
                            fontSize = 14.sp,
                            color = colorTextoSecundario,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 24.dp)
                        )
                    }
                } else {
                    itemsIndexed(pasosFiltradosPorDia) { _, paso ->
                        val indexRealEnFirebase = pasosSeleccionados.indexOfFirst {
                            it.actividad == paso.actividad && it.dias == paso.dias
                        }

                        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp)) {
                            PasoItemCard(
                                tarea = paso,
                                colorSuperficie = colorSuperficieTarjetas,
                                colorTexto = colorTextoPrincipal,
                                colorTextoSec = colorTextoSecundario,
                                colorDetalle = colorAcabadoPrincipal,
                                onDeleteClick = {
                                    if (indexRealEnFirebase != -1) {
                                        onRemoveTaskClick(indexRealEnFirebase)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIOptionsDialog(
    opciones: List<String>,
    colorFondo: Color,
    colorTexto: Color,
    colorTextoSec: Color,
    colorAccion: Color,
    onDismiss: () -> Unit,
    onOpcionSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colorAccion,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.ai_recommendations),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.select_ai_activity_desc),
                    fontSize = 13.sp,
                    color = colorTextoSec,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                opciones.forEach { opcion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpcionSelected(opcion) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = colorFondo),
                        border = BorderStroke(1.2.dp, colorAccion.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Text(
                                text = opcion,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorTexto,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(id = R.string.cancel),
                    color = colorTextoSec,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(24.dp),
        containerColor = colorFondo
    )
}

@Composable
fun SendingRoutineDialog(
    routineTurn: String,
    childName: String,
    isPremiumUser: Boolean,
    colorFondo: Color,
    colorTexto: Color,
    colorProgreso: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        shape = RoundedCornerShape(24.dp),
        containerColor = colorFondo,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = colorProgreso,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                val localizedTurn = when (routineTurn.uppercase()) {
                    "MAÑANA" -> stringResource(id = R.string.morning)
                    "TARDE" -> stringResource(id = R.string.afternoon)
                    "NOCHE" -> stringResource(id = R.string.evening)
                    else -> routineTurn
                }
                Text(
                    text = stringResource(id = R.string.saving_routine_progress, localizedTurn),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}

@Composable
fun PasoItemCard(
    tarea: TaskItem,
    colorSuperficie: Color,
    colorTexto: Color,
    colorTextoSec: Color,
    colorDetalle: Color,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        border = BorderStroke(1.dp, colorDetalle.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorDetalle.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (tarea.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(tarea.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = tarea.actividad,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                        error = painterResource(android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = colorDetalle.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.actividad,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                val localizedDays = tarea.dias.map { dia ->
                    val clean = dia.uppercase()
                        .replace("Á", "A")
                        .replace("É", "E")
                        .replace("Í", "I")
                        .replace("Ó", "O")
                        .replace("Ú", "U")
                        .trim()
                    val dayResId = when (clean) {
                        "LUN" -> R.string.day_mon
                        "MAR" -> R.string.day_tue
                        "MIE", "MIÉ" -> R.string.day_wed
                        "JUE" -> R.string.day_thu
                        "VIE" -> R.string.day_fri
                        "SAB", "SÁB" -> R.string.day_sat
                        "DOM" -> R.string.day_sun
                        else -> R.string.day_mon
                    }
                    stringResource(id = dayResId)
                }
                Text(
                    text = if (tarea.dias.isNotEmpty())
                        stringResource(id = R.string.days_label_prefix, localizedDays.joinToString(", "))
                    else
                        stringResource(id = R.string.days_all),
                    fontSize = 12.sp,
                    color = colorTextoSec
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                    tint = Color(0xFFEF5350)
                )
            }
        }
    }
}