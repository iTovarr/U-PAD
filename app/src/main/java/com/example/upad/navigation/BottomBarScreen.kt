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
        route = "parent_dashboard",
        titleResId = com.example.upad.R.string.nav_home,
        icon = Icons.Default.Dashboard
    )

    object Analytics : BottomBarScreen(
        route = "analytics",
        titleResId = com.example.upad.R.string.nav_analytics,
        icon = Icons.Default.Analytics
    )

    object Profile : BottomBarScreen(
        route = "profile",
        titleResId = com.example.upad.R.string.nav_profile,
        icon = Icons.Default.Person
    )

    object Settings : BottomBarScreen(
        route = "settings",
        titleResId = com.example.upad.R.string.nav_settings,
        icon = Icons.Default.Settings
    )

    // 🌟 Exclusivos Premium
    object Calendario : BottomBarScreen(
        route = "today_calendar",
        titleResId = com.example.upad.R.string.nav_calendar,
        icon = Icons.Default.CalendarMonth
    )

    object Emociones : BottomBarScreen(
        route = "emotions_track",
        titleResId = com.example.upad.R.string.nav_emotions,
        icon = Icons.Default.EmojiEmotions
    )

    object Notificaciones : BottomBarScreen(
        route = "notifications",
        titleResId = com.example.upad.R.string.nav_alerts,
        icon = Icons.Default.Notifications
    )
}