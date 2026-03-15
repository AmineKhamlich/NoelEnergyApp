package com.noel.energyapp.network

import com.noel.energyapp.data.ChangePasswordRequest
import com.noel.energyapp.data.CrearUsuariDto
import com.noel.energyapp.data.GenericResponse
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.data.TancarIncidenciaDto
import com.noel.energyapp.data.UpdatePlantesActivesDto
import com.noel.energyapp.data.UpdateUsuariDto
import com.noel.energyapp.data.UsuariResumDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT


interface ApiService {
    // Fem un POST perquè enviem dades sensibles (usuari/pass) al cos de la petició.
    // L'URL final serà: http://172.20.1.46/api/AppUsuari/login
    @POST("Usuari/login")
    // Es fa suspend perque no volem que el mòbil es quedi congelat esperant
    suspend fun login(@Body request: LoginRequest): Response<UsuariResumDto>

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

    // Per actualitzar massivament les plantes per l'Admin
    @PUT("Planta/estat")
    suspend fun updateEstatMassiu(
        @Header("Authorization") token: String,
        @Body request: UpdatePlantesActivesDto
    ): Response<GenericResponse>

    // --- GESTIÓ D'USUARIS (ADMIN) ---

    // 1. Obtenir tota la llista d'usuaris
    @GET("Usuari")
    suspend fun getUsuaris(@Header("Authorization") token: String): Response<List<UsuariResumDto>>

    // 2. Crear un usuari nou
    @POST("Usuari/crear")
    suspend fun crearUsuari(
        @Header("Authorization") token: String,
        @Body request: CrearUsuariDto
    ): Response<GenericResponse>

    // 3. Actualitzar rol, estat o plantes d'un usuari
    @PUT("Usuari/actualitzar")
    suspend fun actualitzarUsuari(
        @Header("Authorization") token: String,
        @Body request: UpdateUsuariDto
    ): Response<GenericResponse>

    // ==========================================
    // RUTINES D'INCIDÈNCIES (ALARMES)
    // ==========================================

    @GET("incidencia/actives")
    suspend fun getAlarmesActives(@Header("Authorization") token: String): Response<List<IncidenciaVistaDto>>

    @GET("incidencia/historic")
    suspend fun getHistoricAlarmes(@Header("Authorization") token: String): Response<List<IncidenciaVistaDto>>

    // Retorna Response<Unit> perquè l'API només torna un OK i un missatge, sense dades extra.
    // Si fas servir una classe teva com GenericResponse, pots canviar Unit per GenericResponse.
    @POST("incidencia/tancar")
    suspend fun tancarIncidencia(
        @Header("Authorization") token: String,
        @Body request: TancarIncidenciaDto
    ): Response<GenericResponse>
}