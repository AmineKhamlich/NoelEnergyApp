package com.noel.energyapp.network

import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.data.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService{
    // Fem un POST perquè enviem dades sensibles (usuari/pass) al cos de la petició.
    // L'URL final serà: http://172.20.1.46/api/AppUsuari/login
    @POST("Usuari/login")
    // Es fa suspend perque no volem que el mòbil es quedi congelat esperant
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}