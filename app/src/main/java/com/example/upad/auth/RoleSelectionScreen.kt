package com.example.upad.auth

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R
import com.example.upad.utils.BiometricHelper
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit
) {
    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorFondoBase = Color(0xFFF0F4F8)
    val colorMoradoTutor = Color(0xFF9575CD)

    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val usuarioFirebase = remember { FirebaseAuth.getInstance().currentUser }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 🎯 LA CLAVE: El scroll solo se activa en horizontal para que en vertical no rompa los pesos (weights)
                .then(if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- CABECERA SUPERIOR RESPONSIVE ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                    .background(Color.White)
                    .padding(
                        top = if (isLandscape) 24.dp else 60.dp, // Volvemos a tus 60.dp originales en vertical
                        bottom = if (isLandscape) 20.dp else 40.dp, // Volvemos a tus 40.dp originales en vertical
                        start = 24.dp,
                        end = 24.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BIENVENIDO A U-PAD",
                    fontSize = if (isLandscape) 24.sp else 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colorAzulTEA,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Quién usará el dispositivo hoy?",
                    fontSize = if (isLandscape) 15.sp else 18.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            // --- CUERPO DE SELECCIÓN ADAPTATIVO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Si está en vertical, le damos weight(1f) para que empuje la decoración abajo como tu diseño original
                    .then(if (!isLandscape) Modifier.weight(1f) else Modifier)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val cardModifier = Modifier.width(240.dp).aspectRatio(1.1f)
                        TutorCard(cardModifier, usuarioFirebase, activity, context, colorMoradoTutor, onRoleSelected)
                        MenorCard(cardModifier, colorAzulTEA, onRoleSelected)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp), // Tus 20.dp originales
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TutorCard(Modifier.weight(1f).aspectRatio(0.8f), usuarioFirebase, activity, context, colorMoradoTutor, onRoleSelected) // Tu aspect ratio original de 0.8f
                        MenorCard(Modifier.weight(1f).aspectRatio(0.8f), colorAzulTEA, onRoleSelected)
                    }
                }
            }

            // Espaciador decorativo dinámico solo para modo horizontal
            if (isLandscape) {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- DECORACIÓN INFERIOR ---
            if (!isLandscape) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp) // Volvemos a tus 60.dp originales
                        .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun TutorCard(
    modifier: Modifier,
    usuarioFirebase: com.google.firebase.auth.FirebaseUser?,
    activity: androidx.fragment.app.FragmentActivity?,
    context: android.content.Context,
    colorMoradoTutor: Color,
    onRoleSelected: (String) -> Unit
) {
    RoleOptionCard(
        modifier = modifier,
        title = "SOY PADRE\nO TUTOR",
        imageRes = R.drawable.tutor,
        colorTheme = colorMoradoTutor,
        onClick = {
            if (usuarioFirebase != null) {
                if (activity != null && BiometricHelper.esBiometriaDisponible(context)) {
                    BiometricHelper.lanzarLectorHuella(
                        activity = activity,
                        onSuccess = { onRoleSelected("padre_directo") },
                        onError = { error ->
                            android.util.Log.d("Biometria", "Fallo: $error")
                            onRoleSelected("padre")
                        }
                    )
                } else {
                    onRoleSelected("padre_directo")
                }
            } else {
                onRoleSelected("padre")
            }
        }
    )
}

@Composable
private fun MenorCard(
    modifier: Modifier,
    colorAzulTEA: Color,
    onRoleSelected: (String) -> Unit
) {
    RoleOptionCard(
        modifier = modifier,
        title = "SOY EL\nMENOR",
        imageRes = R.drawable.menor,
        colorTheme = colorAzulTEA,
        onClick = { onRoleSelected("menor") }
    )
}

@Composable
fun RoleOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    imageRes: Int,
    colorTheme: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnimated by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "PulsacionRolAnimation"
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = modifier
            .scale(scaleAnimated)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(32.dp), // Tus 32.dp originales
        color = Color.White,
        shadowElevation = 6.dp // Tus 6.dp originales
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Tus 16.dp originales
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)) // Tus 24.dp originales
                    .background(colorTheme.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isLandscape) 8.dp else 14.dp), // Mantiene tus 14.dp originales en vertical
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Tus 16.dp originales

            Text(
                text = title,
                fontSize = 15.sp, // Tus 15.sp originales
                fontWeight = FontWeight.Black,
                color = colorTheme,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp, // Tus 18.sp originales
                letterSpacing = 0.2.sp
            )
        }
    }
}