package com.noel.energyapp.data

// Aquest el rebrem quan demanem la llista de tots els usuaris
data class UsuariResumDto(
    val id: Int,
    val nomUsuari: String,
    val nom: String,
    val cognom: String,
    val rol: String,
    val actiu: Boolean?,
    val canviPasswordObligatori: Boolean,
    val plantesAssignadesText: String?,
    val idsPlantes: List<Int>?
)

// DTO per enviar a C# quan creem un usuari nou
data class CrearUsuariDto(
    val username: String,
    val nom: String,
    val cognom: String,
    val rol: String,
    val idsPlantes: List<Int>? = emptyList()
)

// DTO per enviar a C# quan canviem el rol, l'estat o les plantes d'un usuari
data class UpdateUsuariDto(
    val idUsuari: Int,
    val nouRol: String?,
    val actiu: Boolean?,
    val canviPasswordObligatori: Boolean?,
    val idsPlantes: List<Int>?
)