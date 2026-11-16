package com.example.upad.dashboard.components

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * @param title               Texto del centro de la barra.
 * @param imageUri            Foto de perfil circular (nullable).
 * @param colorFondo          Color SÓLIDO de fondo (no transparent, o se verá el contenido).
 * @param colorIconos         Color de iconos y texto.
 * @param fuentePremium       Tipografía del proyecto.
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
    colorFondo: Color,                        // ← OBLIGATORIO, sin default transparente
    colorIconos: Color = Color.Black,
    fuentePremium: FontFamily = FontFamily.SansSerif,
    showBackButton: Boolean = true,
    onOpenMenu: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)                       // ← siempre encima de TODO el contenido
            .shadow(elevation = 4.dp)          // ← sombra sutil para separarse visualmente
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
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (showBackButton) Icons.Default.ArrowBackIosNew else Icons.Default.Menu,
                    contentDescription = if (showBackButton) "Volver" else "Abrir menú",
                    tint = colorIconos,
                    modifier = Modifier.size(if (showBackButton) 22.dp else 26.dp)
                )
            }

            // ── 2. TÍTULO ────────────────────────────────────────────────────
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = colorIconos,
                fontFamily = fuentePremium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // ── 3. AYUDA ─────────────────────────────────────────────────────
            IconButton(
                onClick = onNavigateToHelp,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Ayuda",
                    tint = colorIconos,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // ── 4. AVATAR ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorIconos.copy(alpha = 0.1f))
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Mi perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Mi perfil",
                        tint = colorIconos,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}