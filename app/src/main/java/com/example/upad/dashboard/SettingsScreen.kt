package com.example.upad.dashboard

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R
import com.example.upad.components.UPADBackgroundWrapper
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }

    // ✅ FIX 1: Cargar imageUri igual que en ProfileScreen — reactivo y con LaunchedEffect
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

    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()
    val appLanguage by routineViewModel.appLanguage.collectAsState()

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
        if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF7F7F7)
    }

    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    var notificationsEnabled by remember { mutableStateOf(true) }
    var expandedLanguage by remember { mutableStateOf(false) }

    val idiomasDisponibles = remember {
        listOf(
            "es" to "Español",
            "en" to "English",
            "fr" to "Français",
            "de" to "Deutsch",
            "ru" to "Русский",
            "pt" to "Português"
        )
    }

    val tituloAjustado = stringResource(R.string.settings_title)

    UPADBackgroundWrapper(isPremium = isPremiumUser, isDarkMode = isDarkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                UpadTopAppBar(
                    title = tituloAjustado,
                    // ✅ FIX 1: ahora sí pasa la foto cargada desde SharedPreferences/Firebase
                    imageUri = imageUri,
                    colorFondo = colorFondoSolido,
                    colorIconos = colorTextoPrincipal,
                    fuentePremium = fuentePremium,
                    showBackButton = true,
                    onBackPressed = onNavigateBack,
                    // ✅ FIX 2: onNavigateToHelp conectado (vendrá de MainActivity)
                    onNavigateToHelp = onNavigateToHelp,
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = stringResource(R.string.settings_subscription),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTextoSecundario
                )

                // Membresía / Plan Actual
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = colorSuperficieTarjetas,
                    border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPremiumUser) Icons.Default.WorkspacePremium else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isPremiumUser) Color(0xFFFFD700) else colorAcabadoPrincipal,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isPremiumUser) stringResource(R.string.plan_gold) else stringResource(R.string.plan_basic),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colorTextoPrincipal
                                )
                                Text(
                                    text = if (isPremiumUser) stringResource(R.string.gold_desc) else stringResource(R.string.basic_desc),
                                    fontSize = 12.sp,
                                    color = colorTextoSecundario
                                )
                            }
                        }

                        if (isPremiumUser) {
                            Surface(
                                color = Color(0xFFFFD700),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.active),
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.settings_general),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTextoSecundario
                )

                // Notificaciones y Modo Oscuro
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = colorSuperficieTarjetas,
                    border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SettingSwitchRow(
                            icon = Icons.Default.Notifications,
                            title = stringResource(R.string.settings_notifications),
                            subtitle = stringResource(R.string.settings_notifications_desc),
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            switchColor = colorAcabadoPrincipal,
                            textColor = colorTextoPrincipal,
                            subTextColor = colorTextoSecundario
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = colorTextoPrincipal.copy(alpha = 0.1f)
                        )

                        SettingSwitchRow(
                            icon = Icons.Default.DarkMode,
                            title = if (isPremiumUser) "Tema Premium Visual" else stringResource(R.string.settings_dark_mode),
                            subtitle = if (isPremiumUser) "Cambiar entre Rosa Pastel y Azul Profundo" else stringResource(R.string.settings_dark_mode_desc),
                            checked = isDarkMode,
                            onCheckedChange = { routineViewModel.setDarkMode(it) },
                            switchColor = colorAcabadoPrincipal,
                            textColor = colorTextoPrincipal,
                            subTextColor = colorTextoSecundario
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.settings_app),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTextoSecundario
                )

                // Idioma
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedLanguage = true },
                        shape = RoundedCornerShape(20.dp),
                        color = colorSuperficieTarjetas,
                        border = if (isPremiumUser) BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.25f)) else null,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = colorAcabadoPrincipal)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_language),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorTextoPrincipal
                                    )
                                    val nombreIdiomaActual = idiomasDisponibles.find { it.first == appLanguage }?.second ?: "Español"
                                    Text(text = nombreIdiomaActual, fontSize = 12.sp, color = colorTextoSecundario)
                                }
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = expandedLanguage,
                        onDismissRequest = { expandedLanguage = false },
                        modifier = Modifier.background(colorSuperficieTarjetas)
                    ) {
                        idiomasDisponibles.forEach { (codigo, nombre) ->
                            DropdownMenuItem(
                                text = { Text(text = nombre, color = colorTextoPrincipal) },
                                onClick = {
                                    expandedLanguage = false
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    routineViewModel.changeLanguage(userId, codigo)
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
fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = switchColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(subtitle, fontSize = 12.sp, color = subTextColor)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = switchColor
            )
        )
    }
}