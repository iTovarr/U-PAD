package com.example.upad.data

import com.example.upad.utils.PairingCode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Dispositivo de un niño vinculado a un tutor, tal como está guardado en Firestore. */
data class LinkedDevice(
    val id: String,
    val modelo: String?,
    val nombreDispositivo: String?,
    val kioscoActivo: Boolean
)

/** Última ubicación conocida de un dispositivo. */
data class DeviceLocation(val latitud: Double, val longitud: Double)

/** Resultado de intentar canjear un código de vinculación. */
enum class PairingResult { VINCULADO, CODIGO_INVALIDO }

/**
 * Acceso a los dispositivos de los niños (`dispositivos_niños`) y a los códigos
 * de vinculación (`codigos_vinculacion`) desde el lado del tutor.
 */
class DeviceRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private val dispositivos get() = firestore.collection("dispositivos_niños")
    private val codigos get() = firestore.collection("codigos_vinculacion")

    /**
     * Emite en tiempo real los dispositivos vinculados al tutor.
     * Si Firestore devuelve un error, el flujo termina con esa excepción.
     */
    fun observeDevicesOf(padreId: String): Flow<List<LinkedDevice>> = callbackFlow {
        val listener = dispositivos
            .whereEqualTo("padreId", padreId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents.orEmpty().map { doc ->
                    LinkedDevice(
                        id = doc.id,
                        modelo = doc.getString("modelo"),
                        nombreDispositivo = doc.getString("nombreDispositivo"),
                        kioscoActivo = doc.getBoolean("kioscoActivo") ?: false
                    )
                }
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    /** Emite la ubicación del dispositivo cada vez que cambia (0.0, 0.0 si aún no la ha enviado). */
    fun observeLocation(deviceId: String): Flow<DeviceLocation> = callbackFlow {
        val listener = dispositivos.document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(
                        DeviceLocation(
                            latitud = snapshot.getDouble("latitud") ?: 0.0,
                            longitud = snapshot.getDouble("longitud") ?: 0.0
                        )
                    )
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Canjea el código que muestra el dispositivo del niño a nombre del tutor.
     * Solo se acepta un código que siga en espera y no haya expirado.
     * Lanza una excepción si falla la conexión con Firestore.
     */
    suspend fun redeemPairingCode(code: String, padreId: String): PairingResult {
        val ref = codigos.document(code)
        val snapshot = ref.get().await()
        val canjeable = snapshot.exists() && PairingCode.esCanjeable(
            estado = snapshot.getString("estado"),
            creadoEnMillis = snapshot.getTimestamp("creadoEn")?.toDate()?.time,
            ahoraMillis = System.currentTimeMillis()
        )
        if (!canjeable) return PairingResult.CODIGO_INVALIDO

        ref.update(
            mapOf(
                "estado" to PairingCode.ESTADO_ENLAZADO,
                "padreId" to padreId
            )
        ).await()
        return PairingResult.VINCULADO
    }

    /** Desvincula un dispositivo borrando su documento. */
    suspend fun deleteDevice(deviceId: String) {
        dispositivos.document(deviceId).delete().await()
    }

    /** Activa o desactiva el bloqueo remoto (modo kiosco) del dispositivo. */
    suspend fun setKioskActive(deviceId: String, activo: Boolean) {
        dispositivos.document(deviceId)
            .set(mapOf("kioscoActivo" to activo), SetOptions.merge())
            .await()
    }
}
