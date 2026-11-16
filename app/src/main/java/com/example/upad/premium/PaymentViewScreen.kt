package com.example.upad.premium

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
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PaymentViewScreen(
    routineViewModel: RoutineViewModel,
    onPaymentConfirmed: () -> Unit // 👶 Callback definitivo que te lleva a ChildProfileSetupScreen
) {
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var procesandoPago by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!procesandoPago) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Desbloquea UPad Gold",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estás a un paso de automatizar las rutinas de tus hijos usando modelos de Inteligencia Artificial.",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Tarjeta de Crédito / Débito", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("•••• •••• •••• 4321", color = Color.Gray, fontSize = 12.sp)
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
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            val datosPremium = hashMapOf(
                                "planSuscripcion" to "premium",
                                "estadoSuscripcion" to "Activo",
                                "fechaSeleccionPlan" to com.google.firebase.Timestamp.now()
                            )
                            firestore.collection("usuarios").document(uid)
                                .set(datosPremium, com.google.firebase.firestore.SetOptions.merge())
                        }

                        // 2. 🔥 Cambiamos el estado reactivo global en el ViewModel a Premium
                        routineViewModel.setPremiumUser(true)

                        // 3. 👶 Saltamos a la pantalla de Perfil del niño
                        onPaymentConfirmed()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Confirmar Pago Simulado", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            CircularProgressIndicator(color = Color(0xFFD4AF37))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Procesando pago seguro...", color = Color.White, fontWeight = FontWeight.Medium)
            Text("Configurando entorno Premium...", color = Color.Gray, fontSize = 12.sp)
        }
    }
}