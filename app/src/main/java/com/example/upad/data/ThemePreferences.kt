package com.example.upad.data

import android.content.Context
import android.content.res.Configuration
import com.example.upad.ui.theme.AppPalette

/** Guarda en el dispositivo la apariencia elegida: tema oscuro y paleta de colores. */
class ThemePreferences(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Tema elegido por el usuario. Si todavía no ha elegido ninguno, se sigue el
     * tema del sistema.
     */
    fun isDarkMode(): Boolean =
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else sistemaEnOscuro()

    private fun sistemaEnOscuro(): Boolean {
        val modoNoche = appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return modoNoche == Configuration.UI_MODE_NIGHT_YES
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    /** Paleta elegida; Calma si todavía no se ha elegido ninguna. */
    fun palette(): AppPalette = AppPalette.desdeClave(prefs.getString(KEY_PALETTE, null))

    fun setPalette(palette: AppPalette) {
        prefs.edit().putString(KEY_PALETTE, palette.name).apply()
    }

    private companion object {
        // Mismo archivo y clave que borra el cierre de sesión en MainActivity.
        const val PREFS_NAME = "UPadPrefs"
        const val KEY_DARK_MODE = "pref_tema_oscuro"
        const val KEY_PALETTE = "pref_paleta"
    }
}
