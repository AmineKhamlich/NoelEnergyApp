package com.noel.energyapp.data

// 1. L'objecte principal que ens serveix pel Login i per la Llista d'Usuaris
data class UsuariResumDto(
    val id: Int,
    val nomUsuari: String,
    val nom: String,
    val cognom: String,
    val rol: String,
    val idRol: Int, // AFEGEIX AIXÒ: El necessitem per amagar botons segons el rol!
    val actiu: Boolean?,
    val canviPasswordObligatori: Boolean,
    val token: String? = null,
    val plantesAssignadesText: String? = null,
    val idsPlantes: List<Int> = emptyList() // Millor llista buida que null
)

// 2. DTO per al Login
data class LoginRequest(
    val username: String,
    val password: String
)

// 3. DTO per al canvi de contrasenya obligatori (El que t'estava donant l'error de redeclaració)
data class ChangePasswordRequest(
    val userId: Int,
    val oldPassword: String,
    val newPassword: String
)

// 4. DTO per crear usuaris (Admin)
data class CrearUsuariDto(
    val username: String,
    val nom: String,
    val cognom: String,
    val rol: String,
    val idsPlantes: List<Int> = emptyList()
)

// 5. DTO per actualitzar dades (Admin)
data class UpdateUsuariDto(
    val idUsuari: Int,
    val nouRol: String? = null,
    val actiu: Boolean? = null,
    val canviPasswordObligatori: Boolean? = null,
    val idsPlantes: List<Int>? = null
)

// 6. Resposta genèrica per missatges del servidor
data class GenericResponse(
    val message: String
)