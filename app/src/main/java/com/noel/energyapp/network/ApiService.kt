/**
 * FITXER: ApiService.kt
 * CAPA: Xarxa (network)
 *
 * Aquest fitxer defineix el contracte complet de comunicació entre l'aplicació Android
 * i el servidor backend (WConsumsAPI). Cada funció representa un endpoint HTTP diferent
 * de l'API REST, i Retrofit s'encarrega automàticament de construir les peticions,
 * enviar-les al servidor i convertir la resposta JSON en objectes Kotlin.
 *
 * Les crides utilitzen 'suspend' per executar-se de forma asíncrona (sense bloquejar la UI),
 * i 'Response<T>' per poder inspeccionar el codi HTTP retornat (200, 401, 404, etc.).
 */
package com.noel.energyapp.network

// Importació dels models de dades que s'envien i es reben en les crides a l'API
import com.noel.energyapp.data.ChangePasswordRequest
import com.noel.energyapp.data.ConsumFiltratDto
import com.noel.energyapp.data.CrearUsuariDto
import com.noel.energyapp.data.DimCntDto
import com.noel.energyapp.data.FactCntHistorianDto
import com.noel.energyapp.data.FotoResponse
import com.noel.energyapp.data.GenericResponse
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.data.TancarIncidenciaDto
import com.noel.energyapp.data.UpdatePlantesActivesDto
import com.noel.energyapp.data.UpdateUsuariDto
import com.noel.energyapp.data.UsuariResumDto
// Importació de les classes de OkHttp3 per construir peticions multipart (enviament de fotos)
import okhttp3.MultipartBody
import okhttp3.RequestBody
// Importació de les classes de Retrofit per definir les anotacions HTTP
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


// Interfície que declara totes les crides HTTP disponibles al backend
// Retrofit la implementa automàticament en temps d'execució sense que calgui escriure codi manual
interface ApiService {

    // ==========================================================================
    // AUTENTICACIÓ I GESTIÓ D'USUARIS
    // ==========================================================================

    // Autenticació: envia usuari i contrasenya, rep el token JWT i les dades del perfil
    // @POST indica que és una petició HTTP POST (s'envia dades sensibles al cos, no a la URL)
    @POST("Usuari/login")
    // 'suspend' indica que la crida és asíncrona i no bloquejarà el fil principal de la UI
    suspend fun login(@Body request: LoginRequest): Response<UsuariResumDto>

