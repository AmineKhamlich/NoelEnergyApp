package com.noel.energyapp.data

// El "mirall" del que ens envia l'API quan demanem les llistes d'alarmes
data class IncidenciaVistaDto(
    val id: Int,
    val dataCreacio: String,
    val gravetat: String,
    val estat: String,
    val comptador: String,
    val ubicacio: String,
    val detallAlarma: String,
    val horaAvisH: String?,
    val horaCriticHH: String?,
    val consumRealAvui: Double,
    val limitH: Int?,
    val limitHH: Int?,
    val dataTancament: String?,
    val tempsTranscorregut: String
)

// El "paquet" que enviarem a l'API per tancar una incidència
data class TancarIncidenciaDto(
    val idIncidencia: Int,
    val descripcioIncidencia: String,
    val solucioAdaptada: String,
    val fotoBase64: String? = null // Opcional per si no hi ha foto
)