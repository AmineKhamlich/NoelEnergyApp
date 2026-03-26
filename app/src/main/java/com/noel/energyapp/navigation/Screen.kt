package com.noel.energyapp.navigation

// Aquest objecte ens serveix per no tenir rutes "hardcoded" pel codi.
// Si demà volem canviar "login" per "autenticacio", ho fem aquí i canvia a tot l'App.
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")

    // HO COMENTEM PER NO PERDRE-HO, PERÒ NO HO FEM SERVIR DE MOMENT
    // object ForgotPassword : Screen("forgot_password")

    // Ruta per al detall de la planta que demana dos paràmetres (ID i Nom)
    object PlantaDetail : Screen("planta_detail/{plantaId}/{plantaNom}") {
        fun createRoute(plantaId: Int, plantaNom: String) = "planta_detail/$plantaId/$plantaNom"
    }

    // Rutes per als apartats d'Administració
    object GestioPlantes : Screen("gestio_plantes")
    object GestioUsuaris : Screen("gestio_usuaris")

    // Rutes per a la gestio d'alarmes
    //object AlarmesActives : Screen("alarmes_actives")

    // Ruta per a tancar incidencia
    object TancarIncidencia : Screen("tancar_incidencia/{incidenciaId}") {
        // Aquesta funció ens ajuda a crear la URL: "tancar_incidencia/45"
        fun createRoute(id: Int) = "tancar_incidencia/$id"
    }

    // Ruta per el canvi de contrasenya obligatori
    object ChangePassword : Screen("change_password")

    // Ruta per a l'historial d'alarmes
    //object AlarmesHistoric : Screen("alarmes_historic")

    // Ruta per al detall d'una alarma tancada
    object HistoricAlarmaDetail : Screen("historic_alarma_detail/{alarmaId}") {
        fun createRoute(alarmaId: Int) = "historic_alarma_detail/$alarmaId"
    }

    // Rutes per a les alarmes, ara demanen la Planta
    object AlarmesActives : Screen("alarmes_actives/{plantaId}") {
        fun createRoute(plantaId: Int) = "alarmes_actives/$plantaId"
    }

    object AlarmesHistoric : Screen("alarmes_historic/{plantaId}") {
        fun createRoute(plantaId: Int) = "alarmes_historic/$plantaId"
    }

    // Ruta de la Gràfica de Consum
    object ConsumGrafica : Screen("consum_grafica/{plantaId}/{plantaNom}") {
        fun createRoute(plantaId: Int, plantaNom: String) = "consum_grafica/$plantaId/$plantaNom"
    }
    
    // Ruta dels Consums Actuals en Viu
    object ConsumsActuals : Screen("consums_actuals/{plantaNom}") {
        fun createRoute(plantaNom: String) = "consums_actuals/$plantaNom"
    }

    // Ruta per a Ajustos i Perfil d'Usuari
    object Ajustos : Screen("ajustos")

    // Ruta per a la Gestió de Registres de Consum (Unitari per dia)
    object ConsumRegistres : Screen("consum_registres/{plantaId}/{plantaNom}") {
        fun createRoute(plantaId: Int, plantaNom: String) = "consum_registres/$plantaId/$plantaNom"
    }
}


