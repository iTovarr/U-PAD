package com.example.upad.capturas

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import java.io.File

/**
 * Guarda la pantalla que está mostrando la regla de Compose como PNG en la carpeta
 * indicada por la propiedad del sistema `upad.capturas` (docs/capturas).
 */
fun ComposeContentTestRule.guardarCaptura(nombre: String) {
    waitForIdle()
    val carpeta = File(System.getProperty("upad.capturas") ?: "build/capturas").apply { mkdirs() }
    val imagen = onRoot().captureToImage().asAndroidBitmap()
    File(carpeta, "$nombre.png").outputStream().use { salida ->
        imagen.compress(Bitmap.CompressFormat.PNG, 100, salida)
    }
}
