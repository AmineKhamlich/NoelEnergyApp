package com.noel.energyapp.data

// Aquest DTO ha de coincidir amb el ChangePasswordDto de C#
data class ChangePasswordRequest(
    val userId: Int,
    val oldPassword: String,
    val newPassword: String
)

// Aquesta classe ens serveix per llegir el missatge "Contrasenya actualitzada" que retorna C#
data class GenericResponse(
    val message: String
)