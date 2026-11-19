package com.example.upad.child

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.upad.R
import com.example.upad.data.SensoryProfile
import java.util.Locale

private const val TAG = "ChildAudio"

/**
 * Voz y sonidos del modo niño. Respeta el perfil sensorial: si el tutor desactivó
 * la voz o los sonidos, las llamadas no hacen nada, y ambos usan su volumen.
 */
class ChildAudio(context: Context, private val locale: Locale) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ttsListo = false
    private var pendiente: Pair<String, Float>? = null

    init {
        tts = TextToSpeech(appContext) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                val resultado = tts?.setLanguage(locale)
                ttsListo = resultado != TextToSpeech.LANG_MISSING_DATA &&
                    resultado != TextToSpeech.LANG_NOT_SUPPORTED
                // Una voz algo más lenta de lo normal se entiende mejor.
                tts?.setSpeechRate(0.9f)
                pendiente?.let { (texto, volumen) -> hablar(texto, volumen) }
                pendiente = null
            } else {
                Log.w(TAG, "El sintetizador de voz no está disponible")
            }
        }
    }

    /** Lee el texto en voz alta si el perfil lo permite. */
    fun leer(texto: String, perfil: SensoryProfile) {
        if (!perfil.voz || texto.isBlank()) return
        if (ttsListo) hablar(texto, perfil.volumen) else pendiente = texto to perfil.volumen
    }

    private fun hablar(texto: String, volumen: Float) {
        val parametros = Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volumen) }
        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, parametros, "upad_tarea")
    }

    /** Reproduce el sonido suave de tarea completada si el perfil lo permite. */
    fun sonarTareaCompletada(perfil: SensoryProfile) {
        if (!perfil.sonido) return
        try {
            MediaPlayer.create(appContext, R.raw.tarea_completada)?.apply {
                setVolume(perfil.volumen, perfil.volumen)
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo reproducir el sonido", e)
        }
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

/** Crea un [ChildAudio] en el idioma de la app y lo libera al salir de la pantalla. */
@Composable
fun rememberChildAudio(): ChildAudio {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val audio = remember(locale) { ChildAudio(context, locale) }
    DisposableEffect(audio) {
        onDispose { audio.liberar() }
    }
    return audio
}
