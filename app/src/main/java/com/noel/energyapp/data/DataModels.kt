/**
 * FITXER: DataModels.kt
 * CAPA: Dades (data)
 *
 * Aquest fitxer conté les classes de dades relacionades amb els consums
 * i els comptadors del sistema SCADA/DW (Data Warehouse).
 *
 * Cada 'data class' és un "mirall" exacte del JSON que retorna l'API del backend:
 * els noms dels camps han de coincidir amb la serialització camelCase que fa
 * ASP.NET Core perquè Gson els pugui deserialitzar automàticament.
 *
 * Aquestes classes s'utilitzen a les pantalles de gràfiques, registres i
 * consums en temps real.
 */
package com.noel.energyapp.data

// Model de dades per a la gràfica de consum: un punt de la gràfica per cada dia
// Representa el consum total acumulat d'un count a una data concreta
data class ConsumFiltratDto(
    val data: String,   // La data arriba com a String ISO 8601 des del C# (ex: "2026-04-14")
    val consum: Double  // Consum total del dia en m³ (agregat per l'stored procedure)
)

// Model de dades per a la llista de comptadors disponibles en una planta
// Cada element representa un sensor físic de mesura al sistema SCADA
data class DimCntDto(
    val id: Int,           // Identificador únic del comptador a la base de dades DW
    val descripcio: String?, // Descripció llegible del comptador (ex: "Comptador Principal Noel-1")
    val tagName: String?,   // Nom intern del tag al sistema SCADA (ex: "NOEL1_CNT_PRINCIPAL")
    val planta: String?     // Nom de la planta a la qual pertany aquest comptador
)

// Model de dades per a cada registre horari individual del comptador
// Representa una fila de la taula FACT_CNT_HISTORIAN_V2 de la base de dades DW
data class FactCntHistorianDto(
    val id: Int,                 // Identificador únic del registre historian
    val valorDiferencial: Double, // Valor de consum original registrat automàticament pel sistema
    val valorDifMod: Double?,    // Valor corregit manualment (null si no s'ha modificat mai)
    val fechaFin: String? = null // Hora de fi del registre (ex: "2026-04-14T10:00:00")
)
