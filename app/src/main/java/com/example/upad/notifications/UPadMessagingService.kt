package com.example.upad.notifications

import android.content.Context
import android.util.Log
import com.example.upad.UPadApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val TAG = "UPadMessagingService"

/**
 * Recibe las notificaciones push del tutor.
 *
 * Con la app en segundo plano o cerrada, el sistema muestra la notificación por sí
 * solo. Con la app en primer plano no hay nada que hacer aquí: MainActivity ya
 * escucha los avisos en Firestore y los muestra con la misma etiqueta (el ID del
 * aviso), de modo que nunca aparecen duplicados.
 */
class UPadMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Aviso push recibido con la app en primer plano")
    }

    /** FCM puede renovar el token en cualquier momento: se vuelve a asociar al tutor. */
    override fun onNewToken(token: String) {
        val container = (application as UPadApplication).container
        val uid = container.sessionRepository.currentUserId
        val rol = getSharedPreferences("UPadPrefs", Context.MODE_PRIVATE).getString("rol_usuario", null)
        if (uid.isEmpty() || (rol != "padre" && rol != "padre_directo")) return

        scope.launch {
            try {
                container.parentProfileRepository.addPushToken(uid, token)
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo guardar el nuevo token push", e)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
