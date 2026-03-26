package com.noel.energyapp.data

// Per rebre la gràfica de consum des de l'API
data class ConsumFiltratDto(
    val data: String, // La data arriba com a String ISO des del C#
    val consum: Double
)

// Per a omplir l'Spinner de comptadors per planta
data class DimCntDto(
    val id: Int,
    val descripcio: String?,
    val tagName: String?,
    val planta: String?
)

