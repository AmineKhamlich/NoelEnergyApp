package com.noel.energyapp.network

import com.noel.energyapp.data.ChangePasswordRequest
import com.noel.energyapp.data.GenericResponse
import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.data.LoginResponse
import com.noel.energyapp.data.PlantaDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    // Fem un POST perquè enviem dades sensibles (usuari/pass) al cos de la petició.
    // L'URL final serà: http://172.20.1.46/api/AppUsuari/login
    @POST("Usuari/login")
    // Es fa suspend perque no volem que el mòbil es quedi congelat esperant
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Utilitzem @Header("Authorization") per enviar el Token: "Bearer <token>"
    @GET("Planta")
    suspend fun getPlantes(@Header("Authorization") token: String): Response<List<PlantaDto>>

    //
    @POST("Usuari/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<Void>

    //  Endpoint per canviar la contrasenya obligatòria
    @POST("Usuari/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<GenericResponse>
}