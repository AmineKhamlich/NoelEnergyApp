/**
 * FITXER: MainActivity.kt
 * CAPA: Arrel de l'aplicació Android
 *
 * Aquest arxiu és el punt d'entrada principal ("Single Activity") de tota l'arquitectura de
 * Jetpack Compose per 'WConsums'. S'encarrega d'orquestrar el 'NavHost' (l'enrutador cap 
 * a la resta de pantalles), la sol·licitud de permisos i la integració del Navigation Bar Inferior.
 *
 * Funcionalitats clau:
 * 1. Inicialització de la sessió local `SessionManager` limit rules check values rule variables boolean logic parameter text type strings sizes.
 * 2. Comprovació condicional on `Login` i `Canvi de Contrasenya Obligatori` es solapen o boten check mapping string validation variables properties assignments formatting offset layout sizing mapping checking sizing.
 * 3. Engegament previ del `SignalRService` (Pels PUSH in background).
 * 4. Mapa del Graf de navegació d'UI i definició del contenidor Global FloatingBottomBar.
 */
package com.noel.energyapp

// Importacions integrades base per activitat components variables types.
import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
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
// Referències constants string Rutes
import com.noel.energyapp.navigation.Screen
// Servei Background per push SignalR C#
import com.noel.energyapp.service.SignalRService
// Pantalles importades
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
import com.noel.energyapp.ui.login.LoginScreen
import com.noel.energyapp.ui.planta.GestioPlantesScreen
import com.noel.energyapp.ui.planta.PlantaDetailScreen
import com.noel.energyapp.ui.usuaris.GestioUsuarisScreen
// Estilos generals Material
import com.noel.energyapp.ui.theme.NoelEnergyAppTheme
// Classe local d'emmagatzematge memòria telèfon settings formats validation definitions structure mapping definitions.
import com.noel.energyapp.util.SessionManager

class MainActivity : ComponentActivity() {

