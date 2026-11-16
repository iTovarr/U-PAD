package com.example.upad.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.upad.MainActivity
import com.example.upad.R
import com.example.upad.utils.RoutineProgressCalculator
import com.example.upad.viewmodel.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PremiumProgressWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH || intent.action == ACTION_DATA_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PremiumProgressWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                actualizarWidget(context, appWidgetManager, appWidgetId, forzarActualizacion = (intent.action == ACTION_REFRESH))
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.upad.widget.premium.ACTION_REFRESH"
        const val ACTION_DATA_CHANGED = "com.example.upad.widget.ACTION_DATA_CHANGED"

        fun notificarCambioDatos(context: Context) {
            val intent = Intent(context, PremiumProgressWidgetProvider::class.java).apply {
                action = ACTION_DATA_CHANGED
            }
            context.sendBroadcast(intent)
        }

        private fun actualizarWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            forzarActualizacion: Boolean = false
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_premium_progress)

            // Configurar PendingIntent para abrir la App al tocar el fondo del widget
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.widget_background, openAppPendingIntent)

            // Configurar PendingIntent para el botón de refresco
            val refreshIntent = Intent(context, PremiumProgressWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent)

            // Leer de SharedPreferences (Caché local compartida)
            val prefs = context.getSharedPreferences("WIDGET_PREFS", Context.MODE_PRIVATE)
            val total = prefs.getInt("TOTAL_TAREAS_DIA", 0)
            val completadas = prefs.getInt("COMPLETADAS_TAREAS_DIA", 0)
            val pendientes = prefs.getInt("PENDIENTES_TAREAS_DIA", 0)
            val porcentaje = prefs.getInt("PORCENTAJE_GLOBAL", 0)
            val esPremium = prefs.getBoolean("IS_PREMIUM", false)

            val localizedContext = obtenerContextoLocalizado(context)

            // Configurar textos estáticos dinámicamente según la localización
            views.setTextViewText(R.id.widget_title, localizedContext.getString(R.string.todays_progress))
            views.setTextViewText(R.id.txt_total_label, localizedContext.getString(R.string.total_tasks))
            views.setTextViewText(R.id.txt_completed_label, localizedContext.getString(R.string.completed_tasks))
            views.setTextViewText(R.id.txt_pending_label, localizedContext.getString(R.string.pending_tasks))
            views.setTextViewText(R.id.txt_premium_locked_title, localizedContext.getString(R.string.todays_progress))
            views.setTextViewText(R.id.txt_premium_locked_desc, localizedContext.getString(R.string.available_in_premium))

            // Renderizar la interfaz según estado premium
            if (esPremium) {
                views.setViewVisibility(R.id.layout_premium_active, View.VISIBLE)
                views.setViewVisibility(R.id.layout_premium_locked, View.GONE)

                // Renderizar gráfico circular dinámico con el porcentaje
                val bitmapCircular = generarGraficoCircular(porcentaje)
                views.setImageViewBitmap(R.id.progress_circle_image, bitmapCircular)

                // Actualizar contadores numéricos
                views.setTextViewText(R.id.txt_total_value, total.toString())
                views.setTextViewText(R.id.txt_completed_value, completadas.toString())
                views.setTextViewText(R.id.txt_pending_value, pendientes.toString())
            } else {
                views.setViewVisibility(R.id.layout_premium_active, View.GONE)
                views.setViewVisibility(R.id.layout_premium_locked, View.VISIBLE)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Cargar datos en background de Firestore si expiró el tiempo o si es un refresh manual
            val ultimoFetch = prefs.getLong("ULTIMO_FETCH", 0)
            val tiempoActual = System.currentTimeMillis()
            if (forzarActualizacion || (tiempoActual - ultimoFetch > 5 * 60 * 1000)) {
                CoroutineScope(Dispatchers.IO).launch {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        try {
                            val db = FirebaseFirestore.getInstance()

                            // Consultar plan del usuario para validar premium
                            val userDoc = db.collection("users").document(userId).get().await()
                            val esPremiumReal = userDoc.getBoolean("isPremium") ?: false

                            val diaDeHoy = RoutineProgressCalculator.obtenerDiaDeHoy()
                            val prefijoDia = RoutineProgressCalculator.obtenerPrefijoDia(diaDeHoy)

                            // Consultar tareas de los tres turnos
                            val mananaTareas = obtenerTareasDeTurno(db, userId, "MAÑANA")
                            val tardeTareas = obtenerTareasDeTurno(db, userId, "TARDE")
                            val nocheTareas = obtenerTareasDeTurno(db, userId, "NOCHE")

                            // Calcular porcentajes individuales para el widget de barras
                            val progManana = calcularPorcentaje(mananaTareas, prefijoDia)
                            val progTarde = calcularPorcentaje(tardeTareas, prefijoDia)
                            val progNoche = calcularPorcentaje(nocheTareas, prefijoDia)

                            // Calcular totales globales del día
                            val (totalManana, compManana) = RoutineProgressCalculator.calcularProgreso(mananaTareas, prefijoDia)
                            val (totalTarde, compTarde) = RoutineProgressCalculator.calcularProgreso(tardeTareas, prefijoDia)
                            val (totalNoche, compNoche) = RoutineProgressCalculator.calcularProgreso(nocheTareas, prefijoDia)

                            val totalTareasDia = totalManana + totalTarde + totalNoche
                            val completadasTareasDia = compManana + compTarde + compNoche
                            val pendientesTareasDia = totalTareasDia - completadasTareasDia
                            val porcentajeGlobal = if (totalTareasDia > 0) (completadasTareasDia * 100) / totalTareasDia else 0

                            // Guardar en la caché compartida
                            prefs.edit().apply {
                                putInt("PROGRESO_MANANA", progManana)
                                putInt("PROGRESO_TARDE", progTarde)
                                putInt("PROGRESO_NOCHE", progNoche)
                                
                                putInt("TOTAL_TAREAS_DIA", totalTareasDia)
                                putInt("COMPLETADAS_TAREAS_DIA", completadasTareasDia)
                                putInt("PENDIENTES_TAREAS_DIA", pendientesTareasDia)
                                putInt("PORCENTAJE_GLOBAL", porcentajeGlobal)
                                putBoolean("IS_PREMIUM", esPremiumReal)
                                putLong("ULTIMO_FETCH", tiempoActual)
                                apply()
                            }

                            // Notificar a ambos proveedores para refrescar UI
                            ParentRoutineWidgetProvider.notificarCambioDatos(context)
                            notificarCambioDatos(context)
                        } catch (e: Exception) {
                            Log.e("PremiumWidgetProvider", "Error cargando rutinas desde Firestore", e)
                        }
                    }
                }
            }
        }

        private suspend fun obtenerTareasDeTurno(db: FirebaseFirestore, userId: String, turn: String): List<TaskItem> {
            val diaDeHoy = RoutineProgressCalculator.obtenerDiaDeHoy()
            val document = db.collection("routines")
                .document(userId)
                .collection("turns")
                .document(turn.uppercase())
                .collection("days")
                .document(diaDeHoy)
                .get()
                .await()

            val tareas = mutableListOf<TaskItem>()
            if (document.exists()) {
                val tasksList = document.get("tasks") as? List<*> ?: emptyList<Any>()
                for (taskItem in tasksList) {
                    if (taskItem is Map<*, *>) {
                        try {
                            val actividad = taskItem["actividad"] as? String ?: ""
                            val palabraClave = taskItem["palabraClave"] as? String ?: ""
                            val imageUrl = taskItem["imageUrl"] as? String ?: ""
                            val rawDias = taskItem["dias"] as? List<*> ?: emptyList<Any>()
                            val dias = rawDias.mapNotNull { it?.toString() }
                            val durationNum = taskItem["duration"] as? Number ?: 15
                            val rawEstados = taskItem["estadosPorDia"] as? Map<*, *> ?: emptyMap<Any, Any>()
                            val estadosPorDia = rawEstados.entries.associate {
                                it.key.toString() to (it.value as? Boolean ?: false)
                            }
                            tareas.add(
                                TaskItem(
                                    actividad = actividad,
                                    palabraClave = palabraClave,
                                    imageUrl = imageUrl,
                                    dias = dias,
                                    duration = durationNum.toInt(),
                                    estadosPorDia = estadosPorDia,
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            return tareas
        }

        private fun calcularPorcentaje(tareas: List<TaskItem>, prefijoDia: String): Int {
            val (total, completadas) = RoutineProgressCalculator.calcularProgreso(tareas, prefijoDia)
            return if (total > 0) (completadas * 100) / total else 0
        }

        private fun generarGraficoCircular(porcentaje: Int): Bitmap {
            val size = 200
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // 1. Dibujar el círculo de fondo de la barra (gris semitransparente)
            paint.color = Color.parseColor("#1FFFFFFF") // Blanco con 12% opacidad
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 16f
            paint.strokeCap = Paint.Cap.ROUND
            val margin = 20f
            val rect = RectF(margin, margin, size - margin, size - margin)
            canvas.drawArc(rect, 0f, 360f, false, paint)

            // 2. Dibujar el arco de progreso verde
            if (porcentaje > 0) {
                paint.color = Color.parseColor("#9CCC65") // Verde premium
                val sweepAngle = 360f * (porcentaje / 100f)
                canvas.drawArc(rect, -90f, sweepAngle, false, paint)
            }

            // 3. Dibujar el texto del porcentaje en el centro
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.textSize = 42f
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true

            val text = "$porcentaje%"
            val textHeight = paint.descent() - paint.ascent()
            val textOffset = textHeight / 2 - paint.descent()
            canvas.drawText(text, size / 2f, size / 2f + textOffset, paint)

            return bitmap
        }

        private fun obtenerContextoLocalizado(context: Context): Context {
            val prefs = context.getSharedPreferences("WIDGET_PREFS", Context.MODE_PRIVATE)
            val idiomaCode = prefs.getString("app_language", "es") ?: "es"
            val locale = java.util.Locale.forLanguageTag(idiomaCode)
            java.util.Locale.setDefault(locale)
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }
}
