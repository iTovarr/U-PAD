package com.example.upad.setup

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePairingScreen(
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val prefs = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
 
    // 🎨 DETECCIÓN DE TEMA REMOTO/LOCAL (Por defecto es falso -> Modo Claro)
    val esTemaOscuro = remember { prefs.getBoolean("TEMA_OSCURO", false) }
 
    // Extraemos variables guardadas en los pasos previos del Setup
    val childName = remember { prefs.getString("CHILD_NAME", "tu pequeño") ?: "tu pequeño" }
 
    // Extraemos el UID real del Padre logueado en Firebase Auth
    val currentParentId = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
 
    // --- ESTADOS LÓGICOS DE CONEXIÓN ---
    var pairingCode by remember { mutableStateOf("") }
    var isPaired by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var estaConectando by remember { mutableStateOf(false) }
 
    val firestore = remember { FirebaseFirestore.getInstance() }
    val pantallaPequeña = configuration.screenHeightDp < 650

    // --- PALETA DE COLORES ADAPTATIVA MULTI-TEMA ---
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorVerdeExito = Color(0xFF4CAF50)

    // Asignación según la personalización previa del usuario
    val colorFondoBase = if (esTemaOscuro) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val colorSuperficie = if (esTemaOscuro) Color(0xFF1E293B) else Color.White
    val colorTextoPrincipal = if (esTemaOscuro) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val colorTextoSecundario = if (esTemaOscuro) Color(0xFF94A3B8) else Color(0xFF64748B)
    val colorBordeInput = if (esTemaOscuro) Color(0xFF334155) else Color(0xFFCBD5E1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ADAPTATIVA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(colorSuperficie)
                .padding(
                    top = if (pantallaPequeña) 32.dp else 50.dp,
                    bottom = if (pantallaPequeña) 16.dp else 24.dp,
                    start = 24.dp,
                    end = 24.dp
                )
        ) {
            Text(
                text = "SINCRONIZACIÓN REALTIME",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                letterSpacing = 1.5.sp
            )
            Text(
                text = if (!isPaired) "Enlaza tu cuenta" else "¡Sincronizado con éxito!",
                fontSize = if (pantallaPequeña) 24.sp else 28.sp,
                fontWeight = FontWeight.Black,
                color = if (!isPaired) colorTextoPrincipal else colorVerdeExito
            )
        }

        // --- CUERPO RESPONSIVO ADAPTATIVO ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (!isPaired) {
                // PANTALLA 1: FORMULARIO DE ENTRADA
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(if (pantallaPequeña) 90.dp else 110.dp),
                        shape = CircleShape,
                        color = colorAzulTEA.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (estaConectando) {
                                CircularProgressIndicator(color = colorAzulTEA)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PhonelinkSetup,
                                    contentDescription = null,
                                    tint = colorAzulTEA,
                                    modifier = Modifier.size(if (pantallaPequeña) 40.dp else 50.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Digita el código de vinculación de 6 dígitos activo en el dispositivo de $childName.",
                        fontSize = 15.sp,
                        color = colorTextoSecundario,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    OutlinedTextField(
                        value = pairingCode,
                        onValueChange = { input ->
                            if (input.length <= 6) {
                                pairingCode = input
                                mensajeError = ""
                            }
                        },
                        label = { Text("Código de vinculación", color = colorTextoSecundario) },
                        placeholder = { Text("000000", color = colorTextoSecundario.copy(alpha = 0.5f)) },
                        enabled = !estaConectando,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(68.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = mensajeError.isNotEmpty(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAzulTEA,
                            unfocusedBorderColor = colorBordeInput,
                            focusedContainerColor = colorSuperficie,
                            unfocusedContainerColor = colorSuperficie,
                            focusedTextColor = colorTextoPrincipal,
                            unfocusedTextColor = colorTextoPrincipal
                        )
                    )

                    if (mensajeError.isNotEmpty()) {
                        Text(
                            text = mensajeError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                // PANTALLA 2: CONTROL DE ÉXITO CONFIRMADO
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(if (pantallaPequeña) 100.dp else 130.dp),
                        shape = CircleShape,
                        color = colorVerdeExito.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colorVerdeExito,
                                modifier = Modifier.size(if (pantallaPequeña) 50.dp else 65.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "¡Enlace Completado!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = colorTextoPrincipal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Firebase ha enlazado las cuentas con éxito. Las rutinas creadas se sincronizarán inmediatamente con la tablet de $childName.",
                        fontSize = 15.sp,
                        color = colorTextoSecundario,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // --- SECCIÓN INFERIOR DE ACCIONES ADAPTATIVA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorSuperficie)
                .padding(horizontal = 24.dp, vertical = if (pantallaPequeña) 12.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!isPaired) {
                        if (pairingCode.length == 6) {
                            mensajeError = ""
                            estaConectando = true

                            firestore.collection("codigos_vinculacion").document(pairingCode)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.exists()) {
                                        val actualizaciones = mapOf(
                                            "estado" to "enlazado",
                                            "padreId" to currentParentId
                                        )

                                        firestore.collection("codigos_vinculacion").document(pairingCode)
                                            .update(actualizaciones)
                                            .addOnSuccessListener {
                                                estaConectando = false
                                                isPaired = true
                                            }
                                            .addOnFailureListener { error ->
                                                estaConectando = false
                                                mensajeError = "Error al vincular: ${error.message}"
                                            }
                                    } else {
                                        estaConectando = false
                                        mensajeError = "El código no existe o ha expirado."
                                    }
                                }
                                .addOnFailureListener { error ->
                                    estaConectando = false
                                    mensajeError = "Error de red: ${error.message}"
                                }
                        } else {
                            mensajeError = "Introduce los 6 números obligatorios."
                        }
                    } else {
                        onNavigateToDashboard()
                    }
                },
                enabled = !estaConectando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (pantallaPequeña) 54.dp else 60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isPaired) colorAzulTEA else colorVerdeExito,
                    disabledContainerColor = Color.Gray
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (estaConectando) {
                        "VERIFICANDO..."
                    } else if (!isPaired) {
                        "VINCULAR DISPOSITIVO 🔗"
                    } else {
                        "CONTINUAR AL DASHBOARD 🚀"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (esTemaOscuro && !isPaired) Color.Black else Color.White
                )
            }

            if (!isPaired) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onNavigateToDashboard,
                    enabled = !estaConectando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Configurar más tarde y omitir",
                        color = colorTextoSecundario,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}