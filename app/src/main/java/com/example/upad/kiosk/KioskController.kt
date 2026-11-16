package com.example.upad.kiosk

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager

private const val TAG = "KioskController"

/**
 * Activa o desactiva el modo kiosco del dispositivo del niño: pantalla siempre
 * encendida, barras del sistema ocultas y fijación de la app en primer plano.
 *
 * Sin privilegios de Device Owner, `startLockTask()` equivale a la fijación de
 * pantalla estándar de Android, que el usuario puede abandonar con el gesto del sistema.
 */
class KioskController(private val activity: Activity) {

    fun aplicar(activar: Boolean) {
        if (activar) bloquear() else liberar()
    }

    private fun bloquear() {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
        try {
            activity.startLockTask()
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo fijar la app en pantalla", e)
        }
    }

    private fun liberar() {
        val window = activity.window
        try {
            activity.stopLockTask()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo liberar la app de la pantalla", e)
        }
    }
}
