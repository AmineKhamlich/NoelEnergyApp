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
    object AlarmesActives : Screen("alarmes_actives")

    // Ruta per a tancar incidencia
    object TancarIncidencia : Screen("tancar_incidencia/{incidenciaId}") {
        // Aquesta funció ens ajuda a crear la URL: "tancar_incidencia/45"
        fun createRoute(id: Int) = "tancar_incidencia/$id"
    }

    // Ruta per el canvi de contrasenya obligatori
    object ChangePassword : Screen("change_password")

    // Ruta per a l'historial d'alarmes
    object AlarmesHistoric : Screen("alarmes_historic")
}


