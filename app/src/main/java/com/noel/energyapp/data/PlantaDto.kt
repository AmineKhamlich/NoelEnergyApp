package com.noel.energyapp.data

// Aquest model ha de coincidir amb el PlantaDto de C#
data class PlantaDto(
    val id_planta: Int,
    val nom_planta: String,
    val activa: Boolean
)

// Aquest DTO coincideix amb el UpdatePlantesActivesDto del C#
data class UpdatePlantesActivesDto(
    val idsPlantesActives: List<Int>
)