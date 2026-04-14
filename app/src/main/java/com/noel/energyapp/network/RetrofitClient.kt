/**
 * FITXER: RetrofitClient.kt
 * CAPA: Xarxa (network)
 *
 * Aquest fitxer crea i configura la instància global de Retrofit, que és la
 * biblioteca que s'encarrega de fer totes les crides HTTP al servidor.
 *
 * Utilitza el patró Singleton (objecte únic compartit a tota l'App) per evitar
 * crear connexions noves innecessàriament. La primera vegada que es demana la
 * instància es construeix, i les vegades posteriors es retorna la mateixa.
 *
 * El convertidor Gson és el que transforma automàticament el JSON de la resposta
 * del servidor en objectes Kotlin que el codi pot utilitzar directament.
 */
package com.noel.energyapp.network

// Importació de la classe principal de Retrofit per construir el client HTTP
import retrofit2.Retrofit
// Importació del convertidor que transforma JSON <-> objectes Kotlin automàticament
import retrofit2.converter.gson.GsonConverterFactory

// 'object' defineix un Singleton: una única instància compartida a tota l'aplicació
object RetrofitClient {

    // Adreça IP i port del servidor Ubuntu on corre l'API ASP.NET Core
    // Totes les crides HTTP de l'App comencen amb aquesta adreça base
    private const val BASE_URL = "http://172.20.1.46/api/"

    // Adreça alternativa per a proves en emulador (apunta al localhost de l'ordinador de desenvolupament)
    // private const val BASE_URL = "http://10.0.2.2:5000/api/"

    // 'by lazy' fa que l'objecte RetrofitClient es construeixi una sola vegada,
    // la primera vegada que s'accedeix a 'instance', i es reutilitzi sempre
    val instance: ApiService by lazy {

        // Es construeix el client Retrofit amb la URL base i el convertidor de JSON
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)                              // Defineix l'adreça del servidor
            .addConverterFactory(GsonConverterFactory.create()) // Afegeix la capacitat de parsejar JSON
            .build()                                        // Construeix l'objecte Retrofit final

        // Genera automàticament la implementació de la interfície ApiService
        // Retrofit crea el codi real de cada funció @GET, @POST, @PUT, etc. en temps d'execució
        retrofit.create(ApiService::class.java)
    }
}