package com.example.upad.dashboard

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R
import com.example.upad.dashboard.components.DashboardDrawerContent
import com.example.upad.dashboard.components.UpadTopAppBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

data class RoutineItem(
    val name: String,
    val icon: ImageVector,
    val totalTasks: Int,
    val completedTasks: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDashboardScreen(
    routineViewModel: com.example.upad.viewmodel.RoutineViewModel,
    onNavigateToCreateRoutine: (String, String) -> Unit,
    onRoutineClick: (String, String) -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToConnection: () -> Unit = {},
    onNavigateToDeviceManagement: () -> Unit = {},
    onNavigateToChangePlan: () -> Unit = {},
    onNavigateToTracking: (String) -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser = firebaseAuth.currentUser
    val context = LocalContext.current

    val sharedPreferences = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
    val imageUriString = sharedPreferences.getString("PROFILE_IMAGE_URI", null)
    val imageUri = remember(imageUriString) {
        imageUriString?.let { android.net.Uri.parse(it) } ?: currentUser?.photoUrl
    }

    // ── ESTADOS DEL VIEWMODEL ────────────────────────────────────────────────
    val esPremium by routineViewModel.isUserPremium.collectAsState()
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    // ── PALETA DE COLORES ADAPTATIVA ─────────────────────────────────────────
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF444444)
    val fuentePremium = FontFamily.SansSerif

    val gradienteFondoPremium = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF121212))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFBE9E7), Color(0xFFF3E5F5), Color(0xFFE8EAF6))
        )
    }

    val colorFondoBase = if (isDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = if (esPremium) {
        if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val colorDinamicoSuscripcion = if (esPremium) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary

    // ── DATOS DEL USUARIO ────────────────────────────────────────────────────
    val hijoVinculadoId = remember(currentUser) {
        sharedPreferences.getString("HIJO_VINCULADO_ID", "DISPOSITIVO_PADRE")
    }

    var parentName by remember {
        mutableStateOf(sharedPreferences.getString("PARENT_NAME", "PADRE/TUTOR") ?: "PADRE/TUTOR")
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val emailName = currentUser.email?.substringBefore("@")?.uppercase()
            val nameToDisplay = currentUser.displayName?.uppercase() ?: emailName
            if (!nameToDisplay.isNullOrBlank()) {
                parentName = nameToDisplay
                sharedPreferences.edit().putString("PARENT_NAME", nameToDisplay).apply()
            }
        }
    }

    val onUbicarHijoAccionUnificada = {
        onNavigateToTracking(hijoVinculadoId ?: "DISPOSITIVO_PADRE")
    }

    // ── ESTADO DEL DRAWER Y COROUTINE ────────────────────────────────────────
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ── ESTADO DEL CALENDARIO ────────────────────────────────────────────────
    val diasSemana = com.example.upad.utils.RoutineProgressCalculator.diasSemana
    val diaDeHoy = remember { com.example.upad.utils.RoutineProgressCalculator.obtenerDiaDeHoy() }
    val numeroDeHoyReal = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }

    var diaSeleccionado by remember { mutableStateOf(diaDeHoy) }
    var diaNumeroSeleccionado by remember { mutableStateOf(numeroDeHoyReal) }
    var mostrarCalendarioCompleto by remember { mutableStateOf(false) }
    var showTurnSelectionDialog by remember { mutableStateOf(false) }

    val currentUserId = remember(currentUser) { currentUser?.uid ?: "PADRE_TEST" }

    LaunchedEffect(diaSeleccionado, currentUserId) {
        routineViewModel.cargarRutinasPorDia(currentUserId, diaSeleccionado)
    }

    // ── INFO DEL MES ACTUAL ──────────────────────────────────────────────────
    val currentLocale = context.resources.configuration.locales[0]
    val infoMesActual = remember(currentLocale) {
        val cal = Calendar.getInstance()
        val nombreMes = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, currentLocale)?.uppercase() ?: ""
        val maxDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val listaDiasDelMes = (1..maxDias).map { dia ->
            val tempCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, dia) }
            val nombreDiaAsignado = when (tempCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "LUNES"
                Calendar.TUESDAY -> "MARTES"
                Calendar.WEDNESDAY -> "MIÉRCOLES"
                Calendar.THURSDAY -> "JUEVES"
                Calendar.FRIDAY -> "VIERNES"
                Calendar.SATURDAY -> "SÁBADO"
                Calendar.SUNDAY -> "DOMINGO"
                else -> "LUNES"
            }
            Pair(dia, nombreDiaAsignado)
        }
        Triple("$nombreMes ${cal.get(Calendar.YEAR)}", maxDias, listaDiasDelMes)
    }
    val tituloMes = infoMesActual.first
    val diasDelMesArray = infoMesActual.third

    // ── PROGRESO DE TAREAS ───────────────────────────────────────────────────
    val allTasksManana by routineViewModel.tasksManana.collectAsState()
    val allTasksTarde by routineViewModel.tasksTarde.collectAsState()
    val allTasksNoche by routineViewModel.tasksNoche.collectAsState()

    val prefijoDiaSeleccionado = com.example.upad.utils.RoutineProgressCalculator.obtenerPrefijoDia(diaSeleccionado)
    val progresoManana = com.example.upad.utils.RoutineProgressCalculator.calcularProgreso(allTasksManana, prefijoDiaSeleccionado)
    val progresoTarde = com.example.upad.utils.RoutineProgressCalculator.calcularProgreso(allTasksTarde, prefijoDiaSeleccionado)
    val progresoNoche = com.example.upad.utils.RoutineProgressCalculator.calcularProgreso(allTasksNoche, prefijoDiaSeleccionado)

    val totalTareasDia = progresoManana.first + progresoTarde.first + progresoNoche.first
    val completadasTareasDia = progresoManana.second + progresoTarde.second + progresoNoche.second
    val pendientesTareasDia = totalTareasDia - completadasTareasDia
    val porcentajeGlobal = if (totalTareasDia > 0) (completadasTareasDia.toFloat() / totalTareasDia * 100).toInt() else 0

    val routines = listOf(
        RoutineItem("MAÑANA", Icons.Default.LightMode, progresoManana.first, progresoManana.second, Color(0xFFFFB74D)),
        RoutineItem("TARDE", Icons.Default.WbTwilight, progresoTarde.first, progresoTarde.second, Color(0xFF81C784)),
        RoutineItem("NOCHE", Icons.Default.NightsStay, progresoNoche.first, progresoNoche.second, Color(0xFF9575CD))
    )

    // ════════════════════════════════════════════════════════════════════════
    //  UI PRINCIPAL
    // ════════════════════════════════════════════════════════════════════════
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // ✅ DRAWER SEPARADO EN SU PROPIO ARCHIVO
            DashboardDrawerContent(
                parentName = parentName,
                userEmail = currentUser?.email ?: "Gestor de Rutinas",
                imageUri = imageUri,
                esPremium = esPremium,
                isDarkMode = isDarkMode,
                colorTextoPrincipal = colorTextoPrincipal,
                colorTextoSecundario = colorTextoSecundario,
                colorDinamicoSuscripcion = colorDinamicoSuscripcion,
                gradienteFondoPremium = gradienteFondoPremium,
                fuentePremium = fuentePremium,
                onChangePlan = { scope.launch { drawerState.close() }; onNavigateToChangePlan() },
                onCancelPremium = { scope.launch { drawerState.close() }; routineViewModel.cancelPremium(currentUser?.uid ?: "") },
                onUbicarHijo = { scope.launch { drawerState.close() }; onUbicarHijoAccionUnificada() },
                onNavigateToAnalytics = { scope.launch { drawerState.close() }; onNavigateToAnalytics() },
                onNavigateToDeviceManagement = { scope.launch { drawerState.close() }; onNavigateToDeviceManagement() },
                onNavigateToConnection = { scope.launch { drawerState.close() }; onNavigateToConnection() },
                onNavigateToProfile = { scope.launch { drawerState.close() }; onNavigateToProfile() },
                onNavigateToSettings = { scope.launch { drawerState.close() }; onNavigateToSettings() }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (esPremium) Color.Transparent else colorFondoBase)
                .then(if (esPremium) Modifier.background(gradienteFondoPremium) else Modifier)
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                // ✅ TOP BAR SEPARADA EN SU PROPIO ARCHIVO
                topBar = {
                    UpadTopAppBar(
                        title = stringResource(R.string.child_routines),
                        imageUri = imageUri,
                        // Fondo sólido: si es premium usa el primer color del gradiente,
                        // si no usa el fondo base del tema. Nunca transparente.
                        colorFondo = if (esPremium) {
                            if (isDarkMode) Color(0xFF0F0C29) else Color(0xFFFBE9E7)
                        } else {
                            colorFondoBase
                        },
                        colorIconos = colorTextoPrincipal,
                        fuentePremium = fuentePremium,
                        showBackButton = false,
                        onOpenMenu = { scope.launch { drawerState.open() } },
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToHelp = onNavigateToHelp
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (esPremium) onUbicarHijoAccionUnificada()
                            else showTurnSelectionDialog = true
                        },
                        containerColor = if (isDarkMode) Color.White else Color.Black,
                        contentColor = if (isDarkMode) Color.Black else Color.White,
                        shape = CircleShape
                    ) {
                        if (esPremium) {
                            Icon(Icons.Default.Map, contentDescription = "Ver mapa", modifier = Modifier.size(28.dp))
                        } else {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_new_routine), modifier = Modifier.size(30.dp))
                        }
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        bottom = paddingValues.calculateBottomPadding() + 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── SALUDO (ya no lleva los iconos, esos están en el TopBar) ──
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.hello_parent, parentName),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = colorTextoSecundario.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(R.string.child_routines),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = colorTextoPrincipal,
                                fontFamily = fuentePremium
                            )
                        }
                    }

                    // ── TARJETA DE PROGRESO PREMIUM ──────────────────────────────
                    if (esPremium) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) Color(0xFF1F1F1F) else Color(0xFF1E222B)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = stringResource(R.string.todays_progress),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = fuentePremium
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                                            CircularProgressIndicator(
                                                progress = { if (totalTareasDia > 0) completadasTareasDia.toFloat() / totalTareasDia else 0f },
                                                modifier = Modifier.fillMaxSize(),
                                                color = Color(0xFF9CCC65),
                                                strokeWidth = 10.dp,
                                                trackColor = Color.White.copy(alpha = 0.12f)
                                            )
                                            Text(
                                                text = "$porcentajeGlobal%",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = fuentePremium
                                            )
                                        }
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.padding(start = 16.dp)
                                        ) {
                                            ProgressStatRow(icon = Icons.Default.Assignment, iconTint = Color.White, value = "$totalTareasDia", label = stringResource(R.string.total_tasks), fuentePremium = fuentePremium)
                                            ProgressStatRow(icon = Icons.Default.Assignment, iconTint = Color(0xFF9CCC65), value = "$completadasTareasDia", label = stringResource(R.string.completed_tasks), fuentePremium = fuentePremium)
                                            ProgressStatRow(icon = Icons.Default.Assignment, iconTint = Color(0xFFE57373), value = "$pendientesTareasDia", label = stringResource(R.string.pending_tasks), fuentePremium = fuentePremium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── SELECTOR DE DÍA HORIZONTAL ───────────────────────────────
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (diaSeleccionado == diaDeHoy && diaNumeroSeleccionado == numeroDeHoyReal) {
                                        stringResource(R.string.today_program) + " (${getLocalizedDayName(diaDeHoy)} $numeroDeHoyReal)"
                                    } else {
                                        stringResource(R.string.program_of) + " ${getLocalizedDayName(diaSeleccionado)}" +
                                                if (diaNumeroSeleccionado > 0) " $diaNumeroSeleccionado" else ""
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colorTextoPrincipal,
                                    fontFamily = fuentePremium
                                )
                                IconButton(
                                    onClick = { mostrarCalendarioCompleto = !mostrarCalendarioCompleto },
                                    modifier = Modifier.background(
                                        if (mostrarCalendarioCompleto) colorTextoPrincipal.copy(alpha = 0.1f) else Color.Transparent,
                                        CircleShape
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = stringResource(R.string.monthly_view),
                                        tint = colorDinamicoSuscripcion
                                    )
                                }
                            }

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(diasSemana) { dia ->
                                    val esElSeleccionado = dia == diaSeleccionado
                                    val esHoyReal = dia == diaDeHoy
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (esElSeleccionado) colorTextoPrincipal
                                                else if (esHoyReal) colorTextoPrincipal.copy(alpha = 0.12f)
                                                else colorSuperficieTarjetas
                                            )
                                            .clickable {
                                                diaSeleccionado = dia
                                                diaNumeroSeleccionado = if (dia == diaDeHoy) numeroDeHoyReal else -1
                                            }
                                            .padding(horizontal = 18.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = getLocalizedDayName(dia),
                                            color = if (esElSeleccionado) {
                                                if (isDarkMode) Color.Black else Color.White
                                            } else colorTextoPrincipal,
                                            fontWeight = if (esElSeleccionado || esHoyReal) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 13.sp,
                                            fontFamily = fuentePremium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── CALENDARIO MENSUAL PLEGABLE ──────────────────────────────
                    item {
                        AnimatedVisibility(visible = mostrarCalendarioCompleto) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tituloMes,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colorTextoPrincipal,
                                            letterSpacing = 0.5.sp,
                                            fontFamily = fuentePremium
                                        )
                                        Text(
                                            text = stringResource(R.string.monthly_view),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorTextoSecundario.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Iniciales de días de la semana
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val localeActual = java.util.Locale.getDefault()
                                        val initialsCal = Calendar.getInstance()
                                        val inicialesDias = (Calendar.SUNDAY..Calendar.SATURDAY).map { dayOfWeek ->
                                            initialsCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                                            initialsCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, localeActual)
                                                ?.take(1)?.uppercase(localeActual) ?: ""
                                        }
                                        inicialesDias.forEach { letra ->
                                            Text(
                                                text = letra,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Center,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorTextoSecundario.copy(alpha = 0.6f),
                                                fontFamily = fuentePremium
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Grid de días
                                    val calendarAux = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
                                    val espacioVacioInicial = calendarAux.get(Calendar.DAY_OF_WEEK) - 1
                                    var diaProcesadoIndex = 0
                                    val filasDeSemanas = (espacioVacioInicial + diasDelMesArray.size + 6) / 7

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        for (semana in 0 until filasDeSemanas) {
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                for (diaSemanaFila in 0 until 7) {
                                                    val posicionCelda = semana * 7 + diaSemanaFila
                                                    if (posicionCelda < espacioVacioInicial || diaProcesadoIndex >= diasDelMesArray.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    } else {
                                                        val datosDelDia = diasDelMesArray[diaProcesadoIndex]
                                                        val numeroDia = datosDelDia.first
                                                        val nombreDiaCompleto = datosDelDia.second
                                                        val esElSeleccionadoHoy = numeroDia == diaNumeroSeleccionado
                                                        val esDiaActualDelMes = numeroDia == numeroDeHoyReal

                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .aspectRatio(1f)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    when {
                                                                        esElSeleccionadoHoy -> colorTextoPrincipal
                                                                        esDiaActualDelMes -> colorTextoPrincipal.copy(alpha = 0.15f)
                                                                        else -> Color.Transparent
                                                                    }
                                                                )
                                                                .clickable {
                                                                    diaSeleccionado = nombreDiaCompleto
                                                                    diaNumeroSeleccionado = numeroDia
                                                                    mostrarCalendarioCompleto = false
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = numeroDia.toString(),
                                                                fontSize = 13.sp,
                                                                fontWeight = if (esElSeleccionadoHoy || esDiaActualDelMes) FontWeight.Black else FontWeight.Medium,
                                                                color = if (esElSeleccionadoHoy) {
                                                                    if (isDarkMode) Color.Black else Color.White
                                                                } else colorTextoPrincipal,
                                                                fontFamily = fuentePremium
                                                            )
                                                        }
                                                        diaProcesadoIndex++
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── ETIQUETA DE BLOQUES ──────────────────────────────────────
                    item {
                        Text(
                            text = if (diaNumeroSeleccionado > 0) {
                                stringResource(R.string.activity_blocks) + " - ${getLocalizedDayName(diaSeleccionado)} $diaNumeroSeleccionado"
                            } else {
                                stringResource(R.string.activity_blocks) + " - ${getLocalizedDayName(diaSeleccionado)}"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorTextoPrincipal,
                            fontFamily = fuentePremium,
                            modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 24.dp)
                        )
                    }

                    // ── TARJETAS MAÑANA / TARDE / NOCHE ─────────────────────────
                    items(routines) { routine ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            RoutineProgressCard(
                                routine = routine,
                                colorSuperficie = colorSuperficieTarjetas,
                                colorTexto = colorTextoPrincipal,
                                colorTextoSec = colorTextoSecundario,
                                fuente = fuentePremium,
                                onClick = { onRoutineClick(routine.name, diaSeleccionado) }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── DIÁLOGO SELECCIÓN DE TURNO ───────────────────────────────────────────
    if (showTurnSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showTurnSelectionDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.select_turn),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = colorTextoPrincipal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.choose_time_block, getLocalizedDayName(diaSeleccionado)),
                        fontSize = 14.sp,
                        color = colorTextoSecundario
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showTurnSelectionDialog = false; onNavigateToCreateRoutine("MAÑANA", diaSeleccionado) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
                    ) { Text("☀️ " + stringResource(R.string.morning), fontWeight = FontWeight.Bold, color = Color.White) }

                    Button(
                        onClick = { showTurnSelectionDialog = false; onNavigateToCreateRoutine("TARDE", diaSeleccionado) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
                    ) { Text("⛅ " + stringResource(R.string.afternoon), fontWeight = FontWeight.Bold, color = Color.White) }

                    Button(
                        onClick = { showTurnSelectionDialog = false; onNavigateToCreateRoutine("NOCHE", diaSeleccionado) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9575CD))
                    ) { Text("🌙 " + stringResource(R.string.evening), fontWeight = FontWeight.Bold, color = Color.White) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTurnSelectionDialog = false }) {
                    Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold, color = colorDinamicoSuscripcion)
                }
            },
            containerColor = colorSuperficieTarjetas,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  COMPONENTES LOCALES
// ════════════════════════════════════════════════════════════════════════════

/** Fila de estadística dentro de la tarjeta de progreso premium. */
@Composable
private fun ProgressStatRow(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    fuentePremium: FontFamily
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = fuentePremium)
            Text(text = label, color = Color.Gray, fontSize = 11.sp, fontFamily = fuentePremium)
        }
    }
}

