package com.noel.energyapp.data

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val id: Int,
    val nom: String,
    val rol: String,
    val actiu: Boolean,
    val canviPasswordObligatori: Boolean,
    val token: String, // Aquest és el Token JWT vital per a la seguretat
    val idsPlantes: List<Int> // La llista de plantes que té assignades l'usuari
)