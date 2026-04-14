package com.noel.energyapp

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.noel.energyapp.navigation.Screen
import com.noel.energyapp.service.SignalRService
import com.noel.energyapp.ui.ajustos.AjustosScreen
import com.noel.energyapp.ui.alarmes.AlarmesActivesScreen
import com.noel.energyapp.ui.alarmes.AlarmesHistoricScreen
import com.noel.energyapp.ui.alarmes.HistoricAlarmaDetailScreen
import com.noel.energyapp.ui.alarmes.TancarIncidenciaScreen
import com.noel.energyapp.ui.components.FloatingBottomBar
import com.noel.energyapp.ui.consums.ConsumGraficaScreen
import com.noel.energyapp.ui.consums.ConsumRegistresScreen
import com.noel.energyapp.ui.consums.ConsumsActualsScreen
import com.noel.energyapp.ui.dashboard.DashboardScreen
import com.noel.energyapp.ui.login.ChangePasswordScreen
import com.noel.energyapp.ui.login.ForgotPasswordScreen
import com.noel.energyapp.ui.login.LoginScreen
import com.noel.energyapp.ui.planta.GestioPlantesScreen
import com.noel.energyapp.ui.planta.PlantaDetailScreen
import com.noel.energyapp.ui.theme.NoelEnergyAppTheme
import com.noel.energyapp.ui.usuaris.GestioUsuarisScreen
import com.noel.energyapp.util.SessionManager


class MainActivity : ComponentActivity() {

    // Llançador per demanar el permís POST_NOTIFICATIONS en temps d'execució (Android 13+)
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("FCM_PERM", "Permís POST_NOTIFICATIONS concedit: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sol·licitem el permís de notificacions en temps d'execució per Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 1. Instanciem el SessionManager per saber si l'usuari ja s'ha loguejat abans
        val sessionManager = SessionManager(this)
        setContent {
            // Un estat simple per forçar la recomposició del tema quan canviem a Ajustos
            var themeRecomposeKey by remember { mutableStateOf(0) }

            key(themeRecomposeKey) {
                NoelEnergyAppTheme {
                    // 2. Creem el NavController: és l'objecte que executa les ordres de navegar
                    val navController = rememberNavController()

                    // Encenem el servei d'alarmes en segon pla INCONDICIONALMENT
                    val serviceIntent = Intent(this@MainActivity, SignalRService::class.java)
                    ContextCompat.startForegroundService(this@MainActivity, serviceIntent)

                    // 3. Decidim quina és la pantalla inicial (Login, Dashboard o Canvi de Contrasenya)
                    val startDestination = if (sessionManager.fetchAuthToken() != null) {
                        if (sessionManager.fetchMustChangePassword()) {
                            Screen.ChangePassword.route
                        } else {
                            Screen.Dashboard.route
                        }
                    } else {
                        Screen.Login.route
                    }

                    // 4. Determinem quines pantalles han de mostrar la BottomBar
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Detectem l'orientació real de la pantalla en temps de composició
                    val configuration = LocalConfiguration.current
                    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                    val isMainScreen = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.GestioPlantes.route,
                        Screen.GestioUsuaris.route
                    )

                    // Amagada al Login, ChangePassword i SEMPRE que el mòbil estigui en Landscape
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
                            startDestination = startDestination
                        ) {

                            // --- RUTA 1: LOGIN ---
                            composable(Screen.Login.route) {
                                LoginScreen(
                                    paddingValues = padding,
                                    onLoginSuccess = { requiresPasswordChange ->

                                        // Accionem el servei de notificacions un cop fem login!
                                        val loginServiceIntent = Intent(this@MainActivity, SignalRService::class.java)
                                        ContextCompat.startForegroundService(this@MainActivity, loginServiceIntent)

                                        if (requiresPasswordChange) {
                                            navController.navigate(Screen.ChangePassword.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

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
                                route = Screen.PlantaDetail.route,
                                arguments = listOf(
                                    navArgument("plantaId") { type = NavType.IntType },
                                    navArgument("plantaNom") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                val plantaNom = backStackEntry.arguments?.getString("plantaNom") ?: "Planta"

                                PlantaDetailScreen(
                                    paddingValues = padding,
                                    plantaId = plantaId,
                                    plantaNom = plantaNom,
                                    userRole = sessionManager.fetchUserRole(),
                                    userRoleId = sessionManager.fetchUserRoleId(),
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
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.ChangePassword.route) { inclusive = true }
                                        }
                                    },
                                    onLogoutClick = {
                                        sessionManager.clearUserData()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // --- RUTA 8: ALARMES ACTIVES ---
                            composable(
                                route = Screen.AlarmesActives.route,
                                arguments = listOf(navArgument("plantaId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                AlarmesActivesScreen(
                                    paddingValues = padding,
                                    plantaId = plantaId,
                                    onBackClick = { navController.popBackStack() },
                                    onNavigateToTancarAlarma = { idIncidencia ->
                                        navController.navigate(Screen.TancarIncidencia.createRoute(idIncidencia))
                                    }
                                )
                            }

                            // --- RUTA 9: TANCAR INCIDÈNCIA ---
                            composable(
                                route = Screen.TancarIncidencia.route,
                                arguments = listOf(navArgument("incidenciaId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("incidenciaId") ?: 0
                                TancarIncidenciaScreen(
                                    paddingValues = padding,
                                    incidenciaId = id,
                                    onBackClick = { navController.popBackStack() },
                                    onSuccess = { navController.popBackStack() }
                                )
                            }

                            // --- RUTA 10: HISTÒRIC D'ALARMES ---
                            composable(
                                route = Screen.AlarmesHistoric.route,
                                arguments = listOf(navArgument("plantaId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                AlarmesHistoricScreen(
                                    paddingValues = padding,
                                    plantaId = plantaId,
                                    onBackClick = { navController.popBackStack() },
                                    onAlarmaClick = { alarmaId ->
                                        navController.navigate(Screen.HistoricAlarmaDetail.createRoute(alarmaId))
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

                            // --- RUTA 12: GRÀFICA CONSUM M3 ---
                            composable(
                                route = Screen.ConsumGrafica.route,
                                arguments = listOf(
                                    navArgument("plantaId") { type = NavType.IntType },
                                    navArgument("plantaNom") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val plantaNom = backStackEntry.arguments?.getString("plantaNom") ?: ""
                                ConsumGraficaScreen(
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
                                ConsumsActualsScreen(
                                    paddingValues = padding,
                                    plantaNom = plantaNom,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // --- RUTA 14: GESTIÓ DE REGISTRES DE CONSUM ---
                            composable(
                                route = Screen.ConsumRegistres.route,
                                arguments = listOf(
                                    navArgument("plantaId") { type = NavType.IntType },
                                    navArgument("plantaNom") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val plantaId = backStackEntry.arguments?.getInt("plantaId") ?: 0
                                val plantaNom = backStackEntry.arguments?.getString("plantaNom") ?: ""
                                ConsumRegistresScreen(
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
                                    onThemeChanged = { themeRecomposeKey++ },
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