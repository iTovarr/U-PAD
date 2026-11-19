package com.example.upad.data

import com.example.upad.ui.theme.AppPalette

/** Intensidad de los colores en el modo niño. */
enum class ColorIntensity { SUAVE, NORMAL }

/** Cómo se muestra el tiempo de cada tarea. */
enum class TimerStyle { CIRCULO, BARRA, NINGUNO }

/** Cuánto se celebra que el niño termine una tarea o la rutina. */
enum class CelebrationStyle { DISCRETA, NORMAL }

/** Claro u oscuro en las pantallas del niño. */
enum class ChildThemeMode {
    /** Oscuro durante la rutina de noche, claro el resto del día. */
    AUTOMATICO,
    CLARO,
    OSCURO
}

/**
 * Ajustes sensoriales del modo niño. Los elige el tutor y se sincronizan con el
 * dispositivo del niño a través de su documento en `users/{uid}`.
 *
 * Los valores por defecto son los más tranquilos: colores suaves y celebración
 * discreta. El tutor puede subir la intensidad si al niño le conviene.
 */
data class SensoryProfile(
    val animaciones: Boolean = true,
    val sonido: Boolean = true,
    val voz: Boolean = true,
    /** Volumen de la voz y de los sonidos, de 0 a 1. */
    val volumen: Float = VOLUMEN_POR_DEFECTO,
    val intensidadColor: ColorIntensity = ColorIntensity.SUAVE,
    val temporizador: TimerStyle = TimerStyle.CIRCULO,
    val celebracion: CelebrationStyle = CelebrationStyle.DISCRETA,
    val paleta: AppPalette = AppPalette.PorDefecto,
    val tema: ChildThemeMode = ChildThemeMode.AUTOMATICO
) {
    /** Si las pantallas del niño deben verse en oscuro, según el turno en curso. */
    fun usaModoOscuro(esTurnoNoche: Boolean): Boolean = when (tema) {
        ChildThemeMode.AUTOMATICO -> esTurnoNoche
        ChildThemeMode.CLARO -> false
        ChildThemeMode.OSCURO -> true
    }

    /** Representación para guardar en Firestore. */
    fun toMap(): Map<String, Any> = mapOf(
        CAMPO_ANIMACIONES to animaciones,
        CAMPO_SONIDO to sonido,
        CAMPO_VOZ to voz,
        CAMPO_VOLUMEN to volumen.toDouble(),
        CAMPO_INTENSIDAD to intensidadColor.name,
        CAMPO_TEMPORIZADOR to temporizador.name,
        CAMPO_CELEBRACION to celebracion.name,
        CAMPO_PALETA to paleta.name,
        CAMPO_TEMA to tema.name
    )

    companion object {
        const val VOLUMEN_POR_DEFECTO = 0.7f

        /** Campo del documento `users/{uid}` donde se guarda el perfil. */
        const val CAMPO_PERFIL = "perfilSensorial"

        private const val CAMPO_ANIMACIONES = "animaciones"
        private const val CAMPO_SONIDO = "sonido"
        private const val CAMPO_VOZ = "voz"
        private const val CAMPO_VOLUMEN = "volumen"
        private const val CAMPO_INTENSIDAD = "intensidadColor"
        private const val CAMPO_TEMPORIZADOR = "temporizador"
        private const val CAMPO_CELEBRACION = "celebracion"
        private const val CAMPO_PALETA = "paleta"
        private const val CAMPO_TEMA = "tema"

        /**
         * Lee el perfil guardado en Firestore. Es tolerante: cualquier campo que falte
         * o tenga un valor no reconocido toma su valor por defecto, de modo que un
         * perfil antiguo o incompleto nunca impide usar la app.
         */
        fun fromMap(map: Map<*, *>?): SensoryProfile {
            if (map == null) return SensoryProfile()
            val defecto = SensoryProfile()
            return SensoryProfile(
                animaciones = map[CAMPO_ANIMACIONES] as? Boolean ?: defecto.animaciones,
                sonido = map[CAMPO_SONIDO] as? Boolean ?: defecto.sonido,
                voz = map[CAMPO_VOZ] as? Boolean ?: defecto.voz,
                volumen = ((map[CAMPO_VOLUMEN] as? Number)?.toFloat() ?: defecto.volumen).coerceIn(0f, 1f),
                intensidadColor = enumDe(map[CAMPO_INTENSIDAD], defecto.intensidadColor),
                temporizador = enumDe(map[CAMPO_TEMPORIZADOR], defecto.temporizador),
                celebracion = enumDe(map[CAMPO_CELEBRACION], defecto.celebracion),
                paleta = enumDe(map[CAMPO_PALETA], defecto.paleta),
                tema = enumDe(map[CAMPO_TEMA], defecto.tema)
            )
        }

        private inline fun <reified T : Enum<T>> enumDe(valor: Any?, defecto: T): T =
            enumValues<T>().firstOrNull { it.name == valor } ?: defecto
    }
}
