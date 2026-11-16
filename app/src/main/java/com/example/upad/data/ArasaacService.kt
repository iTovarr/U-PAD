package com.example.upad.data

import retrofit2.http.GET
import retrofit2.http.Path

interface ArasaacService {
    // Busca pictogramas por palabra clave (ej: "comer")
    @GET("api/pictograms/es/search/{query}")
    suspend fun searchPictograms(@Path("query") query: String): List<ArasaacPictogram>
}