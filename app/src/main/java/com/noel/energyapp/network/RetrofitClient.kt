package com.noel.energyapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // LA IP DEL SERVIDOR UBUNTU
    private const val BASE_URL = "http://172.20.1.46/api/"
    //private const val BASE_URL = "http://10.0.2.2:5000/api/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Converteix JSON a classes de Kotlin automàticament
            .build()

        retrofit.create(ApiService::class.java)
    }
}