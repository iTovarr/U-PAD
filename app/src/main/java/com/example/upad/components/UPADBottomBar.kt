package com.example.upad.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.stringResource
import com.example.upad.navigation.BottomBarScreen
import com.example.upad.viewmodel.RoutineViewModel

@Composable
fun UPADBottomBar(
    navController: NavHostController,
    routineViewModel: RoutineViewModel
) {
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 📋 Definimos los dos flujos de navegación separados
    val basicScreens = listOf(
        BottomBarScreen.Dashboard,
        BottomBarScreen.Analytics,
        BottomBarScreen.Profile,
        BottomBarScreen.Settings
    )

    val premiumScreens = listOf(
        BottomBarScreen.Dashboard,
        BottomBarScreen.Calendario,
        BottomBarScreen.Emociones,
        BottomBarScreen.Notificaciones,
        BottomBarScreen.Profile
    )

    // 🎯 Elegimos de manera reactiva qué lista usar
    val activeScreens = if (isPremiumUser) premiumScreens else basicScreens

    val colorAcabadoPrincipal = if (isPremiumUser) Color(0xFFC5A059) else MaterialTheme.colorScheme.primary
    val colorTextoPrincipal = if (isDarkMode) Color.White else Color(0xFF111111)
    val colorTextoSecundario = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF555555)

    val colorFondoBarra = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.80f)
    } else {
        if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    }

    // Comprobar si la pantalla actual pertenece a la lista activa para decidir si renderizar la barra
    val showBottomBar = activeScreens.any { it.route == currentDestination?.route }

    if (showBottomBar) {
        NavigationBar(
            containerColor = colorFondoBarra,
            tonalElevation = if (isPremiumUser) 0.dp else NavigationBarDefaults.Elevation
        ) {
            activeScreens.forEach { screen ->
                val isSelected = currentDestination?.route == screen.route

                NavigationBarItem(
                    label = {
                        Text(
                            text = stringResource(id = screen.titleResId),
                            color = if (isSelected) colorTextoPrincipal else colorTextoSecundario
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(id = screen.titleResId)
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isPremiumUser && !isDarkMode) Color.Black else Color.White,
                        selectedTextColor = colorTextoPrincipal,
                        unselectedIconColor = colorTextoSecundario,
                        unselectedTextColor = colorTextoSecundario,
                        indicatorColor = colorAcabadoPrincipal
                    )
                )
            }
        }
    }
}