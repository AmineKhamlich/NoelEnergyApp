/**
 * FITXER: usuariModels.kt
 * CAPA: Dades (data)
 *
 * Aquest fitxer conté tots els models de dades relacionats amb els usuaris
 * del sistema: autenticació, gestió d'accés i operacions d'administrador.
 *
 * Inclou:
 * - 'UsuariResumDto': La resposta completa del login (token + perfil + plantes).
 * - 'LoginRequest': Les credencials que s'envien per autenticar l'usuari.
 * - 'ChangePasswordRequest': Les dades per al canvi de contrasenya segur.
 * - 'CrearUsuariDto': Les dades per crear un nou treballador al sistema.
 * - 'UpdateUsuariDto': Les dades per modificar qualsevol aspecte d'un usuari.
 * - 'GenericResponse': La resposta genèrica de l'API per a operacions d'escriptura.
 */
package com.noel.energyapp.data

// Model de resposta del login: conté tota la informació del perfil de l'usuari autenticat
// S'utilitza tant per al Login com per a la llista d'usuaris de la pantalla d'administració
data class UsuariResumDto(
    val id: Int,                            // Identificador únic de l'usuari a la base de dades
    val nomUsuari: String,                  // Nom d'usuari de login (nick, ex: "joanpetit")
    val nom: String,                        // Nom real de pila (ex: "Joan")
    val cognom: String,                     // Cognom real (ex: "Petit")
    val rol: String,                        // Rol textual del sistema: "ADMIN", "SUPERVISOR" o "TECNIC"
    val idRol: Int,                         // ID numèric del rol (1=ADMIN, 2=SUPERVISOR, 3=TECNIC) per comparar permisos
    val actiu: Boolean?,                    // Indica si el compte d'usuari està habilitat (pot ser null per compatibilitat)
    val canviPasswordObligatori: Boolean,   // Si és true, l'usuari ha de canviar la contrasenya en el proper login
    val token: String? = null,              // Token JWT per autenticar les peticions a l'API (present al login, absent a la llista)
    val plantesAssignadesText: String? = null, // Text amb els noms de les plantes assignades (ex: "Noel-1, Noel-7")
    val idsPlantes: List<Int> = emptyList() // Llista d'IDs numèrics de les plantes assignades (per al filtratge)
)

// Model que s'envia a l'API per iniciar sessió
// Conté les credencials de l'usuari en text pla (la connexió és HTTP, però es fa a la xarxa local interna)
data class LoginRequest(
    val username: String, // Nom d'usuari de login
    val password: String  // Contrasenya en text pla
)

// Model que s'envia quan un usuari vol canviar la seva contrasenya
// Requereix la contrasenya actual per verificar la identitat abans de fer el canvi
data class ChangePasswordRequest(
    val userId: Int,      // ID de l'usuari que vol fer el canvi (obtingut de la sessió)
    val oldPassword: String, // Contrasenya actual per verificar que és qui diu ser
    val newPassword: String  // Nova contrasenya que es vol establir
)

// Model que l'administrador envia per crear un nou treballador al sistema
// La contrasenya inicial sempre serà "123456" (l'API la estableix per defecte amb canvi obligatori)
data class CrearUsuariDto(
    val username: String,         // Nick d'accés del nou usuari (ha de ser únic al sistema)
    val nom: String,              // Nom de pila del nou treballador
    val cognom: String,           // Cognom del nou treballador
    val rol: String,              // Rol assignat al nou usuari (ex: "TECNIC")
    val idsPlantes: List<Int> = emptyList() // IDs de les plantes a les quals tindrà accés
)

// Model que l'administrador envia per modificar dades d'un usuari existent
// Tots els camps (excepte idUsuari) són opcionals: s'envia null per als que no es volen modificar
data class UpdateUsuariDto(
    val idUsuari: Int,                        // ID de l'usuari que es vol modificar (obligatori)
    val nouRol: String? = null,              // Nou rol a assignar (null si no es vol canviar)
    val actiu: Boolean? = null,              // Nou estat actiu/inactiu (null si no es vol canviar)
    val canviPasswordObligatori: Boolean? = null, // Forçar o treure l'obligació de canviar contrasenya
    val idsPlantes: List<Int>? = null        // Nova llista de plantes assignades (null si no es vol canviar)
)

// Model limitat per assignar plantes sense exposar canvis de rol o estat al supervisor
data class UpdateUsuariPlantesDto(
    val idUsuari: Int,
    val idsPlantes: List<Int>
)

// Model de resposta genèric de l'API per a les operacions que no retornen dades concretes
// S'utilitza per confirmar que l'operació ha anat bé i mostrar el missatge al usuari
data class GenericResponse(
    val message: String // Missatge de confirmació o d'error retornat pel servidor
)