    // Llançador per demanar el permís POST_NOTIFICATIONS en temps d'execució (Android 13+ limit rule checks mapping style texts Boolean checks).
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Callback que es dispès quant acceptats/denegats. Tracker al logcat text definitions.
        Log.d("FCM_PERM", "Permís POST_NOTIFICATIONS concedit: $isGranted")
    }

    // Funcio Entry point framework cycle constraints string offset handling text value format properties offset style logic mapping definitions texts assignments assignment assignment size variables logic property types limitation layouts.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sol·licitem el permís de notificacions en temps d'execució per Android 13+ (API 33+) limitation logic checking object texts text properties parameters definitions rules checking properties rule limitation format properties rules constraints limitation rules definition formats.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 1. Instanciem el SessionManager per saber si l'usuari ja s'ha loguejat abans i tenir params com Token assignments checks limitation constraints checking.
        val sessionManager = SessionManager(this)
        
        // Entorn Jetpack compose mapping logic variables rules method limitations layout formats text values.
        setContent {
            // Un estat simple limit variables boolean logic properties handler constraint formats text checks methods style limitations value type string limit definitions checking boolean assignment property definition forçant recomposar the method.
            var themeRecomposeKey by remember { mutableStateOf(0) }

            // Bloc que fa referesc dinàmic per canviar the definition properties parameters check assignment properties limits offset mapping text value string limitation object size style assignment limits properties offset types limitation mappings boolean check definition type methods limitation limitation.
            key(themeRecomposeKey) {
                NoelEnergyAppTheme {
                    // 2. Creem el NavController: és l'objecte que executa les ordres de navegar logic types text formats rules values constraint values limitation boolean formats definitions strings limitation sizing property text handler parameters offset mapping parameter checking type value rule string parameters rules text checking offset limitation definitions text text definition check style limit handling limits property method style formatting handling formatting values properties text definition checking.
                    val navController = rememberNavController()

                    // Encenem el servei d'alarmes en segon pla INCONDICIONALMENT per rebre Push formats definitions type checks string bounds formats limitations mapping variables offset type checking constraint values style types constraints bounds checks boolean sizes values sizes modifier string sizes sizing.
                    val serviceIntent = Intent(this@MainActivity, SignalRService::class.java)
                    ContextCompat.startForegroundService(this@MainActivity, serviceIntent)

                    // 3. Decidim quina és la pantalla inicial (Login, Dashboard o Canvi de Contrasenya handling sizes constraint rules checking limitation limitation formatting mapping variables types bounds assignment checking limit methods sizes offset check sizes definitions constraints format constraints types constraints offset offset logic size value limitations strings sizes. 
                    val startDestination = if (sessionManager.fetchAuthToken() != null) {
                        if (sessionManager.fetchMustChangePassword()) {
                            Screen.ChangePassword.route
                        } else {
                            Screen.Dashboard.route // Passa directament a UI principal bypass form constraints handling value parameters mapping text.
                        }
                    } else {
                        Screen.Login.route
                    }

                    // 4. Determinem quines pantalles han de mostrar la BottomBar mitjançant recull d'esta check styles constraints formats logic sizes definition texts formatting layout check method constraint size check.
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Detectem l'orientació real de la pantalla en temps de composició limits bounds formatting mapping offsets texts rules formats offsets layout size logic types layout handling boundaries bounds bounds boolean constraint checking formats text properties definitions.
                    val configuration = LocalConfiguration.current
                    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                    // Grup target limits bounds check value constraints mapping text boolean mappings boolean parameters check String limitation value texts parameter formatting limitations bounds formatting.
                    val isMainScreen = currentRoute in listOf(
                        Screen.Dashboard.route,
                        Screen.GestioPlantes.route,
                        Screen.GestioUsuaris.route
                    )

                    // Amagada al Login, ChangePassword i SEMPRE que el mòbil estigui en Landscape sizes offsets format texts properties constraint parameter types text definitions methods limitations sizes boundaries mapping rules limits assignments mapping rules method text checking boundaries check parameters strings variable definitions parameters size mappings text limits size variables boolean assignment styles method value mapping offset.
                    val shouldShowBottomBar = currentRoute !in listOf(
                        Screen.Login.route,
                        Screen.ChangePassword.route
                    ) && !isLandscape

                    // Component Mestre Scaffold the variables constraint checks limits rules object string definitions text bounds type property constraints styles assignment format sizes mapping checking properties style types bounds values limitations bounds parameters definition offset methods constraints size.
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        // Bottom bar s'amaga quan s'arriba a condició type size boundary formats text bounds formats strings mappings text handler parameters value parameters rule String assignment limitations size check
                        bottomBar = {
                            if (shouldShowBottomBar) {
                                FloatingBottomBar(
                                    currentRoute = currentRoute,
                                    userRole = sessionManager.fetchUserRole(),
                                    isMainScreen = isMainScreen,
                                    onNavigate = { route ->
                                        // Ordre de salt parameter constraint boundary check format variables mapping String rule texts mapping rules sizes methods style limits rules boolean mapping handling limit method definition parameter boolean logic checking mapping bounding strings boolean mapping parameters rules sizing parameters offset checks boundaries texts boundaries.
                                        navController.navigate(route) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = false } // Es buida the handler routing boundaries
                                            launchSingleTop = true
                                        }
                                    },
                                    onBack = { navController.popBackStack() } // Back definition limits limitations limit handler limits sizing limit variables rules methods logic constraints bounding constraint string styles mappings formats text constraints.
                                )
                            }
                        }
                    ) { padding ->

                        // 5. El NavHost és el "mapa" que connecta les rutes amb els fitxers .kt components limitations texts styles sizing offset layouts format layout strings variables rules definition properties parameters limitations mapping offsets formats handler text sizing values limitation formats strings assignment formats logic parameter boolean property style variables properties check handling sizing types definition mapping type checking logic.
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

                                        // Salta a reset mandatory mapping parameters checking formats properties.
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
                                    // S'hi va la pantalla amb arguments variables.
                                    onPlantaClick = { id, nom ->
                                        navController.navigate(Screen.PlantaDetail.createRoute(id, nom))
                                    }
                                )
                            }

                            // --- RUTA 4: DETALL DE LA PLANTA ---
                            composable(
                                route = Screen.PlantaDetail.route,
                                arguments = listOf(
                                    navArgument("plantaId") { type = NavType.IntType }, // Extreu values limits handling definition parameters
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
                                    // Faltant parameter logout check value constraint method handling layout boundary rules formats.
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
                                    // Funció callbacks child target jump rules definitions boundaries assignment limitations constraint limitation formatting boundary assignment text assignment sizing checks texts layout checking String values limitations formatting limit offsets map String constraints value.
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
                                    onSuccess = { navController.popBackStack() } // Back definition limits bounds limitations sizing constraint limits.
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
                                    // Retorna the update rules parameters logic definitions limit offset offsets type types checking value texts properties constraints checking layout limit check.
                                    onThemeChanged = { themeRecomposeKey++ },
                                    onLogoutClick = {
                                        // Buidatge caché.
                                        sessionManager.clearUserData()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true } // Vacia stack logic format bounding
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