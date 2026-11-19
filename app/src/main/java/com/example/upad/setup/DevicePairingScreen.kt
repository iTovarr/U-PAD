package com.example.upad.setup

import com.example.upad.ui.theme.extendedColors
import com.example.upad.R
import androidx.compose.ui.res.stringResource
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
import com.example.upad.di.LocalAppContainer
import com.example.upad.data.PairingResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePairingScreen(
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val prefs = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }
 
    // 🎨 DETECCIÓN DE TEMA REMOTO/LOCAL (Por defecto es falso -> Modo Claro)
 
    // Extraemos variables guardadas en los pasos previos del Setup
    val childName = remember { prefs.getString("CHILD_NAME", "tu pequeño") ?: "tu pequeño" }
 
    // Extraemos el UID real del Padre logueado en Firebase Auth
    val session = LocalAppContainer.current.sessionRepository
    val currentParentId = remember {
        session.currentUserId
    }
 
    // --- ESTADOS LÓGICOS DE CONEXIÓN ---
    var pairingCode by remember { mutableStateOf("") }
    var isPaired by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    val msgErrorRed = stringResource(id = R.string.network_error_linking)
    val msgCodigoInvalido = stringResource(id = R.string.code_invalid_expired)
    val msgIngresa6Numeros = stringResource(id = R.string.enter_6_numbers)
    var estaConectando by remember { mutableStateOf(false) }
 
    val deviceRepository = LocalAppContainer.current.deviceRepository
    val scope = rememberCoroutineScope()
    val pantallaPequeña = configuration.screenHeightDp < 650

    // --- PALETA DE COLORES ADAPTATIVA MULTI-TEMA ---
    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorVerdeExito = MaterialTheme.extendedColors.success

    // Asignación según la personalización previa del usuario
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficie = MaterialTheme.colorScheme.surfaceContainerLowest
    val colorTextoPrincipal = MaterialTheme.colorScheme.onSurface
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurfaceVariant
    val colorBordeInput = MaterialTheme.colorScheme.outline

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
                text = stringResource(id = R.string.pairing_overline),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = if (!isPaired) "Enlaza tu cuenta" else "¡Sincronizado con éxito!",
                color = if (!isPaired) colorTextoPrincipal else colorVerdeExito,
                style = MaterialTheme.typography.headlineMedium
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
                        color = colorTextoSecundario,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
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
                        label = { Text(stringResource(id = R.string.pairing_code_label), color = colorTextoSecundario) },
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
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
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
                        text = stringResource(id = R.string.pairing_done),
                        color = colorTextoPrincipal,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Firebase ha enlazado las cuentas con éxito. Las rutinas creadas se sincronizarán inmediatamente con la tablet de $childName.",
                        color = colorTextoSecundario,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
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

                            scope.launch {
                                try {
                                    when (deviceRepository.redeemPairingCode(pairingCode, currentParentId)) {
                                        PairingResult.VINCULADO -> isPaired = true
                                        PairingResult.CODIGO_INVALIDO -> mensajeError = msgCodigoInvalido
                                    }
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    mensajeError = msgErrorRed
                                } finally {
                                    estaConectando = false
                                }
                            }
                        } else {
                            mensajeError = msgIngresa6Numeros
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
containerColor = colorAzulTEA,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (estaConectando) {
                        "VERIFICANDO..."
                    } else if (!isPaired) {
                        "VINCULAR DISPOSITIVO 🔗"
                    } else {
                        "CONTINUAR AL DASHBOARD 🚀"
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall
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
                        text = stringResource(id = R.string.pairing_skip),
                        color = colorTextoSecundario,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}