@Composable
fun RoutineProgressCard(
    routine: RoutineItem,
    colorSuperficie: Color,
    colorTexto: Color,
    colorTextoSec: Color,
    fuente: FontFamily,
    onClick: () -> Unit
) {
    val factorProgreso = if (routine.totalTasks > 0) routine.completedTasks.toFloat() / routine.totalTasks else 0f
    val porcentajeTexto = (factorProgreso * 100).toInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = colorSuperficie,
        border = BorderStroke(1.dp, colorTextoSec.copy(alpha = 0.1f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(routine.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = routine.icon,
                            contentDescription = null,
                            tint = routine.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = getLocalizedTurnName(routine.name),
                            color = colorTexto,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = fuente
                        )
                        Text(
                            text = stringResource(R.string.tasks_completed, routine.completedTasks, routine.totalTasks),
                            color = colorTextoSec.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = fuente
                        )
                    }
                }
                Text(
                    text = "$porcentajeTexto%",
                    color = colorTexto,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = fuente
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { factorProgreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = routine.color,
                trackColor = routine.color.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
fun getLocalizedDayName(day: String): String {
    val cleanDay = day.uppercase()
        .replace("Á", "A").replace("É", "E").replace("Í", "I")
        .replace("Ó", "O").replace("Ú", "U").trim()
    val resId = when (cleanDay) {
        "LUN", "LUNES" -> R.string.monday
        "MAR", "MARTES" -> R.string.tuesday
        "MIE", "MIERCOLES" -> R.string.wednesday
        "JUE", "JUEVES" -> R.string.thursday
        "VIE", "VIERNES" -> R.string.friday
        "SAB", "SABADO" -> R.string.saturday
        "DOM", "DOMINGO" -> R.string.sunday
        else -> return day
    }
    return stringResource(resId)
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