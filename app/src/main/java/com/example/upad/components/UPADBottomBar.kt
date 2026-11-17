package com.example.upad.components

import com.example.upad.ui.theme.DarkSurface
import com.example.upad.ui.theme.UPadColors
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
import com.example.upad.viewmodel.SettingsViewModel

@Composable
fun UPADBottomBar(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

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

    val colorAcabadoPrincipal = UPadColors.acabadoPrincipal(isPremiumUser)
    val colorTextoPrincipal = UPadColors.textoPrincipal(isDarkMode)
    val colorTextoSecundario = UPadColors.textoSecundario(isDarkMode)

    val colorFondoBarra = if (isPremiumUser) {
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.80f)
    } else {
        if (isDarkMode) DarkSurface else Color.White
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