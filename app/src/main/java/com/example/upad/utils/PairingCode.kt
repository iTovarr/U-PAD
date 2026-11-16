package com.example.upad.utils

import java.security.SecureRandom

/**
 * Reglas del código de 6 dígitos con el que un tutor vincula el dispositivo del niño.
 *
 * Es Kotlin puro (sin dependencias de Android ni Firebase) para poder probarse con tests unitarios.
 */
object PairingCode {

    const val ESTADO_ESPERANDO = "esperando"
    const val ESTADO_ENLAZADO = "enlazado"

    /** Tiempo durante el que un código recién generado puede canjearse. */
    const val VALIDEZ_MS = 15 * 60 * 1000L

    private const val MINIMO = 100_000
    private const val CANTIDAD = 900_000

    private val random = SecureRandom()

    /** Genera un código de 6 dígitos con un generador criptográficamente seguro. */
    fun generar(): String = (MINIMO + random.nextInt(CANTIDAD)).toString()

    /**
     * Un código sin fecha de creación se considera expirado: así los códigos
     * antiguos (creados antes de que existiera la caducidad) dejan de ser válidos.
     */
    fun haExpirado(creadoEnMillis: Long?, ahoraMillis: Long): Boolean {
        if (creadoEnMillis == null) return true
        return ahoraMillis - creadoEnMillis > VALIDEZ_MS
    }

    /** Solo puede canjearse un código que nadie ha usado todavía y que sigue vigente. */
    fun esCanjeable(estado: String?, creadoEnMillis: Long?, ahoraMillis: Long): Boolean =
        estado == ESTADO_ESPERANDO && !haExpirado(creadoEnMillis, ahoraMillis)
}
