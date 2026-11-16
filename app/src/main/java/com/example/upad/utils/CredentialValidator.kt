package com.example.upad.utils

/**
 * Validaciones de credenciales previas a llamar a Firebase Auth.
 *
 * Es Kotlin puro (sin dependencias de Android) para poder probarse con tests unitarios.
 */
object CredentialValidator {

    /** Longitud mínima exigida al crear una cuenta nueva. */
    const val MIN_PASSWORD_LENGTH = 8

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /** El email se evalúa sin espacios al inicio ni al final. */
    fun esEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    /**
     * La contraseña se evalúa tal cual la escribió el usuario: los espacios son
     * caracteres válidos y no deben recortarse.
     */
    fun esPasswordValida(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH
}
