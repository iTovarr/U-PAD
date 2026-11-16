package com.example.upad.di

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Da acceso al [AppContainer] desde cualquier composable sin tener que pasarlo
 * por parámetro pantalla a pantalla. Lo provee MainActivity al crear la interfaz.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no disponible: falta el CompositionLocalProvider de MainActivity")
}
