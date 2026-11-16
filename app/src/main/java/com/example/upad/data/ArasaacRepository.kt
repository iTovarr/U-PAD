package com.example.upad.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ArasaacRepository {
    private val arasaacService = Retrofit.Builder()
        .baseUrl("https://api.arasaac.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ArasaacService::class.java)

    /**
     * Busca pictogramas en el idioma de la app, para que el tutor pueda escribir
     * la palabra en su idioma ("eat", "manger"...) y no solo en español.
     */
    suspend fun searchPictograms(query: String, languageCode: String = DEFAULT_LOCALE): List<ArasaacPictogram> {
        return arasaacService.searchPictograms(localeFor(languageCode), query)
    }

    companion object {
        const val DEFAULT_LOCALE = "es"

        /** Idiomas de la app; ARASAAC tiene palabras clave en todos ellos. */
        private val SUPPORTED_LOCALES = setOf("es", "en", "fr", "de", "pt", "ru")

        /** Convierte el idioma de la app ("en", "pt-BR"...) en un locale de ARASAAC; español si no está soportado. */
        internal fun localeFor(languageCode: String): String {
            val idioma = languageCode.trim().lowercase().substringBefore('-').substringBefore('_')
            return if (idioma in SUPPORTED_LOCALES) idioma else DEFAULT_LOCALE
        }
    }
}
