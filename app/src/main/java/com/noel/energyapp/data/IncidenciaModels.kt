/**
 * FITXER: IncidenciaModels.kt
 * CAPA: Dades (data)
 *
 * Aquest fitxer agrupa tots els models de dades relacionats amb les incidències
 * (alarmes) del sistema de monitorització de consums d'energia.
 *
 * Conté tres classes diferenciades:
 * - 'IncidenciaVistaDto': El model principal que representa una alarma completa,
 *   tant si és activa com si ja ha estat tancada (historial). Inclou tots els camps
 *   possibles que l'API pot retornar en qualsevol dels dos estats.
 * - 'TancarIncidenciaDto': El model que s'envia a l'API quan un tècnic tanca
 *   una alarma activa amb la seva descripció i la solució aplicada.
 * - 'FotoResponse': La resposta de l'API quan es demana la fotografia d'una
 *   alarma tancada (retorna la imatge codificada en Base64).
 */
package com.noel.energyapp.data

// Model principal que representa una incidència/alarma, activa o tancada
// Tots els camps opcionals (nullable) no sempre arriben de l'API: depèn de si l'alarma és activa o tancada
data class IncidenciaVistaDto(
    val id: Int,                            // Identificador únic de la incidència a la base de dades
    val dataCreacio: String? = null,        // Data i hora en que es va generar l'alarma (ISO 8601)
    val dataTancament: String? = null,      // Data i hora en que el tècnic va tancar l'alarma (null si és activa)
    val gravetat: String,                   // Nivell de gravetat: "AVÍS (H)" o "CRÍTIC (HH)"
    val estat: String,                      // Estat actual: "ACTIVA" o "TANCADA"
    val ubicacio: String,                   // Ubicació física del comptador on s'ha detectat l'anomalia
    val descripcioComptador: String? = null, // Nom descriptiu del comptador afectat
    val limitH: Int? = null,                // Límit de consum en m³ per a l'avís normal (H)
    val limitHH: Int? = null,               // Límit de consum en m³ per a l'avís crític (HH)
    val tecnicTancament: String? = null,    // Nom del tècnic que va tancar l'alarma (null si és activa)
    val tempsTranscorregut: String = "",    // Temps que porta activa o que va durar l'alarma (ex: "3h 20min")
    val consumDiaAlarma: Double = 0.0,      // Consum total registrat el dia que es va generar l'alarma (m³)
    val descripcio: String? = null,         // Descripció del problema introduïda pel tècnic al tancar
    val descripcioSolucio: String? = null,  // Solució aplicada introduïda pel tècnic al tancar
    val foto: String? = null,               // Nom del fitxer de la foto presa al tancar (null si no n'hi ha)
    val comptador: String = "",             // Identificador curt del comptador (ex: "CNT-01")
    val detallAlarma: String = "",          // Detall addicional de l'alarma (ex: consum detectat)
    val horaAvisH: String? = null,          // Hora en que es va superar el límit H per primera vegada
    val horaCriticHH: String? = null,       // Hora en que es va superar el límit HH (crític)
    val consumRealAvui: Double = 0.0        // Consum acumulat avui en el moment de l'alarma (m³)
)

// Model lleuger que l'API retorna per construir el text de la notificacio Android
data class NotificacioIncidenciaDto(
    val id: Int,
    val idDimCnt: Int,
    val titol: String = "",
    val missatge: String = "",
    val gravetat: String = "",
    val ubicacio: String = "",
    val comptador: String = "",
    val tagName: String = "",
    val detallAlarma: String = "",
    val consumRealAvui: Double = 0.0,
    val limitH: Int? = null,
    val limitHH: Int? = null,
    val dataCreacio: String? = null,
    val horaAvisH: String? = null,
    val horaCriticHH: String? = null,
    val nivellActual: Int = 0
)

// Model que s'envia al servidor quan un tècnic vol tancar una incidència activa
// Conté la informació del tancament: qui la va resoldre, com i amb una foto opcional
data class TancarIncidenciaDto(
    val idIncidencia: Int,           // ID de l'alarma que es vol tancar
    val descripcioIncidencia: String, // Descripció del problema observat in situ
    val solucioAdaptada: String,     // Explicació de la solució que s'ha aplicat per resoldre-la
    val fotoBase64: String? = null   // Foto de l'evidència codificada en Base64 (opcional)
)

// Model de la resposta de l'API quan es demana la foto d'una alarma de l'historial
// La foto arriba codificada en Base64 per poder-la mostrar directament a la UI sense guardar-la
data class FotoResponse(
    val base64: String // Contingut binari de la imatge codificat en text Base64
)
