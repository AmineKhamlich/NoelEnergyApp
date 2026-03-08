package com.noel.energyapp.navigation

// Aquest objecte ens serveix per no tenir rutes "hardcoded" pel codi.
// Si demà volem canviar "login" per "autenticacio", ho fem aquí i canvia a tot l'App.
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ForgotPassword : Screen("forgot_password")

    // Ruta per al detall de la planta que demana dos paràmetres (ID i Nom)
    object PlantaDetail : Screen("planta_detail/{plantaId}/{plantaNom}") {
        fun createRoute(plantaId: Int, plantaNom: String) = "planta_detail/$plantaId/$plantaNom"
    }

    // Rutes per als apartats d'Administració
    object GestioPlantes : Screen("gestio_plantes")
    object GestioUsuaris : Screen("gestio_usuaris")

    // Ruta per el canvi de contrasenya obligatori
    object ChangePassword : Screen("change_password")
    }