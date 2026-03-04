package com.noel.energyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noel.energyapp.navigation.Screen
import com.noel.energyapp.ui.dashboard.DashboardScreen
import com.noel.energyapp.ui.login.LoginScreen
import com.noel.energyapp.ui.theme.NoelEnergyAppTheme
import com.noel.energyapp.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Instanciem el SessionManager per saber si l'usuari ja s'ha loguejat abans
        val sessionManager = SessionManager(this)

        setContent {
            NoelEnergyAppTheme {
                // 2. Creem el NavController: és l'objecte que executa les ordres de navegar
                val navController = rememberNavController()

                // 3. Decidim quina és la pantalla inicial (Login o Dashboard)
                // Si el token no és nul, vol dir que ja estem loguejats.
                val startDestination = if (sessionManager.fetchAuthToken() != null) {
                    Screen.Dashboard.route
                } else {
                    Screen.Login.route
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    // 4. El NavHost és el "mapa" que connecta les rutes amb els fitxers .kt
                    NavHost(
                        navController = navController,
                        startDestination = startDestination // On comencem
                    ) {
                        // RUTA 1: LOGIN
                        composable(Screen.Login.route) {
                            LoginScreen(
                                padding,
                                onLoginSuccess = {
                                    // Quan el login és correcte, anem al Dashboard
                                    // popUpTo("login") { inclusive = true } serveix per esborrar
                                    // la pantalla de login de l'historial (perquè no es pugui tornar enrere)
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // RUTA 2: DASHBOARD
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                userName = sessionManager.fetchUserName(),
                                onLogout = {
                                    // Quan tanquem sessió, esborrem dades i tornem al Login
                                    sessionManager.clearUserData()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoelEnergyAppTheme {
        Greeting("Android")
    }
}