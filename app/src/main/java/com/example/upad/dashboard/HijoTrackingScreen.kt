package com.example.upad.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import com.example.upad.components.UPADBackgroundWrapper // 🛠️ Importamos el contenedor unificado
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel // 🛠️ Importamos el ViewModel de estado Premium/Tema
import com.example.upad.viewmodel.TrackingViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HijoTrackingScreen(
    routineViewModel: RoutineViewModel, // 🛡️ AGREGADO DE FORMA SEGURA COMO PRIMER PARÁMETRO
    hijoId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    trackingViewModel: TrackingViewModel = viewModel()
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

    // 🎨 PALETA DE COLORES ADAPTATIVA EN BASE AL ESTADO PREMIUM Y MODO VISUAL
    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    // Configuración para tarjetas flotantes y TopBar: Translúcido en Premium o sólido en Básico
    val colorSuperficieFlotante = if (isPremiumUser) {
        if (isDarkMode) Color.Black.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.75f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    }

    val idPadre = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val dispositivosConectados by trackingViewModel.dispositivosConectados.collectAsState()
    var dispositivoSeleccionado by remember { mutableStateOf<DispositivoVinculado?>(null) }

    var miUbicacionReal by remember { mutableStateOf<LatLng?>(null) }
    val datosUbicacionNino by trackingViewModel.ubicacion.collectAsState()

    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPositionState = rememberCameraPositionState()

    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { aceptado ->
        tienePermisoUbicacion = aceptado
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoUbicacion) {
            launcherPermisos.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 📡 ESCUCHA DE UBICACIÓN DEL PADRE EN TIEMPO REAL
    LaunchedEffect(tienePermisoUbicacion) {
        if (tienePermisoUbicacion) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 5000L
                ).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        if (location != null) {
                            val pos = LatLng(location.latitude, location.longitude)
                            miUbicacionReal = pos

                            val ninoEsInvalido = datosUbicacionNino == null ||
                                    (datosUbicacionNino?.latitud == 0.0 && datosUbicacionNino?.longitud == 0.0)

                            if (ninoEsInvalido) {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 15f)
                            }
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // 📡 Lista de dispositivos vinculados en tiempo real
    LaunchedEffect(idPadre) {
        trackingViewModel.escucharDispositivosVinculados(idPadre)
    }

    LaunchedEffect(dispositivosConectados) {
        if (dispositivosConectados.isNotEmpty() && dispositivoSeleccionado == null) {
            val preferido = dispositivosConectados.find { it.id == hijoId }
            dispositivoSeleccionado = preferido ?: dispositivosConectados.first()
        }
    }

    LaunchedEffect(dispositivoSeleccionado) {
        dispositivoSeleccionado?.let {
            trackingViewModel.iniciarRastreoHijo(it.id)
        }
    }

    // 🎯 ENCUADRE DE CÁMARA DINÁMICO
    LaunchedEffect(datosUbicacionNino, miUbicacionReal) {
        val infoNino = datosUbicacionNino
        val infoPadre = miUbicacionReal

        if (infoNino != null && infoNino.latitud != 0.0 && infoNino.longitud != 0.0) {
            val coordenadasHijo = LatLng(infoNino.latitud, infoNino.longitud)

            if (infoPadre != null) {
                val limites = LatLngBounds.Builder()
                    .include(infoPadre)
                    .include(coordenadasHijo)
                    .build()

                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(limites, 150),
                    durationMs = 1000
                )
            } else {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(coordenadasHijo, 16f),
                    durationMs = 1000
                )
            }
        }
    }

    // 🚀 2. IMPLEMENTACIÓN DEL CONTENEDOR BASE
    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent, // Fondo transparente para que funcione la estética Premium/Fondo unificado
            topBar = {
                UpadTopAppBar(
                    title = "Ubicar a mi Hijo",
                    imageUri = imageUri,
                    colorFondo = colorFondoSolido,
                    colorIconos = colorAcabadoPrincipal,
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
                // El mapa base de Google Maps se mantiene a pantalla completa
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = tienePermisoUbicacion),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    // 👨‍🦰 Marcador de tu Ubicación Real
                    miUbicacionReal?.let { posPadre ->
                        MarkerComposable(
                            state = MarkerState(position = posPadre),
                            title = "Tu ubicación"
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(Color(0xFF007AFF), CircleShape)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👨‍🦰", fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    // 👦 Marcador de la Ubicación Real del Menor
                    datosUbicacionNino?.let { info ->
                        if (info.latitud != 0.0 && info.longitud != 0.0) {
                            val coordenadasHijo = LatLng(info.latitud, info.longitud)
                            MarkerComposable(
                                state = MarkerState(position = coordenadasHijo),
                                title = dispositivoSeleccionado?.modelo ?: "Hijo"
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👦", fontSize = 22.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Carrusel horizontal superior flotante de dispositivos adaptado con Glassmorphism
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(dispositivosConectados) { dispositivo ->
                            val esElSeleccionado = dispositivo.id == dispositivoSeleccionado?.id

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (esElSeleccionado) colorAcabadoPrincipal else colorSuperficieFlotante
                                ),
                                border = if (isPremiumUser && !esElSeleccionado) androidx.compose.foundation.BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isPremiumUser) 0.dp else 6.dp),
                                modifier = Modifier.clickable { dispositivoSeleccionado = dispositivo }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (esElSeleccionado) "🎯" else "📱",
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = dispositivo.modelo,
                                        color = if (esElSeleccionado) Color.White else colorTextoPrincipal,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
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