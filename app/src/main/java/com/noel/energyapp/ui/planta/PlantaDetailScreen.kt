/**
 * FITXER: PlantaDetailScreen.kt
 * CAPA: Interfície d'usuari → Planta (ui/planta)
 *
 * Aquesta pantalla serveix de punt d'accés principal (hub central) a totes les
 * funcionalitats d'una planta seleccionada des del Dashboard.
 *
 * Funcionalitats:
 * 1. Mostra un menú de pestanyes arrodonides (estil glassmorphism) per alternar
 *    entre opcions de "Consums" i opcions d'"Alarmes".
 * 2. Secció de Consums:
 *    - Consums Actuals: dades SCADA en temps real.
 *    - Anàlisi de Consum: gràfiques de consum històric agregat (m³).
 *    - Gestió de Registres: llistat de valors horaris (només per a ADMIN, ID Rol == 1).
 * 3. Secció d'Alarmes:
 *    - Alarmes Actives: llistat d'incidències que cal tancar.
 *    - Històric d'Alarmes: incidències del passat per aquesta planta.
 * 4. Utilitza els 'NoelPremiumButton' que apliquen gradents espectaculars i subtítols explicatius.
 */
package com.noel.energyapp.ui.planta

import android.content.res.Configuration
// Importacions de maquinària Jetpack Compose (layout, estils, dibuix)
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// Diferents icones utilitzades per representar les seccions del menú (gràfiques, gota, registres, alarmes)
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
// Importància per al mode Landscape / Portrait (ja no és utilitzada aquí però ho deixem pel context)
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Components Reutilitzables
import com.noel.energyapp.ui.components.NoelPremiumButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.*

@Composable
fun PlantaDetailScreen(
    paddingValues: PaddingValues,         // Pàdding donat pel root (evitant solapament amb nav bars)
    plantaId: Int,                        // ID de la planta a què hem passat
    plantaNom: String,                    // Nom per indicar el títol a la AppBar
    onBackClick: () -> Unit,              // Crida si el root té icona enrera
    // Llista de lambda-callbacks del NavHost
    onNavigateToAlarmesActives: () -> Unit,
    onNavigateToAlarmesHistoric: () -> Unit,
    onNavigateToConsumGrafica: () -> Unit,
    onNavigateToConsumsActuals: () -> Unit,
    onNavigateToConsumRegistres: () -> Unit,
    userRole: String?,                    // Rol textual (potser debug)
    userRoleId: Int                       // Clau per control d'accessos en frontend (ex: Admin = 1)
) {
    // Variable per llegir si el dispositiu està rotat horitzontal o no
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // selectedTabIndex ens guardarà quin dels Tabs (0 = consums, 1 = alarmes) estem visualitzant actualment
    var selectedTabIndex by remember { mutableStateOf(0) }
    // Defineix textuals i ordre dels tabs de menú superior de la pantalla
    val tabs = listOf("🌍 Consums", "⚠️ Alarmes")

    NoelScreen(
        paddingValues = paddingValues,
        title = plantaNom, // Escriu el nom de la planta dalt a la Toolbar (e.g. Noel-1)
        verticalArrangement = Arrangement.Top
    ) {
        
        // --- 1. GLASS TABS (Selector entre Consums o Alarmes) ---
        // S'usa Surface per la facilitat de fer l'efecte de requadre arrodonit
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = DarkSurfaceVariant.copy(alpha = 0.5f), // Transparència de fons com a Glass Efect
            border = BorderStroke(1.dp, GlassWhiteStroke)
        ) {
            // Contenidor tipus Fila que s'adapta al Max Size assignant part de les mateixes proporcions
            Row(modifier = Modifier.fillMaxSize()) {
                // Generem Box clicable per a cada index del Tabs declarat abans ["Consums", "Alarmes"]
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f) // Prengui el seu tros corresponent de row repartits en els tabs presents
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(28.dp)) // Clipping al box, perquè si es pitgi un cantó quedi bé
                            // Només s'acolorix el fons si está Selected
                            .background(if (isSelected) PremiumBlueStart.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedTabIndex = index }, // Actua canviant l'estat local del record
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            // El blanc mig fons fa que els no focus es visualitzin més llurs text que abans per la UI
                            color = if (isSelected) PremiumBlueStart else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // --- 2. PREMIUM CONTENT (Menjar o Opcions dintre del TAB seleccionat) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Es requereix scroll si les plantilles cauen sota l'altura vertical
            verticalArrangement = Arrangement.spacedBy(20.dp) // Genera separació de boxes sense padding manuals per tot arreu
        ) {
            // Evaluació d'estalvi: Mostra la UI respecte les pestanyes escollides (0=Consums , 1=Alarmes)
            if (selectedTabIndex == 0) {
                // SECCIÓ CONSUMS
                NoelPremiumButton(
                    title = "Consums Actuals",
                    subtitle = "Dades en temps real de SCADA", // Context d'ajut al button
                    icon = Icons.Default.WaterDrop,
                    gradient = Brush.linearGradient(listOf(PremiumBlueStart, PremiumBlueEnd)),
                    onClick = onNavigateToConsumsActuals
                )

                NoelPremiumButton(
                    title = "Anàlisi de Consum",
                    subtitle = "Gràfiques històriques i m³", // Context
                    icon = Icons.Default.DateRange, // Icona calendarial
                    gradient = Brush.linearGradient(listOf(PremiumTealStart, PremiumTealEnd)), // Verd Aigua Gradient per trencar estètica totalment blava
                    onClick = onNavigateToConsumGrafica
                )

                // NOVEL BOTO: GESTIÓ DE REGISTRES (Basat en l'ID del Rol per la visualització restringida)
                // 1 = ADMIN (A la Rest API s'ha definit ROLE ID=1 ADMIN, ROLE ID=2 SUPERVISOR)
                if (userRoleId == 1) {
                    NoelPremiumButton(
                        title = "Gestió de Registres",
                        subtitle = "Consulta i correcció per dia unitari",
                        icon = Icons.Default.List,
                        // Utilitza tons taronges d'administrador/perills per l'alteració de bases
                        gradient = Brush.linearGradient(listOf(PremiumOrangeStart, PremiumOrangeEnd)),
                        onClick = onNavigateToConsumRegistres // Aixeca el screen amb listat d'edició
                    )
                }
            } else {
                // SECCIÓ ALARMES (selectedTabIndex = 1)
                NoelPremiumButton(
                    title = "Alarmes Actives",
                    subtitle = "Incidències crítiques pendents",
                    icon = Icons.Default.Warning,
                    gradient = Brush.linearGradient(listOf(AlarmCriticaRed, Color(0xFF660000))), // Rojo profund per ressaltar alertes no ateses
                    onClick = onNavigateToAlarmesActives
                )

                NoelPremiumButton(
                    title = "Històric d'Alarmes",
                    subtitle = "Històric d'incidències tancades",
                    icon = Icons.Default.Notifications,
                    gradient = Brush.linearGradient(listOf(PremiumPurpleStart, PremiumPurpleEnd)), // Ton purpura per contrast i distensió amb Alarm rosses
                    onClick = onNavigateToAlarmesHistoric
                )
            }
            // Fix general per assegurar-se de l'altura extra que ocupa en Navigation Barre Bottom
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}