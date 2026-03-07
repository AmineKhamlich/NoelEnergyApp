package com.noel.energyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType // NOU IMPORT (Per fer el codi més net)
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument // NOU IMPORT (Per fer el codi més net)
import com.noel.energyapp.navigation.Screen
import com.noel.energyapp.ui.dashboard.DashboardScreen
import com.noel.energyapp.ui.login.ForgotPasswordScreen
import com.noel.energyapp.ui.login.LoginScreen
import com.noel.energyapp.ui.planta.PlantaDetailScreen
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
                // Aquest objecte és el "director" de l'App.
                val navController = rememberNavController()

                // 3. Decidim quina és la pantalla inicial (Login o Dashboard)
                // Si el token no és nul, vol dir que ja estem loguejats i saltem el Login.
                val startDestination = if (sessionManager.fetchAuthToken() != null) {
                    Screen.Dashboard.route
                } else {
                    Screen.Login.route
                }

                // El Scaffold principal ens dona els 'paddingValues' (l'espai de la bateria i els botons de baix)
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->

                    // 4. El NavHost és el "mapa" que connecta les rutes amb els fitxers .kt
                    NavHost(
                        navController = navController,
                        startDestination = startDestination // On comencem
                    ) {

                        // --- RUTA 1: LOGIN ---
                        composable(Screen.Login.route) {
                            LoginScreen(
                                paddingValues = padding, // Passem el marge per no tapar el rellotge
                                onLoginSuccess = {
                                    // Quan el login és correcte, anem al Dashboard
                                    // popUpTo(Screen.Login.route) serveix per esborrar
                                    // la pantalla de login de l'historial (perquè al fer "Enrere" no tornem al login)
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onForgotPasswordClick = {
                                    // Quan cliquem, anem a la pantalla de recuperar
                                    navController.navigate(Screen.ForgotPassword.route)
                                }
                            )
                        }

                        // --- RUTA 2: RECUPERAR CONTRASENYA ---
                        composable(Screen.ForgotPassword.route) {
                            ForgotPasswordScreen(
                                paddingValues = padding,
                                onBackToLogin = {
                                    // Simplement tornem enrere a la pila de navegació (com si preméssim el botó físic)
                                    navController.popBackStack()
                                }
                            )
                        }

                        // --- RUTA 3: DASHBOARD ---
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                paddingValues = padding,
                                userName = sessionManager.fetchUserName(),
                                onLogout = {
                                    // Quan tanquem sessió, esborrem dades i tornem al Login,
                                    // destruint l'historial del Dashboard.
                                    sessionManager.clearUserData()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                    }
                                },
                                onPlantaClick = { id, nom ->
                                    // Naveguem a la pantalla de detall construint la ruta dinàmica
                                    // Exemple resultant: "planta_detail/3/Noel-1"
                                    navController.navigate(Screen.PlantaDetail.createRoute(id, nom))
                                }
                            )
                        }

                        // --- RUTA 4: DETALL DE LA PLANTA ---
                        composable(
                            route = Screen.PlantaDetail.route, // Ruta: "planta_detail/{plantaId}/{plantaNom}"
                            // Especifiquem al NavHost quin tipus de dades viatgen a la URL
                            arguments = listOf(
                                navArgument("plantaId") { type = NavType.IntType },
                                navArgument("plantaNom") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            // Extraiem els valors exactes de la URL de navegació de manera segura
                            val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                            val plantaNom = backStackEntry.arguments?.getString("plantaNom") ?: "Planta"

                            // Cridem la nostra funció de Compose passant-li les dades
                            PlantaDetailScreen(
                                paddingValues = padding,
                                plantaId = plantaId,
                                plantaNom = plantaNom,
                                onBackClick = {
                                    // Tornem enrere al Dashboard utilitzant la fletxa de la TopBar
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}