    // Restableix la contrasenya d'un usuari concret (posar-la a 123456)
    // No requereix autenticació perquè és una operació d'administrador via Swagger
    @POST("Usuari/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<Void>

    // Canvia la contrasenya d'un usuari autenticat (login, userId, old i new password)
    // Requereix el token JWT a la capçalera Authorization per verificar la identitat
    @POST("Usuari/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String, // Token JWT en format "Bearer <token>"
        @Body request: ChangePasswordRequest     // Cos de la petició amb les contrasenyes
    ): Response<GenericResponse>

    // Recupera la llista completa d'usuaris del sistema (només accessible per ADMIN)
    @GET("Usuari")
    suspend fun getUsuaris(@Header("Authorization") token: String): Response<List<UsuariResumDto>>

    // Crea un nou usuari al sistema amb les dades proporcionades
    @POST("Usuari/crear")
    suspend fun crearUsuari(
        @Header("Authorization") token: String, // Es necessita ser ADMIN
        @Body request: CrearUsuariDto           // Dades del nou usuari (nick, nom, cognom, rol, plantes)
    ): Response<GenericResponse>

    // Actualitza el rol, l'estat actiu/inactiu o les plantes assignades d'un usuari existent
    @PUT("Usuari/actualitzar")
    suspend fun actualitzarUsuari(
        @Header("Authorization") token: String,  // Es necessita ser ADMIN
        @Body request: UpdateUsuariDto           // Camps a actualitzar (tots opcionals excepte l'ID)
    ): Response<GenericResponse>

    // ==========================================================================
    // PLANTES
    // ==========================================================================

    // Recupera la llista de totes les plantes i el seu estat actiu/inactiu
    @GET("Planta")
    suspend fun getPlantes(@Header("Authorization") token: String): Response<List<PlantaDto>>

    // Actualitza massivament quines plantes estan actives (operació de l'Admin)
    @PUT("Planta/estat")
    suspend fun updateEstatMassiu(
        @Header("Authorization") token: String,         // Es necessita ser ADMIN
        @Body request: UpdatePlantesActivesDto          // Llista d'IDs de plantes que han de quedar actives
    ): Response<GenericResponse>

    // ==========================================================================
    // INCIDÈNCIES (ALARMES)
    // ==========================================================================

    // Recupera la llista d'alarmes actuals no tancades, filtrades per planta si s'especifica
    @GET("incidencia/actives")
    suspend fun getAlarmesActives(
        @Header("Authorization") token: String,
        @Query("plantaId") plantaId: Int? = null // Paràmetre opcional a la URL: ?plantaId=3
    ): Response<List<IncidenciaVistaDto>>

    // Recupera la llista d'alarmes ja tancades (l'historial), filtrades per planta si s'especifica
    @GET("incidencia/historic")
    suspend fun getHistoricAlarmes(
        @Header("Authorization") token: String,
        @Query("plantaId") plantaId: Int? = null // Paràmetre opcional a la URL: ?plantaId=3
    ): Response<List<IncidenciaVistaDto>>

    // Recupera la fotografia d'una alarma tancada, codificada en format Base64
    @GET("incidencia/foto/{alarmaId}")
    suspend fun getFotoAlarma(
        @Header("Authorization") token: String,
        @Path("alarmaId") alarmaId: Int // Substitueix {alarmaId} a la URL per l'ID real
    ): Response<FotoResponse>

    // Tanca una incidència activa enviant una descripció, la solució aplicada i opcionalment una foto
    // @Multipart indica que la petició és de tipus 'form-data' per poder adjuntar un arxiu binari (la foto)
    @Multipart
    @POST("incidencia/tancar")
    suspend fun tancarIncidencia(
        @Header("Authorization") token: String,
        @Part("IdIncidencia") idIncidencia: RequestBody,             // ID de la incidència a tancar
        @Part("DescripcioIncidencia") descripcioIncidencia: RequestBody, // Text descriptiu del problema
        @Part("SolucioAdaptada") solucioAdaptada: RequestBody,       // Text de la solució aplicada
        @Part fotoFile: MultipartBody.Part?                          // Foto opcional (pot ser null)
    ): Response<GenericResponse>

    // ==========================================================================
    // CONSUMS I COMPTADORS
    // ==========================================================================

    // Recupera el consum agregat per dia per a un comptador en un rang de dates (per a la gràfica)
    @GET("FactCntHistorianV2/filtrat")
    suspend fun getConsumFiltrat(
        @Header("Authorization") token: String,
        @Query("idComptador") idComptador: Int,  // ID del comptador del qual volem dades
        @Query("start") start: String,           // Data d'inici en format "yyyy-MM-dd"
        @Query("end") end: String                // Data de fi en format "yyyy-MM-dd"
    ): Response<List<ConsumFiltratDto>>

    // Recupera tots els comptadors associats a una planta concreta
    @GET("DimCnt/planta/{plantaNom}")
    suspend fun getComptadorsPerPlanta(
        @Header("Authorization") token: String,
        @Path("plantaNom") plantaNom: String // Substitueix {plantaNom} a la URL pel nom real de la planta
    ): Response<List<DimCntDto>>

    // Recupera el valor de consum en temps real (live) per a un comptador pel seu tagName de SCADA
    @GET("FactCntHistorianV2/live")
    suspend fun getLiveValue(
        @Header("Authorization") token: String,
        @Query("tagName") tagName: String // Identifador únic del comptador al sistema SCADA
    ): Response<Double>

    // Recupera tots els registres horaris d'un comptador per a un dia concret
    @GET("FactCntHistorianV2/dia")
    suspend fun getRegistresPerDia(
        @Header("Authorization") token: String,
        @Query("idContador") idComptador: Int, // ID del comptador
        @Query("data") data: String            // Data en format "yyyy-MM-dd"
    ): Response<List<FactCntHistorianDto>>

    // Corregeix o restableix el valor d'un registre horari concret (escriu a ValorDifMod)
    // Si 'nouValor' és null, s'elimina la correcció i es torna al valor original
    @POST("FactCntHistorianV2/corregir")
    suspend fun corregirValor(
        @Header("Authorization") token: String,
        @Query("idHistorian") idHistorian: Int, // ID del registre historian a modificar
        @Query("nouValor") nouValor: Float?      // Nou valor en m³. Null per desfer la correcció
    ): Response<GenericResponse>

}