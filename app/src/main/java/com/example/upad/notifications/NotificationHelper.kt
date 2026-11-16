package com.example.upad.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.upad.MainActivity
import com.example.upad.R

private const val TAG = "NotificationHelper"

/** Notificaciones locales que avisan al tutor del avance de las rutinas del niño. */
class NotificationHelper(context: Context) {

    private val appContext = context.applicationContext

    /** Crea el canal de notificaciones (obligatorio desde Android 8). Es seguro llamarlo varias veces. */
    fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Notificaciones de Rutina",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas sobre el estado de las rutinas del niño"
        }
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /** Muestra una notificación que abre la app al tocarla. No hace nada si falta el permiso. */
    fun mostrar(title: String, description: String) {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Desde Android 13 hace falta el permiso POST_NOTIFICATIONS concedido por el usuario.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            NotificationManagerCompat.from(appContext)
                .notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "No se pudo mostrar la notificación", e)
        }
    }

    private companion object {
        const val CHANNEL_ID = "upad_notifications_channel"
    }
}
