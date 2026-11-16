package com.example.upad.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.upad.widget.ChildSessionMonitorWidgetProvider
import com.example.upad.widget.ParentRoutineWidgetProvider
import com.example.upad.widget.PremiumProgressWidgetProvider

private const val TAG = "AppLocaleManager"

/** Aplica el idioma elegido a la app y lo propaga a los widgets de escritorio. */
class AppLocaleManager(context: Context) {

    private val appContext = context.applicationContext

    fun aplicar(idiomaCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(idiomaCode)
        if (AppCompatDelegate.getApplicationLocales() != appLocale) {
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
        actualizarWidgets(idiomaCode)
    }

    /** Los widgets no comparten la configuración de la app: leen el idioma de sus propias preferencias. */
    private fun actualizarWidgets(idiomaCode: String) {
        try {
            for (prefsName in listOf("WIDGET_PREFS", "SESSION_WIDGET_PREFS")) {
                appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    .edit().putString("app_language", idiomaCode).apply()
            }

            ParentRoutineWidgetProvider.notificarCambioDatos(appContext)
            PremiumProgressWidgetProvider.notificarCambioDatos(appContext)

            val intent = Intent(appContext, ChildSessionMonitorWidgetProvider::class.java).apply {
                action = ParentRoutineWidgetProvider.ACTION_DATA_CHANGED
            }
            appContext.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo propagar el idioma a los widgets", e)
        }
    }
}
