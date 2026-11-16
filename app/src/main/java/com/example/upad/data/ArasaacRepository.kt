package com.example.upad.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ArasaacRepository {
    private val arasaacService = Retrofit.Builder()
        .baseUrl("https://api.arasaac.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ArasaacService::class.java)

    suspend fun searchPictograms(query: String): List<ArasaacPictogram> {
        return arasaacService.searchPictograms(query)
    }
}
