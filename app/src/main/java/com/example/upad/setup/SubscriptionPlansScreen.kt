package com.example.upad.setup

import com.example.upad.ui.theme.extendedColors
import androidx.compose.ui.semantics.Role
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
import com.example.upad.di.LocalAppContainer
import com.example.upad.R
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.viewmodel.SettingsViewModel

@Composable
fun SubscriptionPlansScreen(
    routineViewModel: RoutineViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToBenefits: () -> Unit, // 🚀 Te lleva a ChangePlanScreen (Ver beneficios)
    onDirectToProfile: () -> Unit      // 🚀 Te lleva directo a ChildProfileSetupScreen (Plan Básico)

) {
    val colorAzulTEA = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background

    val container = LocalAppContainer.current
    val session = container.sessionRepository
    val parentProfileRepository = container.parentProfileRepository
    var selectedPlan by remember { mutableStateOf("premium") }

    val guardarPlanBasicoEnFirebase = {
        val uid = session.currentUser?.uid
        if (uid != null) {
            parentProfileRepository.saveSubscriptionPlan(uid, plan = "basico", estado = "Gratuito")
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
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(top = 60.dp, bottom = 30.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.choose_a_plan),
                color = colorAzulTEA,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.choose_a_plan_desc),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
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
                colorTheme = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { selectedPlan = "basico" }
            )

            Spacer(modifier = Modifier.height(20.dp))

            PlanCard(
                title = stringResource(id = R.string.plan_premium_title),
                price = stringResource(id = R.string.premium_price),
                imageRes = R.drawable.plan_premium,
                isSelected = selectedPlan == "premium",
                colorTheme = MaterialTheme.extendedColors.premium,
                onClick = { selectedPlan = "premium" }
            )
        }

        // --- ACCIONES INFERIORES ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (selectedPlan == "premium") {
                        onNavigateToBenefits() // Va a ver lo que incluye el plan
                    } else {
                        settingsViewModel.setPremiumUser(false)
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
                Text(stringResource(id = R.string.choose_plan_btn), style = MaterialTheme.typography.titleLarge)
            }

            TextButton(
                onClick = {
                    settingsViewModel.setPremiumUser(false)
                    guardarPlanBasicoEnFirebase()
                    onDirectToProfile()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(id = R.string.other_moment), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
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
    val colorAzulTEA = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(role = Role.Button) { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) colorAzulTEA else MaterialTheme.colorScheme.outlineVariant
        )
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
                    color = if (isSelected) colorAzulTEA else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = price,
                    color = colorTheme,
                    style = MaterialTheme.typography.titleSmall
                )
                if (title.contains("PREMIUM") || title.contains(stringResource(id = R.string.plan_premium_title))) {
                    Text(
                        text = stringResource(id = R.string.unlimited_pictograms),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
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