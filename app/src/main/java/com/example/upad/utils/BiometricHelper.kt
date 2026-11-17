package com.example.upad.utils // O com.example.upad.data según tu estructura

import com.example.upad.R
import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.upad.data.DeviceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
                onError(activity.getString(R.string.biometric_not_recognized))
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_parent_title))
            .setSubtitle(activity.getString(R.string.biometric_parent_subtitle))
            .setNegativeButtonText(activity.getString(R.string.biometric_use_password))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // --- 🔥 NUEVA FUNCIÓN AGREGADA PARA EL BLOQUEO/DESBLOQUEO REMOTO CON HUELLA ---
    fun cambiarEstadoKioscoConHuella(
        activity: FragmentActivity,
        idDispositivoNiño: String,
        debeBloquear: Boolean, // true = Bloquear, false = Desbloquear
        deviceRepository: DeviceRepository,
        onResultado: (Boolean) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // 🔓/🔒 Huella correcta -> se cambia el bloqueo remoto del dispositivo
                activity.lifecycleScope.launch {
                    try {
                        deviceRepository.setKioskActive(idDispositivoNiño, debeBloquear)
                        val mensaje = activity.getString(
                            if (debeBloquear) R.string.biometric_device_locked else R.string.biometric_device_unlocked
                        )
                        Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show()
                        onResultado(true)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Toast.makeText(activity, activity.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
                        onResultado(false)
                    }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity, activity.getString(R.string.biometric_error, errString), Toast.LENGTH_SHORT).show()
                onResultado(false)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(activity, activity.getString(R.string.biometric_not_recognized), Toast.LENGTH_SHORT).show()
                onResultado(false)
            }
        })

        // Personalizamos los textos dinámicamente según la acción elegida por el padre
        val tituloAccion = activity.getString(
            if (debeBloquear) R.string.biometric_lock_title else R.string.biometric_unlock_title
        )
        val subtituloAccion = activity.getString(
            if (debeBloquear) R.string.biometric_lock_subtitle else R.string.biometric_unlock_subtitle
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(tituloAccion)
            .setSubtitle(subtituloAccion)
            .setNegativeButtonText(activity.getString(R.string.action_cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}