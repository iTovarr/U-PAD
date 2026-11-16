package com.example.upad.data

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

/** Aviso que el dispositivo del niño deja al tutor (por ejemplo, "actividad completada"). */
data class ParentNotification(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val timestamp: Date?
)

/**
 * Datos del tutor: plan de suscripción, perfil del hijo, ajustes de experiencia
 * (colección `usuarios`) y avisos recibidos (`users/{uid}/notifications`).
 */
class ParentProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private fun usuario(uid: String) = firestore.collection("usuarios").document(uid)

    /**
     * Registra el plan elegido. No espera la confirmación del servidor: Firestore
     * encola la escritura y la envía aunque la pantalla ya se haya cerrado.
     */
    fun saveSubscriptionPlan(uid: String, plan: String, estado: String) {
        usuario(uid).set(
            mapOf(
                "planSuscripcion" to plan,
                "estadoSuscripcion" to estado,
                "fechaSeleccionPlan" to Timestamp.now()
            ),
            SetOptions.merge()
        )
    }

    /** Guarda los ajustes de tema y audio. Igual que el plan, no espera al servidor. */
    fun saveExperienceSettings(uid: String, tema: String, volumenAudio: Double, vozHabilitada: Boolean) {
        usuario(uid).collection("configuracion").document("experiencia").set(
            mapOf(
                "temaSeleccionado" to tema,
                "volumenAudio" to volumenAudio,
                "vozHabilitada" to vozHabilitada
            ),
            SetOptions.merge()
        )
    }

    /** Sube la foto de perfil del hijo y devuelve su URL de descarga. */
    suspend fun uploadChildPhoto(uid: String, imageUri: Uri): String {
        val ref = storage.reference.child("usuarios/$uid/perfil_hijo.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun saveChildProfile(uid: String, nombre: String, edad: String, intereses: String, fotoUrl: String) {
        usuario(uid).collection("hijos").document("perfil").set(
            mapOf(
                "nombre" to nombre,
                "edad" to edad,
                "intereses" to intereses,
                "fotoUrl" to fotoUrl,
                "fechaRegistro" to Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    /**
     * Emite en tiempo real los avisos del tutor, del más reciente al más antiguo.
     * Si Firestore devuelve un error, el flujo termina con esa excepción.
     */
    fun observeNotifications(padreId: String): Flow<List<ParentNotification>> = callbackFlow {
        val listener = firestore.collection("users")
            .document(padreId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val avisos = snapshot?.documents.orEmpty().map { doc ->
                    ParentNotification(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        type = doc.getString("type") ?: "INFO",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()
                    )
                }
                trySend(avisos)
            }
        awaitClose { listener.remove() }
    }
}
