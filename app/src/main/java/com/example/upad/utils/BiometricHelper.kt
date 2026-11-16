package com.example.upad.utils // O com.example.upad.data según tu estructura

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object BiometricHelper {

    fun esBiometriaDisponible(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun lanzarLectorHuella(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Huella no reconocida.")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso rápido para Padres")
            .setSubtitle("Coloca tu huella para continuar")
            .setNegativeButtonText("Usar Contraseña")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // --- 🔥 NUEVA FUNCIÓN AGREGADA PARA EL BLOQUEO/DESBLOQUEO REMOTO CON HUELLA ---
    fun cambiarEstadoKioscoConHuella(
        activity: FragmentActivity,
        idDispositivoNiño: String,
        debeBloquear: Boolean, // true = Bloquear, false = Desbloquear
        onResultado: (Boolean) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // 🔓/🔒 Huella correcta -> Cambiamos el valor en Firebase Firestore
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("dispositivos_niños")
                    .document(idDispositivoNiño)
                    .set(mapOf("kioscoActivo" to debeBloquear), SetOptions.merge())
                    .addOnSuccessListener {
                        val mensaje = if (debeBloquear) "¡Dispositivo infantil Bloqueado! 🔒" else "¡Dispositivo infantil Liberado! 🔓"
                        Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show()
                        onResultado(true)
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, "Error de conexión con Firebase", Toast.LENGTH_SHORT).show()
                        onResultado(false)
                    }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity, "Error de huella: $errString", Toast.LENGTH_SHORT).show()
                onResultado(false)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(activity, "Huella no reconocida.", Toast.LENGTH_SHORT).show()
                onResultado(false)
            }
        })

        // Personalizamos los textos dinámicamente según la acción elegida por el padre
        val tituloAccion = if (debeBloquear) "Confirmar Bloqueo Infantil" else "Confirmar Desbloqueo"
        val subtituloAccion = if (debeBloquear) "Escanea tu huella para congelar el dispositivo" else "Escanea tu huella para liberar el dispositivo"

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(tituloAccion)
            .setSubtitle(subtituloAccion)
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}