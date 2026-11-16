package com.example.upad.data

import android.net.Uri
import com.example.upad.viewmodel.TaskItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    suspend fun saveUserData(userId: String, name: String) {
        firestore.collection("users").document(userId)
            .set(mapOf("name" to name), SetOptions.merge())
            .await()
    }

    suspend fun uploadPictogram(userId: String, imageUri: Uri): String {
        val ref = storage.child("pictograms/$userId/${System.currentTimeMillis()}.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun setPremiumStatus(userId: String, isPremium: Boolean) {
        firestore.collection("users").document(userId)
            .set(mapOf("isPremium" to isPremium), SetOptions.merge())
            .await()
    }

    suspend fun saveUserLanguage(userId: String, languageCode: String) {
        firestore.collection("users").document(userId)
            .set(mapOf("language" to languageCode), SetOptions.merge())
            .await()
    }

    fun listenUserLanguage(userId: String, onLanguageChanged: (String) -> Unit): ListenerRegistration {
        return firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val lang = snapshot?.getString("language")
                if (!lang.isNullOrEmpty()) {
                    onLanguageChanged(lang)
                }
            }
    }

    // --- GUARDADO CORREGIDO ---
    // Guardamos en: routines -> userId -> turns -> {turno}
    suspend fun saveRoutine(userId: String, turn: String, tasks: List<TaskItem>) {
        firestore.collection("routines")
            .document(userId)
            .collection("turns")
            .document(turn.uppercase()) // Asegura que se guarde en MAYÚSCULAS limpias
            .set(mapOf("tasks" to tasks), SetOptions.merge())
            .await()
    }

    // --- ESCUCHA EN TIEMPO REAL CORREGIDA ---
    // Escucha EXACTAMENTE en la misma ruta donde se guarda
    fun escucharRutinasDelPadre(padreId: String, turn: String, onDataChanged: (List<TaskItem>) -> Unit): ListenerRegistration {
        return firestore.collection("routines")
            .document(padreId)
            .collection("turns")
            .document(turn.uppercase()) // Buscamos en "MAÑANA", "TARDE" o "NOCHE"
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val tareas = mutableListOf<TaskItem>()
                if (snapshot != null && snapshot.exists()) {
                    val tasksList = snapshot.get("tasks") as? List<*> ?: emptyList<Any>()
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

                                val rawEmociones = taskItem["emocionesPorDia"] as? Map<*, *> ?: emptyMap<Any, Any>()
                                val emocionesPorDia = rawEmociones.entries.associate {
                                    it.key.toString() to (it.value as? String ?: "")
                                }

                                tareas.add(
                                    TaskItem(
                                        actividad = actividad,
                                        palabraClave = palabraClave,
                                        imageUrl = imageUrl,
                                        dias = dias,
                                        duration = durationNum.toInt(),
                                        estadosPorDia = estadosPorDia,
                                        emocionesPorDia = emocionesPorDia
                                    )
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                // Le enviamos la lista real con "actividad" al ViewModel
                onDataChanged(tareas)
            }
    }

    suspend fun obtenerRutinasDelPadreDirecto(padreId: String, turn: String): List<TaskItem> {
        val document = firestore.collection("routines")
            .document(padreId)
            .collection("turns")
            .document(turn.uppercase().trim())
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

                        val rawEmociones = taskItem["emocionesPorDia"] as? Map<*, *> ?: emptyMap<Any, Any>()
                        val emocionesPorDia = rawEmociones.entries.associate {
                            it.key.toString() to (it.value as? String ?: "")
                        }

                        tareas.add(
                            TaskItem(
                                actividad = actividad,
                                palabraClave = palabraClave,
                                imageUrl = imageUrl,
                                dias = dias,
                                duration = durationNum.toInt(),
                                estadosPorDia = estadosPorDia,
                                emocionesPorDia = emocionesPorDia
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

    suspend fun saveRoutinePorDia(userId: String, turn: String, dia: String, tasks: List<TaskItem>) {
        firestore.collection("routines")
            .document(userId)
            .collection("turns")
            .document(turn.uppercase())
            .collection("days")
            .document(dia.uppercase())
            .set(mapOf("tasks" to tasks), SetOptions.merge())
            .await()
    }

    fun escucharRutinasDelPadrePorDia(padreId: String, turn: String, dia: String, onDataChanged: (List<TaskItem>) -> Unit): ListenerRegistration {
        return firestore.collection("routines")
            .document(padreId)
            .collection("turns")
            .document(turn.uppercase())
            .collection("days")
            .document(dia.uppercase())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val tareas = mutableListOf<TaskItem>()
                if (snapshot != null && snapshot.exists()) {
                    val tasksList = snapshot.get("tasks") as? List<*> ?: emptyList<Any>()
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

                                val rawEmociones = taskItem["emocionesPorDia"] as? Map<*, *> ?: emptyMap<Any, Any>()
                                val emocionesPorDia = rawEmociones.entries.associate {
                                    it.key.toString() to (it.value as? String ?: "")
                                }

                                tareas.add(
                                    TaskItem(
                                        actividad = actividad,
                                        palabraClave = palabraClave,
                                        imageUrl = imageUrl,
                                        dias = dias,
                                        duration = durationNum.toInt(),
                                        estadosPorDia = estadosPorDia,
                                        emocionesPorDia = emocionesPorDia
                                    )
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                onDataChanged(tareas)
            }
    }

    suspend fun obtenerRutinasDelPadreDirectoPorDia(padreId: String, turn: String, dia: String): List<TaskItem> {
        val document = firestore.collection("routines")
            .document(padreId)
            .collection("turns")
            .document(turn.uppercase())
            .collection("days")
            .document(dia.uppercase())
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

                        val rawEmociones = taskItem["emocionesPorDia"] as? Map<*, *> ?: emptyMap<Any, Any>()
                        val emocionesPorDia = rawEmociones.entries.associate {
                            it.key.toString() to (it.value as? String ?: "")
                        }

                        tareas.add(
                            TaskItem(
                                actividad = actividad,
                                palabraClave = palabraClave,
                                imageUrl = imageUrl,
                                dias = dias,
                                duration = durationNum.toInt(),
                                estadosPorDia = estadosPorDia,
                                emocionesPorDia = emocionesPorDia
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

    fun listenToChildDevice(deviceId: String, onSnapshot: (com.google.firebase.firestore.DocumentSnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit): ListenerRegistration {
        return firestore.collection("dispositivos_niños")
            .document(deviceId)
            .addSnapshotListener(onSnapshot)
    }

    fun createPairingCode(code: String, deviceId: String) {
        firestore.collection("codigos_vinculacion").document(code)
            .set(mapOf(
                "deviceId" to deviceId,
                "estado" to "esperando",
                "padreId" to ""
            ))
    }

    fun listenToPairingCode(code: String, onSnapshot: (com.google.firebase.firestore.DocumentSnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit): ListenerRegistration {
        return firestore.collection("codigos_vinculacion")
            .document(code)
            .addSnapshotListener(onSnapshot)
    }

    fun linkDeviceToParent(deviceId: String, parentId: String) {
        firestore.collection("dispositivos_niños").document(deviceId)
            .set(mapOf(
                "padreId" to parentId,
                "modelo" to android.os.Build.MODEL
            ), SetOptions.merge())
    }

    fun deletePairingCode(code: String) {
        firestore.collection("codigos_vinculacion")
            .document(code)
            .delete()
    }

    fun listenToUserPremium(parentId: String, onPremiumChanged: (Boolean) -> Unit): ListenerRegistration {
        return firestore.collection("users").document(parentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val isPremium = snapshot?.getBoolean("isPremium") ?: false
                onPremiumChanged(isPremium)
            }
    }

    suspend fun updateDeviceLocation(deviceId: String, latitude: Double, longitude: Double) {
        firestore.collection("dispositivos_niños")
            .document(deviceId)
            .set(mapOf(
                "latitud" to latitude,
                "longitud" to longitude,
                "ultimaActualizacion" to com.google.firebase.Timestamp.now()
            ), SetOptions.merge())
            .await()
    }

    suspend fun deleteDeviceDoc(deviceId: String) {
        firestore.collection("dispositivos_niños")
            .document(deviceId)
            .delete()
            .await()
    }

    suspend fun guardarMensajesRefuerzo(userId: String, turn: String, mensajes: List<String>) {
        firestore.collection("routines")
            .document(userId)
            .collection("turns")
            .document(turn.uppercase().trim())
            .set(mapOf("mensajesRefuerzo" to mensajes), SetOptions.merge())
            .await()
    }

    suspend fun obtenerMensajesRefuerzo(userId: String, turn: String): List<String> {
        val doc = firestore.collection("routines")
            .document(userId)
            .collection("turns")
            .document(turn.uppercase().trim())
            .get()
            .await()
        return if (doc.exists()) {
            val list = doc.get("mensajesRefuerzo") as? List<*>
            list?.mapNotNull { it?.toString() } ?: emptyList()
        } else {
            emptyList()
        }
    }
}