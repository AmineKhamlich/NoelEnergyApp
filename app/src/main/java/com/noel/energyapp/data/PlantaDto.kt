/**
 * FITXER: PlantaDto.kt
 * CAPA: Dades (data)
 *
 * Aquest fitxer defineix els models de dades relacionats amb les plantes
 * del sistema de gestió energètica.
 *
 * Conté dos models:
 * - 'PlantaDto': Representa una planta industrial amb el seu estat d'activació.
 *   S'utilitza en el Dashboard per llistar les plantes disponibles i a la pantalla
 *   d'administrador per activar-les o desactivar-les.
 * - 'UpdatePlantesActivesDto': Model que s'envia a l'API per actualitzar massivament
 *   quines plantes estan actives, passant la llista completa d'IDs actius.
 */
package com.noel.energyapp.data

// Model que representa una planta industrial al sistema
// Ha de coincidir exactament amb el PlantaDto que retorna l'API de C# (noms snake_case per coincidència)
data class PlantaDto(
    val id_planta: Int,    // Identificador únic de la planta a la base de dades
    val nom_planta: String, // Nom llegible de la planta (ex: "Noel-1", "Planta Estació Depuradora")
    val activa: Boolean    // Indica si la planta és visible al Dashboard dels usuaris
)

// Model que s'envia a l'API per actualitzar de cop l'estat de totes les plantes
// L'API desactiva totes les que NO estan a la llista i activa les que sí hi són
data class UpdatePlantesActivesDto(
    val idsPlantesActives: List<Int> // Llista d'IDs de les plantes que han de quedar en estat actiu
)