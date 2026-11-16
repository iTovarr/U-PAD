package com.example.upad.dashboard

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import com.example.upad.R
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class DispositivoVinculado(val id: String, val modelo: String = "Dispositivo del Niño")

@Composable
fun ConnectionScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onLinkSuccess: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // 📨 1. RECOLECCIÓN DE ESTADOS GLOBALES (Cabecera del componente)
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    // --- NUEVOS ESTADOS Y CONFIGURACIÓN REQUERIDA ---
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val sharedPreferences = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        val saved = sharedPreferences.getString("PROFILE_IMAGE_URI", null)
        imageUri = when {
            saved != null -> Uri.parse(saved)
            currentUser?.photoUrl != null -> currentUser.photoUrl
            else -> null
        }
    }

    val fuentePremium = FontFamily.SansSerif
    val colorFondoSolido = if (isPremiumUser) {
        if (isDarkMode) Color(0xFF0F0C29) else Color(0xFFFBE9E7)
    } else {
        if (isDarkMode) Color(0xFF121212) else Color.White
    }

    // 🎨 CONFIGURACIÓN DE PALETA ADAPTATIVA EQUILIBRADA
    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    // Configuración estructural para tarjetas: Translúcidas (Premium) o Sólidas (Básico)
    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.7f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF7F7F7)
    }

    // Colores de estado fijos que combinan bien en ambos modos
    val colorVerdePremium = Color(0xFF2E7D32)
    val colorRojoSuave = Color(0xFFEF5350)

    val idPadre = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    var codigoIngresado by remember { mutableStateOf("") }
    var cargandoVerificacion by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    val listaDispositivos = remember { mutableStateListOf<DispositivoVinculado>() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    // --- ESCUCHAR MULTIDISPOSITIVOS EN TIEMPO REAL ---
    DisposableEffect(idPadre) {
        val listener = firestore.collection("dispositivos_niños")
            .whereEqualTo("padreId", idPadre)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cargandoVerificacion = false
                    return@addSnapshotListener
                }
                listaDispositivos.clear()
                if (snapshot != null) {
                    for (dispositivo in snapshot.documents) {
                        val model = dispositivo.getString("modelo") ?: "Dispositivo del Niño"
                        listaDispositivos.add(DispositivoVinculado(id = dispositivo.id, modelo = model))
                    }
                }
                cargandoVerificacion = false
            }

        onDispose {
            listener.remove()
        }
    }

    // 🚀 2. IMPLEMENTACIÓN DEL CONTENEDOR BASE
    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = stringResource(id = R.string.devices_title),
                    imageUri = imageUri,
                    colorFondo = colorFondoSolido,
                    colorIconos = colorTextoPrincipal,
                    fuentePremium = fuentePremium,
                    showBackButton = true,
                    onBackPressed = onNavigateBack,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToHelp = onNavigateToHelp
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (cargandoVerificacion) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorAcabadoPrincipal)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // --- SECCIÓN 1: ENCABEZADO ILUSTRATIVO ---
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    colorAcabadoPrincipal.copy(alpha = 0.2f),
                                                    colorAcabadoPrincipal.copy(alpha = 0.05f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Devices,
                                        contentDescription = null,
                                        tint = colorAcabadoPrincipal,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(id = R.string.device_management_title),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colorTextoPrincipal,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(id = R.string.device_management_desc),
                                    fontSize = 14.sp,
                                    color = colorTextoSecundario,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }

                        // --- SECCIÓN 2: FORMULARIO DE VINCULACIÓN ---
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                                border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.3f)) else BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isPremiumUser) 0.dp else 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = stringResource(id = R.string.connect_new_device),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorAcabadoPrincipal,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = codigoIngresado,
                                        onValueChange = { if (it.length <= 6) codigoIngresado = it },
                                        label = { Text(stringResource(id = R.string.child_code_label)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colorAcabadoPrincipal,
                                            focusedLabelColor = colorAcabadoPrincipal,
                                            unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.2f),
                                            focusedTextColor = colorTextoPrincipal,
                                            unfocusedTextColor = colorTextoPrincipal,
                                            unfocusedLabelColor = colorTextoSecundario
                                        )
                                    )

                                    if (mensajeError.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = mensajeError,
                                            color = colorRojoSuave,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            if (codigoIngresado.length == 6) {
                                                mensajeError = ""
                                                firestore.collection("codigos_vinculacion").document(codigoIngresado)
                                                    .get()
                                                    .addOnSuccessListener { snapshot ->
                                                        if (snapshot.exists()) {
                                                            val actualizaciones = mapOf(
                                                                "estado" to "enlazado",
                                                                "padreId" to idPadre
                                                            )
                                                            firestore.collection("codigos_vinculacion").document(codigoIngresado)
                                                                .update(actualizaciones)
                                                                .addOnSuccessListener {
                                                                    codigoIngresado = ""
                                                                    onLinkSuccess()
                                                                }
                                                                .addOnFailureListener {
                                                                    mensajeError = context.getString(R.string.network_error_linking)
                                                                }
                                                        } else {
                                                            mensajeError = context.getString(R.string.code_invalid_expired)
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        mensajeError = context.getString(R.string.network_error_linking)
                                                    }
                                            } else {
                                                mensajeError = context.getString(R.string.enter_6_numbers)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colorVerdePremium),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        Text(stringResource(id = R.string.link_device_btn), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // --- SECCIÓN 3: LISTA DE DISPOSITIVOS VINCULADOS ---
                        item {
                            Text(
                                text = stringResource(id = R.string.linked_devices_count, listaDispositivos.size),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = colorTextoSecundario,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                            )
                        }

                        if (listaDispositivos.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.no_devices_linked),
                                        fontSize = 14.sp,
                                        color = colorTextoSecundario,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp)
                                    )
                                }
                            }
                        } else {
                            items(listaDispositivos) { dispositivo ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isPremiumUser) 0.dp else 1.dp),
                                    border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.2f)) else BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(colorAcabadoPrincipal.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.PhonelinkRing,
                                                contentDescription = null,
                                                tint = colorAcabadoPrincipal,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            val displayModelo = if (dispositivo.modelo == "Dispositivo del Niño") {
                                                when (java.util.Locale.getDefault().language) {
                                                    "en" -> "Child's Device"
                                                    "fr" -> "Appareil de l'enfant"
                                                    "de" -> "Gerät des Kindes"
                                                    "pt" -> "Dispositivo da Criança"
                                                    "ru" -> "Устройство ребенка"
                                                    else -> "Dispositivo del Niño"
                                                }
                                            } else {
                                                dispositivo.modelo
                                            }
                                            Text(
                                                text = displayModelo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = colorTextoPrincipal
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "ID: ${dispositivo.id.take(8).uppercase()}...",
                                                fontSize = 12.sp,
                                                color = colorTextoSecundario
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                firestore.collection("dispositivos_niños").document(dispositivo.id).delete()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = stringResource(id = R.string.unlink),
                                                tint = colorRojoSuave
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}