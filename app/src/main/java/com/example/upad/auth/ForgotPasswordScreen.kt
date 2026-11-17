package com.example.upad.auth

import com.example.upad.ui.theme.TextoTenueClaro
import com.example.upad.ui.theme.TextoSecundarioClaro
import com.example.upad.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorFondoBase = Color(0xFFF0F4F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA CURVA U-PAD ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 32.dp)
        ) {
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.cd_back), tint = colorAzulTEA)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.forgot_overline),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextoTenueClaro,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = stringResource(id = R.string.forgot_password_question),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp),
                lineHeight = 32.sp
            )
        }

        // --- CUERPO ---
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.forgot_instructions),
                fontSize = 15.sp,
                color = TextoSecundarioClaro,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo Email Estilizado
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.forgot_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorAzulTEA,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón Enviar
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.forgot_send_link),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }

    // --- DIÁLOGO DE ÉXITO ESTILIZADO ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = colorAzulTEA,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.forgot_check_email),
                    fontWeight = FontWeight.Black,
                    color = colorAzulTEA
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.forgot_sent_message),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onBackToLogin()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
                ) {
                    Text(stringResource(id = R.string.action_understood), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}