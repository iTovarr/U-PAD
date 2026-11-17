package com.example.upad.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R
import com.example.upad.di.LocalAppContainer
import com.example.upad.utils.CredentialValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val msgCamposVacios = stringResource(id = R.string.error_fill_all_fields)
    val msgEmailInvalido = stringResource(id = R.string.error_invalid_email)
    val session = LocalAppContainer.current.sessionRepository
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorFondoBase = Color(0xFFF0F4F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(
                    top = 60.dp,
                    bottom = 40.dp,
                    start = 32.dp,
                    end = 32.dp
                )
        ) {
            Text(
                text = stringResource(id = R.string.login_overline),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray
            )
            Text(
                text = stringResource(id = R.string.login_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA
            )
            Text(
                text = stringResource(id = R.string.login_subtitle),
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.field_email_short)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = colorAzulTEA) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorAzulTEA,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.field_password)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colorAzulTEA) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(20.dp),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorAzulTEA,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                Text(
                    stringResource(id = R.string.forgot_password_question),
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    if (isLoading) return@Button

                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, msgCamposVacios, Toast.LENGTH_SHORT).show()
                    } else if (!CredentialValidator.esEmailValido(email)) {
                        Toast.makeText(context, msgEmailInvalido, Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        // La contraseña se envía tal cual: recortarla la haría distinta de la que escribió el usuario.
                        scope.launch {
                            try {
                                session.signIn(email.trim(), password)

                                // Guardamos el nombre antes de disparar la navegación
                                val prefs = context.getSharedPreferences("UPAD_PREFS", android.content.Context.MODE_PRIVATE)
                                val emailName = email.substringBefore("@").uppercase()
                                prefs.edit().putString("PARENT_NAME", emailName).apply()

                                onLoginClick(email, password)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                isLoading = false
                                Toast.makeText(context, e.message ?: "Error desconocido", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoading) Color.Gray else colorAzulTEA
                ),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.login_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        TextButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp),
            enabled = !isLoading
        ) {
            Text(
                stringResource(id = R.string.login_back_to_start),
                color = colorAzulTEA.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}