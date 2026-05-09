/**
 * FITXER: DashboardScreen.kt
 * CAPA: Interfície d'usuari → Dashboard (ui/dashboard)
 *
 * Aquesta pantalla és la pantalla principal de l'aplicació un cop l'usuari ha iniciat sessió.
 * Mostra la llista de plantes assignades a l'usuari autenticat amb el seu estat d'alarmes en temps real.
 *
 * En carregar la pantalla:
 * 1. Obté totes les plantes actives del sistema via API.
 * 2. Les filtra per les plantes assignades a l'usuari (guardades al SessionManager).
 * 3. Carrega les alarmes actives per saber quines plantes tenen incidències pendents.
 *
 * Cada planta es mostra com una targeta premium ('PremiumPlantaCard') que indica
 * de forma visual si la planta està en estat correcte o té alarmes actives/crítiques.
 * En tocar una planta, l'usuari navega a la pantalla de detall d'aquesta planta.
 *
 * La salutació a dalt es personalitza amb el nom real de l'usuari si n'hi ha.
 * El disseny s'adapta automàticament al mode fosc o clar del tema seleccionat.
 */
package com.noel.energyapp.ui.dashboard

// Importació de la funció de detecció del tema de l'App (fosc/clar) com a alias clar
import com.noel.energyapp.ui.theme.isAppInDarkTheme as isSystemInDarkTheme
// Importació dels components bàsics de disseny de Jetpack Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Importació dels models de dades
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.navigation.Screen
// Importació del client de xarxa i components reutilitzables de l'App
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.GlassCard
import com.noel.energyapp.ui.components.NoelPremiumButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.noelReveal
import com.noel.energyapp.ui.theme.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.noel.energyapp.util.SessionManager

