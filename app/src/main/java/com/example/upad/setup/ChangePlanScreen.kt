package com.example.upad.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import com.example.upad.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePlanScreen(
    onBackClick: () -> Unit,
    onNavigateToPayment: () -> Unit // 💳 Te lleva a la pasarela PaymentViewScreen
) {
    val scope = rememberCoroutineScope()

    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurface

    val colorOroBanner = Color(0xFFC5A059)
    val colorVerdeExito = Color(0xFF4CAF50)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.plan_subscription_title), fontWeight = FontWeight.Black, fontSize = 20.sp, color = colorTextoPrincipal) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back), tint = colorAcabadoPrincipal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorSuperficieTarjetas)
            )
        },
        containerColor = colorFondoBase
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(colorOroBanner)
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.most_recommended_plan),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 0.dp, topEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.plan_premium_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = colorAcabadoPrincipal,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$9.99",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = colorTextoPrincipal
                        )
                        Text(
                            text = stringResource(id = R.string.per_month),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorTextoSecundario,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.premium_desc),
                        fontSize = 13.sp,
                        color = colorTextoSecundario,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = colorFondoBase, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    val beneficiosPremium = listOf(
                        R.string.benefit_1,
                        R.string.benefit_2,
                        R.string.benefit_3,
                        R.string.benefit_4,
                        R.string.benefit_5,
                        R.string.benefit_6
                    )

                    beneficiosPremium.forEach { beneficio ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colorVerdeExito,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = beneficio),
                                fontSize = 14.sp,
                                color = colorTextoPrincipal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas.copy(alpha = 0.8f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(id = R.string.renewal_desc),
                        fontSize = 12.sp,
                        color = colorTextoSecundario,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

             Text(
                text = stringResource(id = R.string.secure_payment_methods),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = colorTextoSecundario,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { scope.launch { onNavigateToPayment() } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal)
            ) {
                Text(stringResource(id = R.string.pay_with_credit_card), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { scope.launch { onNavigateToPayment() } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorTextoPrincipal),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, colorTextoSecundario.copy(alpha = 0.4f))
            ) {
                Text(stringResource(id = R.string.pay_with_google), fontSize = 15.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}