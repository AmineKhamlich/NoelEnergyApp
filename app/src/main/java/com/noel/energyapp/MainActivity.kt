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
import com.noel.energyapp.ui.alarmes.AlarmesActivesScreen
import com.noel.energyapp.ui.dashboard.DashboardScreen
import com.noel.energyapp.ui.login.ForgotPasswordScreen
import com.noel.energyapp.ui.login.LoginScreen
import com.noel.energyapp.ui.login.ChangePasswordScreen // NOU IMPORT
import com.noel.energyapp.ui.planta.PlantaDetailScreen
import com.noel.energyapp.ui.theme.NoelEnergyAppTheme
import com.noel.energyapp.util.SessionManager
import com.noel.energyapp.ui.planta.GestioPlantesScreen
import com.noel.energyapp.ui.usuaris.GestioUsuarisScreen
import com.noel.energyapp.ui.alarmes.AlarmesHistoricScreen


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

                // 3. Decidim quina és la pantalla inicial (Login, Dashboard o Canvi de Contrasenya)
                // LÒGICA ANTIBALES: Si té token, comprovem si està bloquejat pel canvi de contrasenya.
                val startDestination = if (sessionManager.fetchAuthToken() != null) {
                    if (sessionManager.fetchMustChangePassword()) {
                        Screen.ChangePassword.route // Està bloquejat! A la presó del canvi de contrasenya.
                    } else {
                        Screen.Dashboard.route // Tot OK, al Dashboard!
                    }
                } else {
                    Screen.Login.route // No té token, al Login.
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
                                onLoginSuccess = { requiresPasswordChange -> // NOU: Rebem el boolean per saber on enviar-lo
                                    if (requiresPasswordChange) {
                                        // Va a la pantalla de canvi de contrasenya
                                        navController.navigate(Screen.ChangePassword.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    } else {
                                        // Quan el login és correcte i no ha de canviar res, anem al Dashboard com sempre
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                                // COMENTEM AQUESTA FUNCIÓ PERQUÈ JA NO HI HA BOTÓ AL LOGIN
                                /*
                                , onForgotPasswordClick = {
                                    // Quan cliquem, anem a la pantalla de recuperar
                                    navController.navigate(Screen.ForgotPassword.route)
                                }*/
                            )
                        }

//                        // --- RUTA 2: RECUPERAR CONTRASENYA ---
//                        composable(Screen.ForgotPassword.route) {
//                            ForgotPasswordScreen(
//                                paddingValues = padding,
//                                onBackToLogin = {
//                                    // Simplement tornem enrere a la pila de navegació (com si preméssim el botó físic)
//                                    navController.popBackStack()
//                                }
//                            )
//                        }

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
                                },
                                // Li hem de passar aquestes accions al Dashboard perquè aquest
                                // les passi a la plantilla NoelScreen.
                                onNavigateToGestioPlantes = { navController.navigate(Screen.GestioPlantes.route) },
                                onNavigateToGestioUsuaris = { navController.navigate(Screen.GestioUsuaris.route) }
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
                            val plantaNom =
                                backStackEntry.arguments?.getString("plantaNom") ?: "Planta"

                            // Cridem la nostra funció de Compose passant-li les dades
                            PlantaDetailScreen(
                                paddingValues = padding,
                                plantaId = plantaId,
                                plantaNom = plantaNom,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToAlarmesActives = { navController.navigate(Screen.AlarmesActives.route) },
                                onNavigateToAlarmesHistoric = { navController.navigate(Screen.AlarmesHistoric.route) }
                            )
                        }

                        // --- RUTA 5: GESTIÓ DE PLANTES ---
                        composable(Screen.GestioPlantes.route) {
                            GestioPlantesScreen(
                                paddingValues = padding,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToGestioPlantes = { /* Ja som aquí, no fem res */ },
                                onNavigateToGestioUsuaris = {
                                    navController.navigate(Screen.GestioUsuaris.route) {
                                        popUpTo(Screen.Dashboard.route)
                                    }
                                }
                            )
                        }

                        // --- RUTA 6: GESTIÓ D'USUARIS ---
                        composable(Screen.GestioUsuaris.route) {
                            GestioUsuarisScreen(
                                paddingValues = padding,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToGestioUsuaris = { /* Ja som aquí, no fem res */ },
                                onNavigateToGestioPlantes = {
                                    navController.navigate(Screen.GestioPlantes.route) {
                                        popUpTo(Screen.Dashboard.route)
                                    }
                                }
                            )
                        }

                        // --- RUTA 7: CANVI DE CONTRASENYA OBLIGATORI ---
                        composable(Screen.ChangePassword.route) {
                            ChangePasswordScreen(
                                paddingValues = padding,
                                onPasswordChangedSuccessfully = {
                                    // Quan ho fa bé, viatja al Dashboard i esborra la pantalla actual de l'historial
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.ChangePassword.route) { inclusive = true }
                                    }
                                },
                                onLogoutClick = {
                                    // Si es penedeix i no vol canviar la contrasenya, l'esborrem i el tirem al Login
                                    sessionManager.clearUserData()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) {
                                            inclusive = true
                                        } // popUpTo(0) buida TOT l'historial
                                    }
                                }
                            )
                        }

                        // --- RUTA 8: ALARMES ACTIVES ---
                        composable(Screen.AlarmesActives.route) {
                            AlarmesActivesScreen(
                                paddingValues = padding,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToTancarAlarma = { idIncidencia ->
                                    navController.navigate(Screen.TancarIncidencia.createRoute(idIncidencia))
                                }
                            )
                        }

                        // --- RUTA 9: TANCAR INCIDÈNCIA ---
                        composable(
                            route = Screen.TancarIncidencia.route,
                            arguments = listOf(androidx.navigation.navArgument("incidenciaId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("incidenciaId") ?: 0

                            com.noel.energyapp.ui.alarmes.TancarIncidenciaScreen(
                                paddingValues = padding,
                                incidenciaId = id,
                                onBackClick = { navController.popBackStack() },
                                onSuccess = {
                                    // Si va bé, tirem enrere cap a la llista d'alarmes per veure com ha desaparegut
                                    navController.popBackStack()
                                }
                            )
                        }

                        // --- RUTA 10: HISTÒRIC D'ALARMES ---
                        composable(Screen.AlarmesHistoric.route) {
                            AlarmesHistoricScreen(
                                paddingValues = padding,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}