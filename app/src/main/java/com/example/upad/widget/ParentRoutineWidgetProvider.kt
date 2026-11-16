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

class ParentRoutineWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH || intent.action == ACTION_DATA_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ParentRoutineWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                actualizarWidget(context, appWidgetManager, appWidgetId, forzarActualizacion = (intent.action == ACTION_REFRESH))
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.upad.widget.ACTION_REFRESH"
        const val ACTION_DATA_CHANGED = "com.example.upad.widget.ACTION_DATA_CHANGED"

        fun notificarCambioDatos(context: Context) {
            val intent = Intent(context, ParentRoutineWidgetProvider::class.java).apply {
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
            val views = RemoteViews(context.packageName, R.layout.widget_parent_routine)

            // Configurar PendingIntent para abrir la App al tocar el widget
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.widget_background, openAppPendingIntent)

            // Configurar PendingIntent para el botón de refresco
            val refreshIntent = Intent(context, ParentRoutineWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent)

            // Leer caché local de SharedPreferences primero
            val prefs = context.getSharedPreferences("WIDGET_PREFS", Context.MODE_PRIVATE)
            val mananaProgreso = prefs.getInt("PROGRESO_MANANA", 0)
            val tardeProgreso = prefs.getInt("PROGRESO_TARDE", 0)
            val nocheProgreso = prefs.getInt("PROGRESO_NOCHE", 0)

            val localizedContext = obtenerContextoLocalizado(context)

            // Configurar textos estáticos dinámicamente
            views.setTextViewText(R.id.widget_title, localizedContext.getString(R.string.todays_progress))
            views.setTextViewText(R.id.legend_morning_txt, localizedContext.getString(R.string.legend_morning))
            views.setTextViewText(R.id.legend_afternoon_txt, localizedContext.getString(R.string.legend_afternoon))
            views.setTextViewText(R.id.legend_evening_txt, localizedContext.getString(R.string.legend_evening))

            // Renderizar inmediatamente usando datos cacheados
            val bitmapInicial = generarGraficoBarras(localizedContext, mananaProgreso, tardeProgreso, nocheProgreso)
            views.setImageViewBitmap(R.id.chart_image, bitmapInicial)
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Si se requiere actualización o si la caché tiene más de 5 minutos, consultar Firestore en background
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

                            // Soportar múltiples niños en futuras versiones:
                            // Buscamos dispositivos asociados al padre
                            val querySnapshot = db.collection("dispositivos_niños")
                                .whereEqualTo("padreId", userId)
                                .get()
                                .await()

                            val idsParaConsultar = mutableListOf<String>()
                            if (!querySnapshot.isEmpty) {
                                for (doc in querySnapshot.documents) {
                                    idsParaConsultar.add(doc.id)
                                }
                            }

                            val diaDeHoy = RoutineProgressCalculator.obtenerDiaDeHoy()
                            val prefijoDia = RoutineProgressCalculator.obtenerPrefijoDia(diaDeHoy)

                            // Consultamos las rutinas del padre (las cuales se aplican a sus hijos actualmente)
                            val mananaTareas = obtenerTareasDeTurno(db, userId, "MAÑANA")
                            val tardeTareas = obtenerTareasDeTurno(db, userId, "TARDE")
                            val nocheTareas = obtenerTareasDeTurno(db, userId, "NOCHE")

                            val progManana = calcularPorcentaje(mananaTareas, prefijoDia)
                            val progTarde = calcularPorcentaje(tardeTareas, prefijoDia)
                            val progNoche = calcularPorcentaje(nocheTareas, prefijoDia)

                            // Guardar en caché
                            prefs.edit().apply {
                                putInt("PROGRESO_MANANA", progManana)
                                putInt("PROGRESO_TARDE", progTarde)
                                putInt("PROGRESO_NOCHE", progNoche)
                                putLong("ULTIMO_FETCH", tiempoActual)
                                apply()
                            }

                            // Actualizar vista
                            val bitmapActualizado = generarGraficoBarras(localizedContext, progManana, progTarde, progNoche)
                            views.setImageViewBitmap(R.id.chart_image, bitmapActualizado)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        } catch (e: Exception) {
                            Log.e("WidgetProvider", "Error cargando rutinas", e)
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

        private fun generarGraficoBarras(context: Context, progManana: Int, progTarde: Int, progNoche: Int): Bitmap {
            val width = 300
            val height = 150
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            canvas.drawColor(Color.TRANSPARENT)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            val colorManana = Color.parseColor("#FFB74D") // Naranja
            val colorTarde = Color.parseColor("#81C784")  // Verde
            val colorNoche = Color.parseColor("#9575CD")  // Morado
            val colorFondoBarra = Color.parseColor("#EAEAEA") // Gris sutil

            val progresos = listOf(progManana, progTarde, progNoche)
            val colores = listOf(colorManana, colorTarde, colorNoche)
            val nombres = listOf(
                context.getString(R.string.axis_morning),
                context.getString(R.string.axis_afternoon),
                context.getString(R.string.axis_evening)
            )

            val barWidth = 32f
            val spacing = 50f
            val leftStart = (width - (3 * barWidth + 2 * spacing)) / 2f
            val chartBottom = height - 25f
            val maxBarHeight = height - 55f

            for (i in 0 until 3) {
                val left = leftStart + i * (barWidth + spacing)
                val right = left + barWidth

                // Fondo de la barra (100%)
                rectPaint.color = colorFondoBarra
                rectPaint.style = Paint.Style.FILL
                val bgRect = RectF(left, chartBottom - maxBarHeight, right, chartBottom)
                canvas.drawRoundRect(bgRect, 8f, 8f, rectPaint)

                // Progreso real de la barra
                val progreso = progresos[i]
                val barHeight = maxBarHeight * (progreso / 100f)
                if (barHeight > 0) {
                    rectPaint.color = colores[i]
                    val fgRect = RectF(left, chartBottom - barHeight, right, chartBottom)
                    canvas.drawRoundRect(fgRect, 8f, 8f, rectPaint)
                }

                // Porcentaje numérico en la parte superior
                paint.color = Color.parseColor("#333333")
                paint.textSize = 12f
                paint.textAlign = Paint.Align.CENTER
                paint.isFakeBoldText = true
                canvas.drawText("$progreso%", (left + right) / 2f, chartBottom - Math.max(barHeight, 10f) - 6f, paint)

                // Etiqueta del eje X (M, T, N)
                paint.color = Color.parseColor("#666666")
                paint.textSize = 11f
                paint.isFakeBoldText = false
                canvas.drawText(nombres[i], (left + right) / 2f, chartBottom + 16f, paint)
            }

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
