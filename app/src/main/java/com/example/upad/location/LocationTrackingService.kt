package com.example.upad.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.upad.R
import com.example.upad.UPadApplication
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val TAG = "LocationTrackingService"

/**
 * Servicio en primer plano que envía la ubicación del dispositivo del niño a
 * Firestore para que el tutor la vea en el mapa.
 *
 * Al ser un servicio en primer plano sigue funcionando con la app en segundo
 * plano o la pantalla apagada, y muestra una notificación permanente para que
 * quede claro que la ubicación se está compartiendo.
 *
 * Solo debe iniciarse con la app visible y el permiso de ubicación concedido
 * (requisito de Android 14 para los servicios de tipo "location").
 */
class LocationTrackingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var locationClient: FusedLocationProviderClient
    private var escuchando = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val container = (application as UPadApplication).container
            scope.launch {
                try {
                    container.firebaseRepository.updateDeviceLocation(
                        deviceId = container.deviceIdProvider(),
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "No se pudo enviar la ubicación", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!tienePermisoUbicacion(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                crearNotificacion(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
            )
            iniciarActualizaciones()
        } catch (e: Exception) {
            // Por ejemplo, si el sistema no permite iniciar el servicio en este momento.
            Log.e(TAG, "No se pudo iniciar el rastreo en primer plano", e)
            stopSelf()
        }

        // Si el sistema lo detiene no se reinicia solo: desde segundo plano Android no
        // permite arrancar un servicio de ubicación. Se reanuda al abrir el modo niño.
        return START_NOT_STICKY
    }

    private fun iniciarActualizaciones() {
        if (escuchando) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVALO_MS)
            .setMinUpdateIntervalMillis(INTERVALO_MINIMO_MS)
            .build()
        try {
            locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            escuchando = true
        } catch (e: SecurityException) {
            Log.e(TAG, "Sin permiso de ubicación", e)
            stopSelf()
        }
    }

    private fun crearNotificacion(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.location_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.location_notification_title))
            .setContentText(getString(R.string.location_notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        if (escuchando) locationClient.removeLocationUpdates(locationCallback)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "upad_location_channel"
        private const val NOTIFICATION_ID = 2001
        private const val INTERVALO_MS = 15_000L
        private const val INTERVALO_MINIMO_MS = 10_000L

        fun tienePermisoUbicacion(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        /** Inicia el rastreo. Debe llamarse con la app visible; sin permiso no hace nada. */
        fun start(context: Context) {
            if (!tienePermisoUbicacion(context)) return
            try {
                ContextCompat.startForegroundService(context, Intent(context, LocationTrackingService::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo iniciar el servicio de ubicación", e)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LocationTrackingService::class.java))
        }
    }
}
