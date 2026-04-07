package com.noel.energyapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.noel.energyapp.ui.alarmes.HistoricAlarmaDetailScreen
import com.noel.energyapp.ui.components.FloatingBottomBar
import com.noel.energyapp.ui.ajustos.AjustosScreen


class MainActivity : ComponentActivity() {

    // Llançador per demanar el permís POST_NOTIFICATIONS en temps d'execució (Android 13+)
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("FCM_PERM", "Permís POST_NOTIFICATIONS concedit: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sol·licitem el permís de notificacions en temps d'execució per Android 13+ (API 33+)
        // Sense aixo, cap push notification arribarà al dispositiu tot i tenir-ho al Manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 1. Instanciem el SessionManager per saber si l'usuari ja s'ha loguejat abans
        val sessionManager = SessionManager(this)
        setContent {
            // Un estat simple per forçar la recomposició del tema quan canviem a Ajustos
            var themeRecomposeKey by remember { mutableStateOf(0) }

            key(themeRecomposeKey) {
                NoelEnergyAppTheme {
                    // 2. Creem el NavController: és l'objecte que executa les ordres de navegar
                    // Aquest objecte és el "director" de l'App.
                    val navController = rememberNavController()

                    // NOU: Encenem el servei d'alarmes en segon pla INCONDICIONALMENT perquè
                    // t'arribin les notificacions a tot arreu encara que no hi hagi sessió
                    val serviceIntent = android.content.Intent(this@MainActivity, com.noel.energyapp.service.SignalRService::class.java)
                    androidx.core.content.ContextCompat.startForegroundService(this@MainActivity, serviceIntent)

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

                    // 4. Determinem quines pantalles han de mostrar la BottomBar
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // NOU: Detectem l'orientació real de la pantalla en temps de composició
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                    val isMainScreen = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.GestioPlantes.route,
                        Screen.GestioUsuaris.route
                    )

                    // Determineu si hem de mostrar la barra de navegació inferior
                    // Amagada al Login, ChangePassword i SEMPRE que el mòbil estigui en Landscape (Horitzontal)
                    val shouldShowBottomBar = currentRoute !in listOf(
                        Screen.Login.route,
                        Screen.ChangePassword.route
                    ) && !isLandscape

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (shouldShowBottomBar) {
                                FloatingBottomBar(
                                    currentRoute = currentRoute,
                                    userRole = sessionManager.fetchUserRole(),
                                    isMainScreen = isMainScreen,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    ) { padding ->

                        // 5. El NavHost és el "mapa" que connecta les rutes amb els fitxers .kt
                        NavHost(
                            navController = navController,
                            startDestination = startDestination // On comencem
                        ) {

                            // --- RUTA 1: LOGIN ---
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    paddingValues = padding, // Passem el marge per no tapar el rellotge
                                    onLoginSuccess = { requiresPasswordChange -> // NOU: Rebem el boolean per saber on enviar-lo
                                        
                                        // Accionem el servei de notificacions un cop fem login!
                                        val serviceIntent = android.content.Intent(this@MainActivity, com.noel.energyapp.service.SignalRService::class.java)
                                        androidx.core.content.ContextCompat.startForegroundService(this@MainActivity, serviceIntent)

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
                                    onPlantaClick = { id, nom ->
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
                                val plantaNom =
                                    backStackEntry.arguments?.getString("plantaNom") ?: "Planta"

                                // Cridem la nostra funció de Compose passant-li les dades
                                PlantaDetailScreen(
                                    paddingValues = padding,
                                    plantaId = plantaId,
                                    plantaNom = plantaNom,
                                    userRole = sessionManager.fetchUserRole(),
                                    userRoleId = sessionManager.fetchUserRoleId(), // PASSEM L'ID NUMÈRIC
                                    onBackClick = { navController.popBackStack() },
                                    onNavigateToAlarmesActives = { navController.navigate(Screen.AlarmesActives.createRoute(plantaId)) },
                                    onNavigateToAlarmesHistoric = { navController.navigate(Screen.AlarmesHistoric.createRoute(plantaId)) },
                                    onNavigateToConsumGrafica = { navController.navigate(Screen.ConsumGrafica.createRoute(plantaId, plantaNom)) },
                                    onNavigateToConsumsActuals = { navController.navigate(Screen.ConsumsActuals.createRoute(plantaNom)) },
                                    onNavigateToConsumRegistres = { navController.navigate(Screen.ConsumRegistres.createRoute(plantaId, plantaNom)) }
                                )
                            }

                            // --- RUTA 5: GESTIÓ DE PLANTES ---
                            composable(Screen.GestioPlantes.route) {
                                GestioPlantesScreen(
                                    paddingValues = padding,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // --- RUTA 6: GESTIÓ D'USUARIS ---
                            composable(Screen.GestioUsuaris.route) {
                                GestioUsuarisScreen(
                                    paddingValues = padding,
                                    onBackClick = { navController.popBackStack() }
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

                            // --- RUTA 8: ALARMES ACTIVES (Actualitzada) ---
                            composable(
                                route = Screen.AlarmesActives.route,
                                arguments = listOf(navArgument("plantaId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                AlarmesActivesScreen(
                                    paddingValues = padding,
                                    plantaId = plantaId, // <--- Li passem la planta
                                    onBackClick = { navController.popBackStack() },
                                    onNavigateToTancarAlarma = { idIncidencia ->
                                        navController.navigate(
                                            Screen.TancarIncidencia.createRoute(
                                                idIncidencia
                                            )
                                        )
                                    }
                                )
                            }

                            // --- RUTA 9: TANCAR INCIDÈNCIA ---
                            composable(
                                route = Screen.TancarIncidencia.route,
                                arguments = listOf(androidx.navigation.navArgument("incidenciaId") {
                                    type = NavType.IntType
                                })
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


                            // --- RUTA 10: HISTÒRIC D'ALARMES (Actualitzada) ---
                            composable(
                                route = Screen.AlarmesHistoric.route,
                                arguments = listOf(navArgument("plantaId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                AlarmesHistoricScreen(
                                    paddingValues = padding,
                                    plantaId = plantaId, // <--- Li passem la planta
                                    onBackClick = { navController.popBackStack() },
                                    onAlarmaClick = { alarmaId ->
                                        navController.navigate(
                                            Screen.HistoricAlarmaDetail.createRoute(
                                                alarmaId
                                            )
                                        )
                                    }
                                )
                            }

                            // --- RUTA 11: DETALL D'ALARMA HISTÒRICA ---
                            composable(
                                route = Screen.HistoricAlarmaDetail.route,
                                arguments = listOf(navArgument("alarmaId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val alarmaId = backStackEntry.arguments?.getInt("alarmaId") ?: 0
                                HistoricAlarmaDetailScreen(
                                    paddingValues = padding,
                                    alarmaId = alarmaId,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // --- RUTA 12: GRÀFICA CONSUM M3 (Nova) ---
                            composable(
                                route = Screen.ConsumGrafica.route,
                                arguments = listOf(
                                    navArgument("plantaId") { type = NavType.IntType },
                                    navArgument("plantaNom") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                val plantaNom = backStackEntry.arguments?.getString("plantaNom") ?: ""
                                com.noel.energyapp.ui.consums.ConsumGraficaScreen(
                                    paddingValues = padding,
                                    plantaNom = plantaNom,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // --- RUTA 13: CONSUMS ACTUALS LIVE ---
                            composable(
                                route = Screen.ConsumsActuals.route,
                                arguments = listOf(navArgument("plantaNom") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val plantaNom = backStackEntry.arguments?.getString("plantaNom") ?: ""
                                com.noel.energyapp.ui.consums.ConsumsActualsScreen(
                                    paddingValues = padding,
                                    plantaNom = plantaNom,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // --- RUTA 14: GESTIÓ DE REGISTRES DE CONSUM (Nova) ---
                            composable(
                                route = Screen.ConsumRegistres.route,
                                arguments = listOf(
                                    navArgument("plantaId") { type = NavType.IntType },
                                    navArgument("plantaNom") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                val plantaNom = backStackEntry.arguments?.getString("plantaNom") ?: ""
                                com.noel.energyapp.ui.consums.ConsumRegistresScreen(
                                    paddingValues = padding,
                                    plantaId = plantaId,
                                    plantaNom = plantaNom,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // --- RUTA 15: AJUSTOS ---
                            composable(Screen.Ajustos.route) {
                                AjustosScreen(
                                    paddingValues = padding,
                                    onBackClick = { navController.popBackStack() },
                                    onThemeChanged = { themeRecomposeKey++ }, // NOU CALLBACK
                                    onLogoutClick = {
                                        sessionManager.clearUserData()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
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
}