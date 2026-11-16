package com.example.upad.notifications

import com.example.upad.data.ParentProfileRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Asocia el token de notificaciones push (FCM) de este dispositivo con la cuenta
 * del tutor, para que la Cloud Function `notifyParent` sepa a qué dispositivos avisar.
 */
class PushTokenManager(private val parentProfileRepository: ParentProfileRepository) {

    /** Registra este dispositivo para recibir los avisos del tutor. */
    suspend fun registrar(uid: String) {
        parentProfileRepository.addPushToken(uid, tokenActual())
    }

    /** Deja de enviar a este dispositivo los avisos del tutor (al cerrar sesión). */
    suspend fun eliminar(uid: String) {
        parentProfileRepository.removePushToken(uid, tokenActual())
    }

    private suspend fun tokenActual(): String = FirebaseMessaging.getInstance().token.await()
}
