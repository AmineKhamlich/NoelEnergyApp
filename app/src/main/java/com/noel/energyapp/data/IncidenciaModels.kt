package com.noel.energyapp.data

// El "mirall" del que ens envia l'API quan demanem les llistes d'alarmes
data class IncidenciaVistaDto(
    val id: Int,
    // Camps de l'històric
    val dataCreacio: String? = null,
    val dataTancament: String? = null,
    val gravetat: String,
    val estat: String,
    val ubicacio: String,
    val descripcioComptador: String? = null,
    val limitH: Int? = null,
    val limitHH: Int? = null,
    val tecnicTancament: String? = null,
    val tempsTranscorregut: String = "",
    val consumDiaAlarma: Double = 0.0,
    // Camps del detall (descripció, solució i foto de quan es va tancar)
    val descripcio: String? = null,
    val descripcioSolucio: String? = null,
    val foto: String? = null,
    // Camps de les alarmes actives (mantinguts per AlarmaCard)
    val comptador: String = "",
    val detallAlarma: String = "",
    val horaAvisH: String? = null,
    val horaCriticHH: String? = null,
    val consumRealAvui: Double = 0.0
)


// El "paquet" que enviarem a l'API per tancar una incidència
data class TancarIncidenciaDto(
    val idIncidencia: Int,
    val descripcioIncidencia: String,
    val solucioAdaptada: String,
    val fotoBase64: String? = null // Opcional per si no hi ha foto
)

// Resposta de l'API quan demanem la foto d'una alarma històrica
data class FotoResponse(
    val base64: String
)