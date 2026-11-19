package com.example.upad.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.upad.navigation.BottomBarScreen
import com.example.upad.viewmodel.SettingsViewModel

@Composable
fun UPADBottomBar(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    val isPremiumUser by settingsViewModel.isUserPremium.collectAsState(initial = false)

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

    // Comprobar si la pantalla actual pertenece a la lista activa para decidir si renderizar la barra
    val showBottomBar = activeScreens.any { it.route == currentDestination?.route }

    if (showBottomBar) {
        // Colores de Material 3 a partir de la paleta activa: la pestaña elegida se
        // marca con el contenedor secundario y su texto queda en negrita.
        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            activeScreens.forEach { screen ->
                val isSelected = currentDestination?.route == screen.route

                NavigationBarItem(
                    label = {
                        Text(
                            text = stringResource(id = screen.titleResId),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                    }
                )
            }
        }
    }
}