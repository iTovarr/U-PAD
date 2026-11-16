package com.example.upad.data

import retrofit2.http.GET
import retrofit2.http.Path

interface ArasaacService {
    /** Busca pictogramas por palabra clave (ej: "comer") en el idioma indicado (ej: "es"). */
    @GET("api/pictograms/{locale}/search/{query}")
    suspend fun searchPictograms(
        @Path("locale") locale: String,
        @Path("query") query: String
    ): List<ArasaacPictogram>
}
