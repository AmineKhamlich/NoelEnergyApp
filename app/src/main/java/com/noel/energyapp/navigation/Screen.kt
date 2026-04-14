/**
 * FITXER: Screen.kt
 * CAPA: Navegació (navigation)
 *
 * Aquest fitxer centralitza totes les rutes de navegació de l'aplicació.
 * Cada pantalla té el seu identificador de ruta definit aquí, de manera que
 * si en algún moment es vol canviar el nom d'una ruta, només cal modificar
 * aquest fitxer en lloc de buscar-ho per tot el projecte.
 *
 * Les rutes que inclouen paràmetres (com plantaId o alarmaId) disposen d'una
 * funció 'createRoute()' que construeix la URL final amb els valors reals,
 * evitant errors tipogràfics en la concatenació de strings.
 *
 * Utilitza el patró 'sealed class' perquè només es puguin crear subclasses
 * definides dins d'aquest fitxer, cosa que garanteix que el NavHost coneix
 * en temps de compilació totes les pantalles possibles.
 */
package com.noel.energyapp.navigation

// Classe segellada (sealed class) que agrupa totes les rutes de navegació
// Cada objecte fill representa una pantalla diferent de l'aplicació
sealed class Screen(val route: String) {

    // Pantalla d'inici de sessió (usuari i contrasenya)
    object Login : Screen("login")

    // Pantalla principal amb la llista de plantes de l'usuari
    object Dashboard : Screen("dashboard")

    // Pantalla de detall d'una planta, que requereix l'ID i el nom per a mostrar dades correctes
    object PlantaDetail : Screen("planta_detail/{plantaId}/{plantaNom}") {
        // Construeix la URL substituint els marcadors {plantaId} i {plantaNom} pels valors reals
        fun createRoute(plantaId: Int, plantaNom: String) = "planta_detail/$plantaId/$plantaNom"
    }

    // Pantalla d'administració per activar/desactivar plantes (només ADMIN)
    object GestioPlantes : Screen("gestio_plantes")

    // Pantalla d'administració per gestionar usuaris, rols i plantes assignades (només ADMIN)
    object GestioUsuaris : Screen("gestio_usuaris")

    // Pantalla per tancar una incidència activa, requereix l'ID de la incidència
    object TancarIncidencia : Screen("tancar_incidencia/{incidenciaId}") {
        // Construeix la URL final amb l'ID real de la incidència: ex. "tancar_incidencia/45"
        fun createRoute(id: Int) = "tancar_incidencia/$id"
    }

    // Pantalla de canvi de contrasenya obligatori (apareix quan 'canviPasswordObligatori' és true)
    object ChangePassword : Screen("change_password")

    // Pantalla de detall d'una alarma ja tancada de l'historial
    object HistoricAlarmaDetail : Screen("historic_alarma_detail/{alarmaId}") {
        // Construeix la URL amb l'ID de l'alarma: ex. "historic_alarma_detail/12"
        fun createRoute(alarmaId: Int) = "historic_alarma_detail/$alarmaId"
    }

    // Pantalla amb la llista d'alarmes actives d'una planta concreta
    object AlarmesActives : Screen("alarmes_actives/{plantaId}") {
        // Construeix la URL amb l'ID de la planta: ex. "alarmes_actives/3"
        fun createRoute(plantaId: Int) = "alarmes_actives/$plantaId"
    }

    // Pantalla amb l'historial d'alarmes tancades d'una planta concreta
    object AlarmesHistoric : Screen("alarmes_historic/{plantaId}") {
        // Construeix la URL amb l'ID de la planta: ex. "alarmes_historic/3"
        fun createRoute(plantaId: Int) = "alarmes_historic/$plantaId"
    }

    // Pantalla amb la gràfica de barres de consums diaris d'una planta
    object ConsumGrafica : Screen("consum_grafica/{plantaId}/{plantaNom}") {
        // Construeix la URL amb l'ID i el nom de la planta
        fun createRoute(plantaId: Int, plantaNom: String) = "consum_grafica/$plantaId/$plantaNom"
    }

    // Pantalla amb el valor de consum en temps real (live dashboard de comptadors)
    object ConsumsActuals : Screen("consums_actuals/{plantaNom}") {
        // Construeix la URL amb el nom de la planta: ex. "consums_actuals/Noel-1"
        fun createRoute(plantaNom: String) = "consums_actuals/$plantaNom"
    }

    // Pantalla d'ajustos personals: tema visual, preferències i tancament de sessió
    object Ajustos : Screen("ajustos")

    // Pantalla per revisar i corregir registres horaris de consum d'una planta concreta
    object ConsumRegistres : Screen("consum_registres/{plantaId}/{plantaNom}") {
        // Construeix la URL amb l'ID i el nom de la planta
        fun createRoute(plantaId: Int, plantaNom: String) = "consum_registres/$plantaId/$plantaNom"
    }
}
