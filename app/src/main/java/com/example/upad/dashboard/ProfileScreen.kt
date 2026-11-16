package com.example.upad.dashboard

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.upad.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onNavigateToHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser = firebaseAuth.currentUser

    // 🌗 ESTADOS GLOBALES DE DISEÑO
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    // 🔄 DETECTAR ORIENTACIÓN DE PANTALLA
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    // 🔄 ESTADO DE SUB-PANTALLA: false = Vista Perfil / true = Editar Perfil
    var isEditingMode by remember { mutableStateOf(false) }

    // 📸 IMAGEN DE PERFIL — estado reactivo para que el TopBar también se actualice
    // FIX: usamos var mutable para que al cambiar foto se re-renderice el TopBar
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    LaunchedEffect(Unit) {
        val saved = sharedPreferences.getString("PROFILE_IMAGE_URI", null)
        imageUri = when {
            saved != null -> Uri.parse(saved)
            currentUser?.photoUrl != null -> currentUser.photoUrl
            else -> null
        }
    }

    // ✏️ ESTADOS DE CAMPOS FORMULARIO
    val initialName = remember(currentUser) {
        sharedPreferences.getString("PARENT_NAME", null)
            ?: currentUser?.displayName
            ?: currentUser?.email?.substringBefore("@")?.uppercase()
            ?: "PADRE/TUTOR"
    }
    var nameInput by remember { mutableStateOf(initialName) }
    var emailInput by remember { mutableStateOf(currentUser?.email ?: "") }

    // Lanzador de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && isEditingMode) imageUri = uri  // ← actualiza también el TopBar
    }

    // 🎨 PALETA ADAPTATIVA
    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)
    val fuentePremium = FontFamily.SansSerif

    val colorFondoSolido = if (isPremiumUser) {
        if (isDarkMode) Color(0xFF0F0C29) else Color(0xFFFBE9E7)
    } else {
        if (isDarkMode) Color(0xFF121212) else Color.White
    }

    val colorSuperficieTarjetas = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    }

    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = if (isEditingMode) stringResource(id = R.string.edit_profile_title) else stringResource(id = R.string.my_profile),
                    // FIX: pasamos imageUri reactivo — se actualiza cuando el usuario cambia foto
                    imageUri = imageUri,
                    colorFondo = colorFondoSolido,
                    colorIconos = colorTextoPrincipal,
                    fuentePremium = fuentePremium,
                    showBackButton = true,
                    onBackPressed = {
                        if (isEditingMode) isEditingMode = false else onNavigateBack()
                    },
                    // FIX: ya estamos en perfil, el avatar no navega pero sí abre modo edición
                    onNavigateToProfile = { isEditingMode = true },
                    // FIX: onNavigateToHelp conectado correctamente
                    onNavigateToHelp = onNavigateToHelp
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // ── AVATAR ────────────────────────────────────────────────────
                Box(
                    modifier = Modifier.size(if (isLandscape) 90.dp else 110.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(colorAcabadoPrincipal.copy(alpha = 0.15f))
                            .clickable(enabled = isEditingMode) { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            // FIX: ImageRequest con contexto explícito para URIs de galería (content://)
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(id = R.string.nav_profile),
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colorAcabadoPrincipal,
                                modifier = Modifier.size(if (isLandscape) 44.dp else 54.dp)
                            )
                        }
                    }

                    if (isEditingMode) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(colorAcabadoPrincipal)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = stringResource(id = R.string.change_photo),
                                tint = if (isPremiumUser && !isDarkMode) Color(0xFF111111) else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // ── MODO 1: VISTA DE PERFIL ───────────────────────────────────
                if (!isEditingMode) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = nameInput,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTextoPrincipal
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.active),
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = colorSuperficieTarjetas,
                        border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.08f)),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(colorAcabadoPrincipal.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = colorAcabadoPrincipal, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(stringResource(id = R.string.name_label), fontSize = 11.sp, color = colorTextoSecundario, fontWeight = FontWeight.Medium)
                                    Text(nameInput, fontSize = 15.sp, color = colorTextoPrincipal, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(colorAcabadoPrincipal.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = colorAcabadoPrincipal, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(stringResource(id = R.string.email_label), fontSize = 11.sp, color = colorTextoSecundario, fontWeight = FontWeight.Medium)
                                    Text(emailInput, fontSize = 15.sp, color = colorTextoPrincipal, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MenuOptionRow(
                            icon = Icons.Default.Person,
                            title = stringResource(id = R.string.edit_profile_title),
                            colorSuperficie = colorSuperficieTarjetas,
                            isPremium = isPremiumUser,
                            colorPrincipal = colorAcabadoPrincipal,
                            colorTexto = colorTextoPrincipal,
                            colorTextoSec = colorTextoSecundario,
                            onClick = { isEditingMode = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 45.dp))

                    MenuOptionRow(
                        icon = Icons.Default.ExitToApp,
                        title = stringResource(id = R.string.logout),
                        colorSuperficie = Color(0xFFFEEBEE),
                        isPremium = false,
                        colorPrincipal = Color(0xFFEF5350),
                        colorTexto = Color(0xFFC62828),
                        colorTextoSec = Color(0xFFEF5350),
                        onClick = {
                            firebaseAuth.signOut()
                            onLogoutSuccess()
                        }
                    )
                }

                // ── MODO 2: FORMULARIO DE EDICIÓN ────────────────────────────
                else {
                    Spacer(modifier = Modifier.height(28.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(stringResource(id = R.string.username_label), fontSize = 13.sp, color = colorTextoSecundario, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            TextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colorSuperficieTarjetas,
                                    unfocusedContainerColor = colorSuperficieTarjetas,
                                    disabledContainerColor = colorSuperficieTarjetas,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = colorTextoPrincipal,
                                    unfocusedTextColor = colorTextoPrincipal
                                ),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }

                        Column {
                            Text(stringResource(id = R.string.email_label), fontSize = 13.sp, color = colorTextoSecundario, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            TextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colorSuperficieTarjetas,
                                    unfocusedContainerColor = colorSuperficieTarjetas,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = colorTextoPrincipal,
                                    unfocusedTextColor = colorTextoPrincipal
                                ),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 30.dp else 60.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                nameInput = initialName
                                isEditingMode = false
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.4f))
                        ) {
                            Text(stringResource(id = R.string.cancel), color = colorTextoPrincipal, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                // 🔐 PERMISOS PERSISTENTES ANTES DE GUARDAR LA URI
                                imageUri?.let { uri ->
                                    if (uri.scheme == "content") {
                                        try {
                                            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                sharedPreferences.edit().apply {
                                    putString("PARENT_NAME", nameInput)
                                    imageUri?.let { putString("PROFILE_IMAGE_URI", it.toString()) }
                                }.apply()
                                isEditingMode = false
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.save_action),
                                color = if (isPremiumUser && !isDarkMode) Color(0xFF111111) else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── COMPONENTE LOCAL: fila de opción del menú (sin cambios) ─────────────────
@Composable
fun MenuOptionRow(
    icon: ImageVector,
    title: String,
    colorSuperficie: Color,
    isPremium: Boolean,
    colorPrincipal: Color,
    colorTexto: Color,
    colorTextoSec: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = colorSuperficie,
        border = BorderStroke(
            1.dp,
            if (isPremium) colorPrincipal.copy(alpha = 0.25f) else colorTextoSec.copy(alpha = 0.08f)
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorPrincipal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = colorTexto,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colorTextoSec.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}