package com.example.upad.premium

import com.example.upad.ui.theme.extendedColors
import com.example.upad.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.di.LocalAppContainer
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PaymentViewScreen(
    routineViewModel: RoutineViewModel,
    settingsViewModel: SettingsViewModel,
    onPaymentConfirmed: () -> Unit // 👶 Callback definitivo que te lleva a ChildProfileSetupScreen
) {
    val scope = rememberCoroutineScope()
    val container = LocalAppContainer.current
    val session = container.sessionRepository
    val parentProfileRepository = container.parentProfileRepository

    var procesandoPago by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!procesandoPago) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.extendedColors.premium,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.payment_title),
color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.payment_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(stringResource(id = R.string.payment_card), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
                        Text("•••• •••• •••• 4321", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    scope.launch {
                        procesandoPago = true
                        delay(2500) // Simulación elegante de verificación bancaria

                        // 1. 🌟 Actualizamos la base de datos de Firebase con la suscripción Premium Activa
                        val uid = session.currentUser?.uid
                        if (uid != null) {
                            parentProfileRepository.saveSubscriptionPlan(uid, plan = "premium", estado = "Activo")
                        }

                        // 2. 🔥 Cambiamos el estado reactivo global en el ViewModel a Premium
                        settingsViewModel.setPremiumUser(true)

                        // 3. 👶 Saltamos a la pantalla de Perfil del niño
                        onPaymentConfirmed()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.extendedColors.premiumContainer,
                    contentColor = MaterialTheme.extendedColors.onPremiumContainer
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(stringResource(id = R.string.payment_confirm_simulated), style = MaterialTheme.typography.titleSmall)
            }
        } else {
            CircularProgressIndicator(color = MaterialTheme.extendedColors.premium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(id = R.string.payment_processing), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(id = R.string.payment_setting_up), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}