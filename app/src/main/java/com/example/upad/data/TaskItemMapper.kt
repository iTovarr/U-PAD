package com.example.upad.data

import com.example.upad.viewmodel.TaskItem

/**
 * Convierte el campo `tasks` de un documento de rutina de Firestore en [TaskItem].
 *
 * Es tolerante con datos incompletos o de tipos inesperados: cada campo que falte
 * toma su valor por defecto y los elementos que no son un mapa se ignoran.
 * Es Kotlin puro para poder probarse con tests unitarios.
 */
object TaskItemMapper {

    const val DURACION_POR_DEFECTO = 15

    /** [raw] es el valor de `snapshot.get("tasks")`; cualquier cosa que no sea una lista da una lista vacía. */
    fun fromFirestoreList(raw: Any?): List<TaskItem> {
        val lista = raw as? List<*> ?: return emptyList()
        return lista.filterIsInstance<Map<*, *>>().map(::fromMap)
    }

    fun fromMap(map: Map<*, *>): TaskItem {
        val dias = (map["dias"] as? List<*>).orEmpty().mapNotNull { it?.toString() }

        val estadosPorDia = (map["estadosPorDia"] as? Map<*, *>).orEmpty().entries.associate {
            it.key.toString() to (it.value as? Boolean ?: false)
        }

        val emocionesPorDia = (map["emocionesPorDia"] as? Map<*, *>).orEmpty().entries.associate {
            it.key.toString() to (it.value as? String ?: "")
        }

        return TaskItem(
            actividad = map["actividad"] as? String ?: "",
            palabraClave = map["palabraClave"] as? String ?: "",
            imageUrl = map["imageUrl"] as? String ?: "",
            dias = dias,
            // Firestore devuelve los números como Long.
            duration = (map["duration"] as? Number)?.toInt() ?: DURACION_POR_DEFECTO,
            estadosPorDia = estadosPorDia,
            emocionesPorDia = emocionesPorDia
        )
    }
}
