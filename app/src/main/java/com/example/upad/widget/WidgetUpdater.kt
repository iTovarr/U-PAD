package com.example.upad.widget

import android.content.Context

/** Guarda el progreso que leen los widgets del tutor y les pide que se redibujen. */
class WidgetUpdater(context: Context) {

    private val appContext = context.applicationContext

    fun publicar(progreso: WidgetProgress) {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putInt("PROGRESO_MANANA", progreso.porcentajeManana)
            putInt("PROGRESO_TARDE", progreso.porcentajeTarde)
            putInt("PROGRESO_NOCHE", progreso.porcentajeNoche)

            // Estadísticas del widget Premium
            putInt("TOTAL_TAREAS_DIA", progreso.totalTareas)
            putInt("COMPLETADAS_TAREAS_DIA", progreso.completadas)
            putInt("PENDIENTES_TAREAS_DIA", progreso.pendientes)
            putInt("PORCENTAJE_GLOBAL", progreso.porcentajeGlobal)
            putBoolean("IS_PREMIUM", progreso.isPremium)

            putLong("ULTIMO_FETCH", System.currentTimeMillis())
            apply()
        }
        ParentRoutineWidgetProvider.notificarCambioDatos(appContext)
        PremiumProgressWidgetProvider.notificarCambioDatos(appContext)
    }

    private companion object {
        const val PREFS_NAME = "WIDGET_PREFS"
    }
}
