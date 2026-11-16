package com.example.upad.auth

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.upad.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// --- 1. LÓGICA DE AUTENTICACIÓN CON GOOGLE Y FIREBASE ---
fun iniciarSesionConGoogle(context: Context, coroutineScope: CoroutineScope, onResultado: (Boolean) -> Unit) {
    val credentialManager = CredentialManager.create(context)
    val auth = FirebaseAuth.getInstance()

    val webClientId = com.example.upad.BuildConfig.GOOGLE_CLIENT_ID

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("GoogleAuth", "Login exitoso: ${auth.currentUser?.displayName}")
                            onResultado(true)
                        } else {
                            Log.e("GoogleAuth", "Fallo al conectar con Firebase", task.exception)
                            onResultado(false)
                        }
                    }
            }
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuth", "Error del Credential Manager: ${e.message}")
            onResultado(false)
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Error desconocido: ${e.message}")
            onResultado(false)
        }
    }
}

// --- 2. INTERFAZ DE USUARIO (TU DISEÑO) ---
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginExitoso: () -> Unit // Callback para cuando Google dice "OK"
) {
    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorFondoBase = Color(0xFFF0F4F8)

    // Herramientas necesarias para lanzar la hoja flotante de Android
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA CON LOGO ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(24.dp),
                color = colorFondoBase.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.upad_icono),
                        contentDescription = "U-Pad Logo",
                        modifier = Modifier.size(90.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BIENVENIDO A U-PAD",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Tu guía de rutinas amigable",
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        // --- CUERPO DE BOTONES ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "EMPEZAR AHORA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BOTÓN DE GOOGLE ---
            OutlinedButton(
                onClick = {
                    // Ejecutamos la función de la línea 33 al hacer clic
                    iniciarSesionConGoogle(context, coroutineScope) { exitoso ->
                        if (exitoso) {
                            onLoginExitoso() // Cambia de pantalla si el login fue correcto
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, colorAzulTEA)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_login),
                        contentDescription = "Google Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "CONTINUAR CON GOOGLE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAzulTEA
                    )
                }
            }
        }

        // --- PANEL INFERIOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "¿Ya tienes una cuenta?", color = Color.Gray, fontSize = 15.sp)
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Inicia sesión",
                        color = colorAzulTEA,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}