// Composable principal del Dashboard, rep el callback de navegació a la planta i el nom d'usuari
@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,           // Marges del sistema Android (barres superior i inferior)
    onPlantaClick: (Int, String) -> Unit,   // Callback que rep l'ID i el nom de la planta pulsada
    userName: String?                        // Nick de l'usuari rebut del SessionManager (fallback si no hi ha nom real)
) {
    // Obté el context d'Android per al SessionManager
    val context = LocalContext.current
    // Instancia el SessionManager per llegir les dades de la sessió local
    val sessionManager = remember { SessionManager(context) }

    // Estat que emmagatzema la llista de plantes filtrades per a l'usuari actual
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    // Estat que emmagatzema totes les alarmes actives (per saber quines plantes en tenen)
    var alarmes by remember { mutableStateOf<List<IncidenciaVistaDto>>(emptyList()) }
    // Estat booleà que controla la visibilitat de la rodeta de càrrega
    var isLoading by remember { mutableStateOf(true) }

    // Recupera el nom real (pila + cognom) per a la salutació personalitzada
    val realName = sessionManager.fetchUserRealName()
    // Si hi ha nom real, s'usa per a la salutació; si no, s'usa el nick o "Usuari" com a fallback
    val displayGreeting = if (!realName.isNullOrBlank()) realName else userName ?: "Usuari"
    // Recupera la llista d'IDs de plantes assignades a l'usuari per al filtratge
    val assignedPlants = sessionManager.fetchAssignedPlants()

    // Executa les crides a l'API en muntar el composable per primera vegada
    LaunchedEffect(Unit) {
        // Obté el token JWT de la sessió local
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            try {
                // 1. Obté totes les plantes actives del sistema
                val rPlantes = RetrofitClient.instance.getPlantes("Bearer $token")
                if (rPlantes.isSuccessful) {
                    val totes = rPlantes.body() ?: emptyList()
                    // Filtra: deixa només les plantes actives I que estiguin assignades a l'usuari
                    plantes = totes.filter { it.activa && assignedPlants.contains(it.id_planta) }
                }
                // 2. Obté totes les alarmes actives per mostrar l'indicador de risc a cada planta
                val rAlarmes = RetrofitClient.instance.getAlarmesActives("Bearer $token")
                if (rAlarmes.isSuccessful) {
                    alarmes = rAlarmes.body() ?: emptyList() // Actualitza la llista d'alarmes
                }
            } catch (e: Exception) {
                // Si hi ha un error de xarxa, el registra al Logcat sense tancar l'App
                e.printStackTrace()
            } finally {
                // Sempre amaga la rodeta de càrrega en acabar, hagi anat bé o malament
                isLoading = false
            }
        }
    }

    // Determina si el tema actiu és fosc per adaptar els colors de la UI
    val isDark = isSystemInDarkTheme()
    // Color de la salutació adaptat al tema: blanc en fosc, fosc en clar
    val greetingColor = if (isDark) Color.White.copy(alpha = 0.9f) else LightOnBackground
    // Color del subtítol de secció, més subtil que el de la salutació
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.5f) else LightOnSurfaceVariant

    // Renderitza l'estructura base de la pantalla (sense títol: el Dashboard té salutació pròpia)
    NoelScreen(
        paddingValues = paddingValues,
        title = null  // Sense títol a la capçalera: es mostra la salutació personalitzada al contingut
    ) {
        // --- SALUTACIÓ PERSONALITZADA ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Benvingut, $displayGreeting", // Nom de l'usuari en la salutació
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = greetingColor
            )
        }

        // --- TÍTOL DE SECCIÓ "ESTAT DE LES PLANTES" ---
        Text(
            text = "ESTAT DE LES PLANTES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp  // Espaiat de lletres per a estètica premium
            ),
            color = if (isDark) PremiumBlueStart.copy(alpha = 0.8f) else LightPrimary.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        if (isLoading) {
            // Mostra la rodeta de progrés centrada mentre es carreguen les dades
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = if (isDark) PremiumBlueStart else LightPrimary)
            }
        } else {
            // Quan les dades han carregat, mostra la llista de plantes en columna scrollable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()), // Permet desplaçar-se si hi ha moltes plantes
                verticalArrangement = Arrangement.spacedBy(16.dp) // Espai de 16dp entre cada targeta de planta
            ) {
                // Per cada planta de la llista filtrada, renderitza una targeta premium
                plantes.forEachIndexed { index, planta ->
                    PremiumPlantaCard(
                        planta = planta,
                        alarmes = alarmes, // Passa totes les alarmes per calcular l'estat de la planta
                        animationDelayMillis = (index * 70).coerceAtMost(350),
                        onClick = { onPlantaClick(planta.id_planta, planta.nom_planta) } // Navega al detall
                    )
                }
                // Espai extra al final per evitar que l'última targeta quedi tapada per la barra de navegació
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

// Targeta premium que representa una planta al Dashboard amb indicadors d'estat visuals
// Mostra el nom de la planta i un resum de les alarmes actives (cap, avis, o crítiques)
@Composable
private fun PremiumPlantaCard(
    planta: PlantaDto,                   // Dades de la planta a mostrar
    alarmes: List<IncidenciaVistaDto>,   // Totes les alarmes actives per calcular les de la planta concreta
    animationDelayMillis: Int = 0,
    onClick: () -> Unit                  // Callback que s'executa en tocar la targeta
) {
    // Determina si el tema actiu és fosc per adaptar els colors de la targeta
    val isDark = isSystemInDarkTheme()

    // Filtra les alarmes que pertanyen a la ubicació d'aquesta planta concreta
    val pAlarmes = alarmes.filter { it.ubicacio.contains(planta.nom_planta, ignoreCase = true) }
    // Compta les alarmes crítiques (HH) dins de les alarmes de la planta
    val criticalCount = pAlarmes.count { it.gravetat.contains("CRIT", ignoreCase = true) }
    // Nombre total d'alarmes actives de la planta
    val totalCount = pAlarmes.size

    // Color de fons de la targeta adaptat al tema: Glassmorphism en fosc, blau suau en clar
    val cardBgColor = if (isDark) {
        Color.White.copy(alpha = 0.05f) // Molt transparent per a l'efecte glassmorphism en mode fosc
    } else {
        LightWaterBlue // Color blau aiguat subtil per al mode clar
    }

    // Color del contorn de la targeta adaptat al tema
    val cardBorderColor = if (isDark) {
        GlassWhiteStroke.copy(alpha = 0.5f) // Contorn blanc semitransparent en fosc
    } else {
        LightWaterBlueStroke // Contorn blau suau en clar
    }

    // Color del text dels noms adaptat al tema
    val textColor = if (isDark) Color.White else LightOnSurface
    // Color d'estat: taronja si hi ha alarmes, blau si tot va bé
    val statusColor = if (totalCount > 0) AlarmAlertaOrange else (if (isDark) PremiumTealStart else LightPrimary)

    // Surface clicable que serveix de base de la targeta amb contorn i elevació adaptats
    Surface(
        onClick = onClick,                            // Navega al detall de la planta en tocar
        modifier = Modifier
            .fillMaxWidth()
            .noelReveal(delayMillis = animationDelayMillis),
        shape = RoundedCornerShape(28.dp),            // Cantonades molt arrodonides per a disseny premium
        color = cardBgColor,                          // Color de fons adaptat al tema
        border = BorderStroke(1.dp, cardBorderColor), // Contorn d'1dp adaptat al tema
        shadowElevation = if (isDark) 0.dp else 4.dp // Ombra només en mode clar (el fosc usa glassmorphism)
    ) {
        Row(
            modifier = Modifier.padding(20.dp), // Padding intern per a la targeta
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ICONA D'ESTAT ESQUERRA ---
            Box(
                modifier = Modifier
                    .size(54.dp) // Mida fixa per a la caixa de la icona
                    .clip(RoundedCornerShape(16.dp)) // Cantonades arrodonides per a la caixa
                    .background(statusColor.copy(alpha = if (isDark) 0.1f else 0.15f)), // Fons del color d'estat
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    // Icona Warning si hi ha alarmes, Info si tot és correcte
                    imageVector = if (totalCount > 0) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = statusColor,         // Color de la icona segueix l'estat de la planta
                    modifier = Modifier.size(28.dp) // Mida de la icona
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Espai entre la icona i el text

            // --- INFORMACIÓ DE LA PLANTA ---
            Column(modifier = Modifier.weight(1f)) { // Ocupa tot l'espai disponible entre icona i fletxa
                Text(
                    text = planta.nom_planta, // Nom de la planta en gran
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = textColor
                )
                Text(
                    // Subtítol d'estat: mostra compte d'alarmes si n'hi ha, o "SISTEMA CORRECTE" si no
                    text = if (totalCount > 0) {
                        if (criticalCount > 0) "$criticalCount CRÍTIQUES / $totalCount TOTALS"
                        else "$totalCount INCIDÈNCIES ACTIVES"
                    } else "SISTEMA CORRECTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = statusColor.copy(alpha = 0.9f) // Mateix color que la icona d'estat
                )
            }

            // --- FLETXA DE NAVEGACIÓ ---
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight, // Fletxa dreta
                contentDescription = null,
                // Transparència alta: és un indicador visual suau, no el focus principal
                tint = (if (isDark) Color.White else Color.Black).copy(alpha = 0.2f)
            )
        }
    }
}
