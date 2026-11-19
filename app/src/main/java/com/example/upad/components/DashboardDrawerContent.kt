package com.example.upad.dashboard.components

import com.example.upad.ui.theme.extendedColors
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.upad.R

/**
 * Contenido del ModalDrawer del Dashboard.
 *
 * Se extrae del archivo principal para reducir su tamaño.
 * No contiene lógica propia: recibe todo por parámetros y
 * devuelve las acciones mediante lambdas.
 *
 * @param parentName            Nombre del padre/tutor a mostrar.
 * @param userEmail             Correo del usuario autenticado.
 * @param imageUri              URI de la foto de perfil (puede ser null).
 * @param esPremium             Si el usuario tiene plan premium.
 * @param isDarkMode            Si el tema oscuro está activo.
 * @param colorTextoPrincipal   Color principal de texto e iconos.
 * @param colorTextoSecundario  Color secundario de texto e iconos.
 * @param colorDinamicoSuscripcion Color dorado (premium) o primario (básico).
 * @param fuentePremium         Familia tipográfica del proyecto.
 * @param onChangePlan          Acción al pulsar "Cambiar Plan".
 * @param onCancelPremium       Acción al pulsar "Volver a Plan Básico".
 * @param onUbicarHijo          Acción al pulsar "Ubicar a mi Hijo".
 * @param onNavigateToAnalytics Acción para ir a Análisis.
 * @param onNavigateToDeviceManagement Acción para ir a Bloquear Dispositivo.
 * @param onNavigateToConnection Acción para ir a Conectar con el Niño.
 * @param onNavigateToProfile   Acción para ir a Mi Perfil.
 * @param onNavigateToSettings  Acción para ir a Ajustes.
 */
@Composable
fun DashboardDrawerContent(
    parentName: String,
    userEmail: String,
    imageUri: Uri?,
    esPremium: Boolean,
    isDarkMode: Boolean,
    colorTextoPrincipal: Color,
    colorTextoSecundario: Color,
    colorDinamicoSuscripcion: Color,
    fuentePremium: FontFamily,
    onChangePlan: () -> Unit,
    onCancelPremium: () -> Unit,
    onUbicarHijo: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToDeviceManagement: () -> Unit,
    onNavigateToConnection: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
    ) {
        // ── CABECERA: foto, nombre, correo ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column {
                // Avatar circular
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = stringResource(id = R.string.cd_profile_photo),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = parentName,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = userEmail,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── COLORES DE LOS ÍTEMS ─────────────────────────────────────────────
        val drawerColors = NavigationDrawerItemDefaults.colors(
            unselectedIconColor = colorTextoSecundario,
            unselectedTextColor = colorTextoPrincipal,
            selectedIconColor = colorDinamicoSuscripcion,
            selectedTextColor = colorTextoPrincipal,
            unselectedContainerColor = Color.Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
        )

        // ── ÍTEMS DEL MENÚ ───────────────────────────────────────────────────

        // Cambiar plan
        NavigationDrawerItem(
            icon = {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.extendedColors.premium)
            },
            label = {
                Text(
                    text = if (esPremium) stringResource(R.string.change_basic_plan) else stringResource(R.string.change_premium_plan),
                    fontWeight = FontWeight.Bold,
                    color = colorTextoPrincipal,
                    )
            },
            selected = false,
            onClick = { if (esPremium) onCancelPremium() else onChangePlan() },
            colors = drawerColors,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Ubicar hijo (solo Premium)
        if (esPremium) {
            NavigationDrawerItem(
                icon = {
                    Icon(Icons.Default.LocationOn, contentDescription = stringResource(id = R.string.cd_locate_child))
                },
                label = {
                    Text(
                        text = stringResource(R.string.locate_child),
                        fontWeight = FontWeight.Bold,
                        color = colorTextoPrincipal,
                        )
                },
                selected = false,
                onClick = onUbicarHijo,
                colors = drawerColors,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Análisis
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(R.string.performance_analysis),
                    fontWeight = FontWeight.Medium,
                    color = colorTextoPrincipal,
                    )
            },
            selected = false,
            onClick = onNavigateToAnalytics,
            colors = drawerColors,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Bloquear Dispositivo
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(R.string.lock_device),
                    fontWeight = FontWeight.Medium,
                    color = colorTextoPrincipal,
                    )
            },
            selected = false,
            onClick = onNavigateToDeviceManagement,
            colors = drawerColors,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Conectar con el Niño
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Link, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(R.string.connect_child),
                    fontWeight = FontWeight.Medium,
                    color = colorTextoPrincipal,
                    )
            },
            selected = false,
            onClick = onNavigateToConnection,
            colors = drawerColors,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Mi Perfil
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(R.string.my_profile),
                    fontWeight = FontWeight.Medium,
                    color = colorTextoPrincipal,
                    )
            },
            selected = false,
            onClick = onNavigateToProfile,
            colors = drawerColors,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Ajustes
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontWeight = FontWeight.Medium,
                    color = colorTextoPrincipal,
                    )
            },
            selected = false,
            onClick = onNavigateToSettings,
            colors = drawerColors,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}