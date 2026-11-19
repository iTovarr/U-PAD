package com.example.upad.dashboard

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.upad.R
import com.example.upad.components.SectionTitle
import com.example.upad.components.SettingRow
import com.example.upad.components.SettingSwitchRow
import com.example.upad.components.UPadCard
import com.example.upad.components.UPadCardShape
import com.example.upad.dashboard.components.UpadTopAppBar
import com.example.upad.di.LocalAppContainer
import com.example.upad.ui.theme.AppPalette
import com.example.upad.ui.theme.colorScheme
import com.example.upad.ui.theme.extendedColors
import com.example.upad.viewmodel.SettingsViewModel

/** Idiomas de la app con su nombre en ese mismo idioma. */
private val IdiomasDisponibles = listOf(
    "es" to "Español",
    "en" to "English",
    "fr" to "Français",
    "de" to "Deutsch",
    "ru" to "Русский",
    "pt" to "Português"
)

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSensoryProfile: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val session = LocalAppContainer.current.sessionRepository
    val currentUser = remember { session.currentUser }

    // Foto de perfil: la elegida en el perfil o, si no hay, la de la cuenta de Google.
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

    val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val palette by settingsViewModel.palette.collectAsState()
    val appLanguage by settingsViewModel.appLanguage.collectAsState()

    SettingsContent(
        isPremiumUser = isPremiumUser,
        isDarkMode = isDarkMode,
        palette = palette,
        appLanguage = appLanguage,
        imageUri = imageUri,
        onPaletteChange = settingsViewModel::setPalette,
        onDarkModeChange = settingsViewModel::setDarkMode,
        onLanguageChange = { codigo -> settingsViewModel.changeLanguage(session.currentUser?.uid, codigo) },
        onNavigateBack = onNavigateBack,
        onNavigateToSensoryProfile = onNavigateToSensoryProfile,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToProfile = onNavigateToProfile
    )
}

/**
 * Contenido de Ajustes sin estado: recibe las preferencias y avisa de los cambios.
 * Se puede previsualizar y capturar en tests sin ViewModel ni Firebase.
 */
@Composable
internal fun SettingsContent(
    isPremiumUser: Boolean,
    isDarkMode: Boolean,
    palette: AppPalette,
    appLanguage: String,
    imageUri: Uri?,
    onPaletteChange: (AppPalette) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSensoryProfile: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    // Aún no hay una preferencia real de notificaciones: el interruptor solo vive en esta pantalla.
    var notificationsEnabled by remember { mutableStateOf(true) }
    var expandedLanguage by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UpadTopAppBar(
                title = stringResource(R.string.settings_title),
                imageUri = imageUri,
                showBackButton = true,
                onBackPressed = onNavigateBack,
                onNavigateToHelp = onNavigateToHelp,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Suscripción ─────────────────────────────────────────────────
            SectionTitle(stringResource(R.string.settings_subscription))
            PlanCard(isPremiumUser = isPremiumUser)

            // ── Apariencia ──────────────────────────────────────────────────
            Spacer(modifier = Modifier.size(8.dp))
            SectionTitle(stringResource(R.string.settings_appearance))
            UPadCard {
                Text(
                    text = stringResource(R.string.settings_palette),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppPalette.entries.forEach { opcion ->
                        PaletteOption(
                            palette = opcion,
                            darkTheme = isDarkMode,
                            selected = opcion == palette,
                            onSelect = { onPaletteChange(opcion) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingSwitchRow(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = stringResource(R.string.settings_dark_mode_desc),
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeChange
                )
            }

            // ── Modo niño ───────────────────────────────────────────────────
            Spacer(modifier = Modifier.size(8.dp))
            SectionTitle(stringResource(R.string.settings_child_mode))
            UPadCard {
                SettingRow(
                    icon = Icons.Default.Tune,
                    title = stringResource(R.string.sensory_title),
                    subtitle = stringResource(R.string.settings_sensory_desc),
                    modifier = Modifier.clickable(role = Role.Button, onClick = onNavigateToSensoryProfile)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── General ─────────────────────────────────────────────────────
            Spacer(modifier = Modifier.size(8.dp))
            SectionTitle(stringResource(R.string.settings_general))
            UPadCard {
                SettingSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_notifications),
                    subtitle = stringResource(R.string.settings_notifications_desc),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Box {
                    SettingRow(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = IdiomasDisponibles.find { it.first == appLanguage }?.second ?: "Español",
                        modifier = Modifier.clickable(role = Role.Button) { expandedLanguage = true }
                    )
                    DropdownMenu(
                        expanded = expandedLanguage,
                        onDismissRequest = { expandedLanguage = false }
                    ) {
                        IdiomasDisponibles.forEach { (codigo, nombre) ->
                            DropdownMenuItem(
                                text = { Text(text = nombre, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    expandedLanguage = false
                                    onLanguageChange(codigo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Plan actual del tutor. El premium se distingue solo con el acento dorado. */
@Composable
private fun PlanCard(isPremiumUser: Boolean) {
    val dorado = MaterialTheme.extendedColors.premium
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = UPadCardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (isPremiumUser) BorderStroke(1.dp, dorado) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPremiumUser) Icons.Default.WorkspacePremium else Icons.Default.Star,
                contentDescription = null,
                tint = if (isPremiumUser) dorado else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(if (isPremiumUser) R.string.plan_gold else R.string.plan_basic),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(if (isPremiumUser) R.string.gold_desc else R.string.basic_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isPremiumUser) {
                Surface(
                    color = MaterialTheme.extendedColors.premiumContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.active),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.extendedColors.onPremiumContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Opción de paleta: muestra sus colores reales (en el modo claro u oscuro actual)
 * para que el tutor vea cómo quedará la app antes de elegirla.
 */
@Composable
private fun PaletteOption(
    palette: AppPalette,
    darkTheme: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esquema = palette.colorScheme(darkTheme)
    val (nombre, descripcion) = when (palette) {
        AppPalette.CALMA -> R.string.palette_calma to R.string.palette_calma_desc
        AppPalette.TIERRA -> R.string.palette_tierra to R.string.palette_tierra_desc
    }
    val borde = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val forma = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .clip(forma)
            .border(if (selected) 2.dp else 1.dp, borde, forma)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .background(esquema.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(esquema.primary, esquema.secondary, esquema.tertiary, esquema.surfaceVariant).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(nombre),
                style = MaterialTheme.typography.titleSmall,
                color = esquema.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.palette_selected),
                    tint = esquema.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = stringResource(descripcion),
            style = MaterialTheme.typography.bodySmall,
            color = esquema.onSurfaceVariant
        )
    }
}
