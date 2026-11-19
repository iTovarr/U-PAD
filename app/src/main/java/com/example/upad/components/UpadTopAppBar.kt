package com.example.upad.dashboard.components

import androidx.compose.ui.semantics.Role
import com.example.upad.R
import androidx.compose.ui.res.stringResource
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage

/**
 * TopAppBar global — fija y opaca, igual que YouTube/Instagram.
 *
 * Siempre flota encima del contenido gracias a zIndex + fondo sólido.
 * El contenido de la pantalla debe tener padding superior = 56dp + statusBar
 * para no quedar oculto detrás de la barra.
 *
 * Dashboard  → [ ☰ ]  Título  [ ? ]  [ 👤 ]
 * Secundaria → [ ← ]  Título  [ ? ]  [ 👤 ]
 *
 * Los colores y la tipografía salen del tema activo.
 *
 * @param title               Texto del centro de la barra.
 * @param imageUri            Foto de perfil circular (nullable).
 * @param showBackButton      false = muestra ☰ (Dashboard). true = muestra ← (resto).
 * @param onOpenMenu          Callback del botón ☰.
 * @param onBackPressed       Callback del botón ←.
 * @param onNavigateToProfile Callback del avatar.
 * @param onNavigateToHelp    Callback del botón ?.
 */
@Composable
fun UpadTopAppBar(
    title: String,
    imageUri: Uri? = null,
    showBackButton: Boolean = true,
    onOpenMenu: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    val colorFondo = MaterialTheme.colorScheme.surfaceContainer
    val colorIconos = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)                       // ← siempre encima de TODO el contenido
            .background(colorFondo)            // ← fondo SÓLIDO que tapa lo que hay debajo
            .statusBarsPadding()               // ← respeta la barra de estado del teléfono
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ── 1. IZQUIERDA: ☰ o ← ────────────────────────────────────────
            IconButton(
                onClick = if (showBackButton) onBackPressed else onOpenMenu,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (showBackButton) Icons.Default.ArrowBackIosNew else Icons.Default.Menu,
                    contentDescription = stringResource(id = if (showBackButton) R.string.cd_back else R.string.open_menu),
                    tint = colorIconos,
                    modifier = Modifier.size(if (showBackButton) 22.dp else 26.dp)
                )
            }

            // ── 2. TÍTULO ────────────────────────────────────────────────────
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorIconos,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // ── 3. AYUDA ─────────────────────────────────────────────────────
            IconButton(
                onClick = onNavigateToHelp,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = stringResource(id = R.string.cd_help),
                    tint = colorIconos,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // ── 4. AVATAR ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorIconos.copy(alpha = 0.1f))
                    .clickable(role = Role.Button) { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = stringResource(id = R.string.cd_my_profile),
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(id = R.string.cd_my_profile),
                        tint = colorIconos,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}