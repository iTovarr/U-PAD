package com.example.upad.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    // 🏠 Comunes / Básicos
    object Dashboard : BottomBarScreen(
        route = Routes.PARENT_DASHBOARD,
        titleResId = com.example.upad.R.string.nav_home,
        icon = Icons.Default.Dashboard
    )

    object Analytics : BottomBarScreen(
        route = Routes.ANALYTICS,
        titleResId = com.example.upad.R.string.nav_analytics,
        icon = Icons.Default.Analytics
    )

    object Profile : BottomBarScreen(
        route = Routes.PROFILE,
        titleResId = com.example.upad.R.string.nav_profile,
        icon = Icons.Default.Person
    )

    object Settings : BottomBarScreen(
        route = Routes.SETTINGS,
        titleResId = com.example.upad.R.string.nav_settings,
        icon = Icons.Default.Settings
    )

    // 🌟 Exclusivos Premium
    object Calendario : BottomBarScreen(
        route = Routes.TODAY_CALENDAR,
        titleResId = com.example.upad.R.string.nav_calendar,
        icon = Icons.Default.CalendarMonth
    )

    object Emociones : BottomBarScreen(
        route = Routes.EMOTIONS_TRACK,
        titleResId = com.example.upad.R.string.nav_emotions,
        icon = Icons.Default.EmojiEmotions
    )

    object Notificaciones : BottomBarScreen(
        route = Routes.NOTIFICATIONS,
        titleResId = com.example.upad.R.string.nav_alerts,
        icon = Icons.Default.Notifications
    )
}