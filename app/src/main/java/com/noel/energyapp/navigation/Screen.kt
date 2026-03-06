package com.noel.energyapp.navigation

// Aquest objecte ens serveix per no tenir rutes "hardcoded" pel codi.
// Si demà volem canviar "login" per "autenticacio", ho fem aquí i canvia a tot l'App.
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ForgotPassword : Screen("forgot_password")

}