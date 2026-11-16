package com.example.upad.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import com.example.upad.R
import com.example.upad.utils.RoutineProgressCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ChildSessionMonitorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        // Ahora solo escuchamos los eventos de actualizar (Refresh manual o cambio de datos)
        if (action == ACTION_REFRESH || action == ParentRoutineWidgetProvider.ACTION_DATA_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ChildSessionMonitorWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                actualizarWidget(context, appWidgetManager, appWidgetId, forzarActualizacion = (action == ACTION_REFRESH))
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.upad.widget.session.ACTION_REFRESH"

        private fun actualizarWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            forzarActualizacion: Boolean = false
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_child_session_monitor)
            val prefs = context.getSharedPreferences("SESSION_WIDGET_PREFS", Context.MODE_PRIVATE)

            // Configurar botón de refresco
            val refreshIntent = Intent(context, ChildSessionMonitorWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_refresh_session, refreshPendingIntent)

            // Leer de caché rápida
            val localizedContext = obtenerContextoLocalizado(context)

            // Configurar textos estáticos dinámicamente
            views.setTextViewText(R.id.tv_widget_title, localizedContext.getString(R.string.widget_progreso))
            views.setTextViewText(R.id.tv_registro_emocional_title, localizedContext.getString(R.string.widget_registro_emocional))

            // Leer de caché rápida
            val cachedActividad = prefs.getString("ACTUAL_ACTIVIDAD", "Buscando actividad...") ?: "Buscando actividad..."
            val cachedMinutos = prefs.getInt("ACTUAL_MINUTOS", 0)
            val cachedEstado = prefs.getString("ACTUAL_ESTADO", "") ?: ""
            val cachedCompletada = prefs.getBoolean("RUTINA_COMPLETADA", false)

            val cachedActividadLocalized = when (cachedActividad) {
                "Buscando actividad..." -> localizedContext.getString(R.string.widget_buscando)
                "Libre" -> localizedContext.getString(R.string.widget_free_time)
                else -> cachedActividad
            }
            views.setTextViewText(R.id.tv_actividad_actual, cachedActividadLocalized)

            if (cachedCompletada) {
                val emocionTexto = when (cachedEstado.lowercase().trim()) {
                    "feliz" -> localizedContext.getString(R.string.widget_feeling_happy)
                    "neutral" -> localizedContext.getString(R.string.widget_feeling_neutral)
                    "triste" -> localizedContext.getString(R.string.widget_feeling_sad)
                    else -> localizedContext.getString(R.string.widget_feeling_unregistered)
                }
                views.setTextViewText(R.id.tv_tiempo_restante, localizedContext.getString(R.string.widget_routine_completed_feedback, emocionTexto))
            } else {
                if (cachedActividad == "Libre" || cachedActividad == "Buscando actividad...") {
                    views.setTextViewText(R.id.tv_tiempo_restante, "---")
                } else {
                    views.setTextViewText(R.id.tv_tiempo_restante, localizedContext.getString(R.string.widget_next_activity_minutes, cachedMinutos))
                }
            }

            // Actualizar vista de los emojis con la caché
            configurarVisualizacionDeEmocion(context, views, cachedEstado)
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Consultar a Firestore si es forzado o si pasaron 5 mins
            val ultimoFetch = prefs.getLong("ULTIMO_FETCH_SESSION", 0)
            val tiempoActual = System.currentTimeMillis()

            if (forzarActualizacion || (tiempoActual - ultimoFetch > 5 * 60 * 1000)) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val currentAuthId = FirebaseAuth.getInstance().currentUser?.uid
                        val userId = if (!currentAuthId.isNullOrEmpty()) {
                            currentAuthId
                        } else {
                            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                            db.collection("dispositivos_niños").document(deviceId).get().await().getString("padreId")
                        }

                        if (userId != null && userId.isNotEmpty()) {
                            // Determinar turno actual basado en la hora local
                            val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            val turnoActual = when {
                                horaActual < 13 -> "MAÑANA"
                                horaActual in 13..17 -> "TARDE"
                                else -> "NOCHE"
                            }

                            val diaDeHoy = RoutineProgressCalculator.obtenerDiaDeHoy()
                            val document = db.collection("routines")
                                .document(userId)
                                .collection("turns")
                                .document(turnoActual)
                                .collection("days")
                                .document(diaDeHoy)
                                .get()
                                .await()

                            var actividadActualStr = "Libre"
                            var tiempoRestante = 0
                            var estadoEmocionalActual = ""
                            var ultimaTareaApplicable: Map<*, *>? = null
                            var tieneTareasHoy = false
                            var todasCompletadas = true
                            val prefijoDia = RoutineProgressCalculator.obtenerPrefijoDia(diaDeHoy)

                            if (document.exists()) {
                                val tasksList = document.get("tasks") as? List<*> ?: emptyList<Any>()

                                for (taskItem in tasksList) {
                                    if (taskItem is Map<*, *>) {
                                        val rawDias = taskItem["dias"] as? List<*> ?: emptyList<Any>()
                                        val dias = rawDias.mapNotNull { it?.toString() }

                                        val aplicaHoy = dias.isEmpty() || dias.any { it.uppercase().trim().startsWith(prefijoDia.uppercase()) }

                                        if (aplicaHoy) {
                                            tieneTareasHoy = true
                                            ultimaTareaApplicable = taskItem

                                            val rawEstados = taskItem["estadosPorDia"] as? Map<*, *> ?: emptyMap<Any, Any>()
                                            val estadosPorDia = rawEstados.entries.associate {
                                                it.key.toString() to (it.value as? Boolean ?: false)
                                            }
                                            val estaCompletada = estadosPorDia[prefijoDia] ?: false

                                            val rawEmociones = taskItem["emocionesPorDia"] as? Map<*, *> ?: emptyMap<Any, Any>()
                                            val emocionDeEstaTarea = rawEmociones[prefijoDia]?.toString() ?: ""

                                            if (estaCompletada) {
                                                if (emocionDeEstaTarea.isNotEmpty()) {
                                                    estadoEmocionalActual = emocionDeEstaTarea
                                                }
                                            } else {
                                                todasCompletadas = false
                                                actividadActualStr = taskItem["actividad"] as? String ?: "Actividad"
                                                val durationNum = taskItem["duration"] as? Number ?: 15
                                                tiempoRestante = durationNum.toInt()
                                                break
                                            }
                                        }
                                    }
                                }
                            }

                            val rutinaCompletada = tieneTareasHoy && todasCompletadas
                            if (rutinaCompletada && ultimaTareaApplicable != null) {
                                actividadActualStr = ultimaTareaApplicable["actividad"] as? String ?: "Libre"
                                tiempoRestante = 0
                            }

                            // Guardar en caché
                            prefs.edit().apply {
                                putString("ACTUAL_ACTIVIDAD", actividadActualStr)
                                putInt("ACTUAL_MINUTOS", tiempoRestante)
                                putString("ACTUAL_ESTADO", estadoEmocionalActual)
                                putBoolean("RUTINA_COMPLETADA", rutinaCompletada)
                                putLong("ULTIMO_FETCH_SESSION", tiempoActual)
                                apply()
                            }

                            // Actualizar UI con datos reales
                            val actividadActualStrLocalized = when (actividadActualStr) {
                                "Buscando actividad..." -> localizedContext.getString(R.string.widget_buscando)
                                "Libre" -> localizedContext.getString(R.string.widget_free_time)
                                else -> actividadActualStr
                            }
                            views.setTextViewText(R.id.tv_actividad_actual, actividadActualStrLocalized)
                            if (rutinaCompletada) {
                                val emocionTexto = when (estadoEmocionalActual.lowercase().trim()) {
                                    "feliz" -> localizedContext.getString(R.string.widget_feeling_happy)
                                    "neutral" -> localizedContext.getString(R.string.widget_feeling_neutral)
                                    "triste" -> localizedContext.getString(R.string.widget_feeling_sad)
                                    else -> localizedContext.getString(R.string.widget_feeling_unregistered)
                                }
                                views.setTextViewText(R.id.tv_tiempo_restante, localizedContext.getString(R.string.widget_routine_completed_feedback, emocionTexto))
                            } else {
                                if (actividadActualStr == "Libre" || actividadActualStr == "Buscando actividad...") {
                                    views.setTextViewText(R.id.tv_tiempo_restante, "---")
                                } else {
                                    views.setTextViewText(R.id.tv_tiempo_restante, localizedContext.getString(R.string.widget_next_activity_minutes, tiempoRestante))
                                }
                            }

                            configurarVisualizacionDeEmocion(context, views, estadoEmocionalActual)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    } catch (e: Exception) {
                        Log.e("ChildSessionMonitor", "Error cargando rutina actual", e)
                    }
                }
            }
        }

        private fun configurarVisualizacionDeEmocion(context: Context, views: RemoteViews, estado: String) {
            // 1. Limpiamos los fondos de todos los emojis
            views.setInt(R.id.btn_emoji_tranquilo, "setBackgroundColor", Color.TRANSPARENT)
            views.setInt(R.id.btn_emoji_ansioso, "setBackgroundColor", Color.TRANSPARENT)
            views.setInt(R.id.btn_emoji_crisis, "setBackgroundColor", Color.TRANSPARENT)

            // 2. Cambiamos el emoji de crisis a la carita triste para consistencia
            views.setTextViewText(R.id.btn_emoji_crisis, "🙁")

            // 3. Resaltamos el emoji según el estado
            when (estado.lowercase().trim()) {
                "feliz" -> views.setInt(R.id.btn_emoji_tranquilo, "setBackgroundResource", android.R.drawable.dialog_holo_light_frame)
                "neutral" -> views.setInt(R.id.btn_emoji_ansioso, "setBackgroundResource", android.R.drawable.dialog_holo_light_frame)
                "triste" -> views.setInt(R.id.btn_emoji_crisis, "setBackgroundResource", android.R.drawable.dialog_holo_light_frame)
            }

            // 4. Deshabilitamos los clics vaciando los intents usando el context correcto
            val emptyIntent = PendingIntent.getBroadcast(context, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_emoji_tranquilo, emptyIntent)
            views.setOnClickPendingIntent(R.id.btn_emoji_ansioso, emptyIntent)
            views.setOnClickPendingIntent(R.id.btn_emoji_crisis, emptyIntent)
        }

        private fun obtenerContextoLocalizado(context: Context): Context {
            val prefs = context.getSharedPreferences("SESSION_WIDGET_PREFS", Context.MODE_PRIVATE)
            val idiomaCode = prefs.getString("app_language", "es") ?: "es"
            val locale = java.util.Locale.forLanguageTag(idiomaCode)
            java.util.Locale.setDefault(locale)
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }
}