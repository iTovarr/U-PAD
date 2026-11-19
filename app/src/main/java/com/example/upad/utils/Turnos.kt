package com.example.upad.utils

import java.util.Calendar

/**
 * Turnos del día en los que se reparten las rutinas. Los nombres coinciden con
 * los IDs de documento de Firestore (`routines/{uid}/turns/{turno}`).
 */
object Turnos {
    const val MANANA = "MAÑANA"
    const val TARDE = "TARDE"
    const val NOCHE = "NOCHE"

    /** Hora (0-23) a la que empieza la tarde y la noche. */
    private const val INICIO_TARDE = 13
    private const val INICIO_NOCHE = 18

    /** Turno que corresponde a una hora del día (0-23). */
    fun paraHora(hora: Int): String = when {
        hora < INICIO_TARDE -> MANANA
        hora < INICIO_NOCHE -> TARDE
        else -> NOCHE
    }

    /** Turno en curso según la hora del dispositivo. */
    fun actual(): String = paraHora(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
}
