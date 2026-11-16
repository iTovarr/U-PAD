package com.example.upad.dashboard

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import com.example.upad.R
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.utils.BiometricHelper
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Usamos un modelo consistente con los datos reales guardados en Firebase
data class DispositivoNiño(
    val id: String = "",
    val nombreDispositivo: String = "Tablet/Celular Niño",
    val kioscoActivo: Boolean = false
)

@Composable
fun DeviceManagementScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // 📨 1. LLAMAR A LOS ESTADOS LOCALES EN LA CABECERA (Procedimiento Estándar)
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

    // 🎨 PALETA DE COLORES RE-ACOPLADA AL DISEÑO DEL CONTENEDOR
    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.7f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF7F7F7)
    }

    val activity = context as FragmentActivity

    var listaDispositivos by remember { mutableStateOf(listOf<DispositivoNiño>()) }
    var cargando by remember { mutableStateOf(true) }

    val firestore = remember { FirebaseFirestore.getInstance() }
    val idPadre = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    // 📡 ESCUCHA EN ESPEJO
    DisposableEffect(idPadre) {
        val listener = firestore.collection("dispositivos_niños")
            .whereEqualTo("padreId", idPadre)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cargando = false
                    return@addSnapshotListener
                }

                val listaTemporal = mutableListOf<DispositivoNiño>()
                if (snapshot != null) {
                    for (dispositivo in snapshot.documents) {
                        val id = dispositivo.id
                        val nombre = dispositivo.getString("nombreDispositivo")
                            ?: dispositivo.getString("modelo")
                            ?: "Tablet/Celular Niño"
                        val activo = dispositivo.getBoolean("kioscoActivo") ?: false

                        listaTemporal.add(DispositivoNiño(id, nombre, activo))
                    }
                }

                listaDispositivos = listaTemporal
                cargando = false
            }

        onDispose {
            listener.remove()
        }
    }

    // 🚀 2. IMPLEMENTAR EL CONTENEDOR BASE
    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = stringResource(id = R.string.remote_lock_control_title),
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
        ) { paddingValues ->
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorAcabadoPrincipal)
                }
            } else if (listaDispositivos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(id = R.string.no_linked_devices_for_lock), color = colorTextoPrincipal, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.go_to_connection_menu_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorTextoSecundario,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaDispositivos) { dispositivo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                            border = if (isPremiumUser) androidx.compose.foundation.BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.2f)) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isPremiumUser) 0.dp else 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val displayName = if (dispositivo.nombreDispositivo == "Tablet/Celular Niño") {
                                        when (java.util.Locale.getDefault().language) {
                                            "en" -> "Child's Tablet/Phone"
                                            "fr" -> "Tablette/Téléphone de l'enfant"
                                            "de" -> "Tablet/Handy des Kindes"
                                            "pt" -> "Tablet/Celular da Criança"
                                            "ru" -> "Планшет/телефон ребенка"
                                            else -> "Tablet/Celular Niño"
                                        }
                                    } else {
                                        dispositivo.nombreDispositivo
                                    }
                                    Text(displayName, color = colorTextoPrincipal, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (dispositivo.kioscoActivo) stringResource(id = R.string.status_blocked_remote) else stringResource(id = R.string.status_free),
                                        color = if (dispositivo.kioscoActivo) MaterialTheme.colorScheme.error else colorAcabadoPrincipal,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        val nuevoEstado = !dispositivo.kioscoActivo
                                        BiometricHelper.cambiarEstadoKioscoConHuella(
                                            activity = activity,
                                            idDispositivoNiño = dispositivo.id,
                                            debeBloquear = nuevoEstado
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (dispositivo.kioscoActivo) colorAcabadoPrincipal else MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (dispositivo.kioscoActivo) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(id = R.string.unlock_action))
                                    } else {
                                        Icon(Icons.Default.Lock, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(id = R.string.lock_action))
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