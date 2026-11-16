package com.example.upad.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.upad.R
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SubscriptionPlansScreen(
    routineViewModel: RoutineViewModel,
    onNavigateToBenefits: () -> Unit, // 🚀 Te lleva a ChangePlanScreen (Ver beneficios)
    onDirectToProfile: () -> Unit      // 🚀 Te lleva directo a ChildProfileSetupScreen (Plan Básico)

) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorAmarilloTEA = Color(0xFFFFD54F)
    val colorFondoBase = Color(0xFFF0F4F8)

    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    var selectedPlan by remember { mutableStateOf("premium") }

    val guardarPlanBasicoEnFirebase = {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val datosPlan = hashMapOf(
                "planSuscripcion" to "basico",
                "estadoSuscripcion" to "Gratuito",
                "fechaSeleccionPlan" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("usuarios").document(uid)
                .set(datosPlan, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 30.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.choose_a_plan),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.choose_a_plan_desc),
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // --- CUERPO ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlanCard(
                title = stringResource(id = R.string.plan_basic_title),
                price = stringResource(id = R.string.free_price),
                imageRes = R.drawable.plan_basico,
                isSelected = selectedPlan == "basico",
                colorTheme = Color.Gray,
                onClick = { selectedPlan = "basico" }
            )

            Spacer(modifier = Modifier.height(20.dp))

            PlanCard(
                title = stringResource(id = R.string.plan_premium_title),
                price = stringResource(id = R.string.premium_price),
                imageRes = R.drawable.plan_premium,
                isSelected = selectedPlan == "premium",
                colorTheme = colorAmarilloTEA,
                onClick = { selectedPlan = "premium" }
            )
        }

        // --- ACCIONES INFERIORES ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (selectedPlan == "premium") {
                        onNavigateToBenefits() // Va a ver lo que incluye el plan
                    } else {
                        routineViewModel.setPremiumUser(false)
                        guardarPlanBasicoEnFirebase()
                        onDirectToProfile()    // Va directo a configurar el niño en básico
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
            ) {
                Text(stringResource(id = R.string.choose_plan_btn), fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            TextButton(
                onClick = {
                    routineViewModel.setPremiumUser(false)
                    guardarPlanBasicoEnFirebase()
                    onDirectToProfile()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(id = R.string.other_moment), color = Color.LightGray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// (Tu PlanCard se mantiene igual, omitido por brevedad)
@Composable
fun PlanCard(
    title: String,
    price: String,
    imageRes: Int,
    isSelected: Boolean,
    colorTheme: Color,
    onClick: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 12.dp else 2.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, colorAzulTEA) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) colorAzulTEA else Color.DarkGray
                )
                Text(
                    text = price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTheme
                )
                if (title.contains("PREMIUM") || title.contains(stringResource(id = R.string.plan_premium_title))) {
                    Text(
                        text = stringResource(id = R.string.unlimited_pictograms),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = colorAzulTEA)
                )
            }
        }
    }
}