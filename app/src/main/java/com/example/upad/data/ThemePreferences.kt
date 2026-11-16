package com.example.upad.data

import android.content.Context

/** Guarda en el dispositivo si el usuario eligió el tema oscuro. */
class ThemePreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    private companion object {
        // Mismo archivo y clave que ya usaban MainActivity y ProfileScreen.
        const val PREFS_NAME = "UPadPrefs"
        const val KEY_DARK_MODE = "pref_tema_oscuro"
    }
}
