/**
 * FITXER: AjustosScreen.kt
 * CAPA: Interfície d'usuari → Ajustos (ui/ajustos)
 *
 * Aquesta pantalla permet a l'usuari consultar les dades del seu perfil
 * i modificar les preferències de l'aplicació, així com tancar la sessió.
 *
 * Funcionalitats:
 * 1. Mostra el nom real de l'usuari i el seu rol (marcats clarament per a verificació).
 * 2. Permet triar el tema visual de l'App: "AUTO" (segueix el sistema), "LIGHT" (clar) o "DARK" (fosc).
 * 3. Permet activar o desactivar les animacions fluides de la UI.
 * 4. Botó per tancar la sessió de forma segura (esborra dades locals i torna a Login).
 */
package com.noel.energyapp.ui.ajustos

// Importació de components interactius
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// Importació de la icona per a tancar sessió amb suport "AutoMirrored" per a idiomes RTL
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Importació de components propis i gestió de sessió
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelDashboardButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.PremiumBlueStart
import com.noel.energyapp.ui.theme.PremiumLogoutRed
import com.noel.energyapp.util.SessionManager

// Composable de la pantalla de Perfil i Ajustos
@Composable
fun AjustosScreen(
    paddingValues: PaddingValues, // Marges segurs dalt i baix (System Insets)
    onBackClick: () -> Unit,      // Callback de fletxa endarrere (si apliqués)
    onThemeChanged: () -> Unit,   // Callback per reconstruir la UI després de canviar el tema
    onLogoutClick: () -> Unit     // Callback per dir-li al NavHost que vagi a la pantalla de Login
) {
    // Context per a accedir al 'SharedPreferences' usant SessionManager
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Obtenim de persistència el valor de les preferències de l'usuari
    var themePreference by remember { mutableStateOf(sessionManager.fetchThemePreference()) }
    var animationsEnabled by remember { mutableStateOf(sessionManager.fetchAnimationsEnabled()) }

    // Obtenim dades de l'usuari actual: si no hi ha nom real, només mostra l'usari (fallback: "Usuari")
    val userRealName = sessionManager.fetchUserRealName() ?: sessionManager.fetchUserName() ?: "Usuari"
    // Obtenim el textual role de l'usuari ("ADMIN", "TECNIC" o "SUPERVISOR")
    val userRole = sessionManager.fetchUserRole() ?: "Sense Rol"

    // Component Screen envolta el layout general
    NoelScreen(
        paddingValues = paddingValues,
        title = "PERFIL I AJUSTOS", // Títol dalt de la pantalla
        verticalArrangement = Arrangement.Top // Organitzem elements cap a dalt
    ) {
        // --- 1. DADES DEL PERFIL ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            // Utilitzem un fons més subtil de color SurfaceVariant (amb alfa 0.5f) pel disseny Premium
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp) // Voreres generosament arrodonides
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Nom de l'usuari a la carta, sempre en majúscules
                Text(
                    text = userRealName.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                // Badge amb el ROL sota el nom, usant colors cridaners i clars
                Surface(
                    color = PremiumBlueStart.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = " ROL: $userRole ", // Ex. ROL: ADMIN
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = PremiumBlueStart,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. TEMA DE L'APLICACIÓ ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TEMA DE L'APLICACIÓ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary, // Lletra en primary
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Caixa que inclou els 3 tipus de modes ("AUTO", "LIGHT", "DARK")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Iteració sobre les tres opcions valides per al mode dark/light
                    listOf("AUTO", "LIGHT", "DARK").forEach { mode ->
                        // Una columna per crear la fila de RadioButton + Text dalt i baix
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // Fa la columna clickable per una millor experiència amb interfícies tàctils
                            modifier = Modifier.clickable {
                                themePreference = mode
                                sessionManager.saveThemePreference(mode) // Es guarda a "SharedPreferences"
                                onThemeChanged()                         // Fa disparar la redibuix a main per canvi
                            }
                        ) {
                            RadioButton(
                                selected = themePreference == mode,
                                onClick = { // Igual que l'element clickable exterior
                                    themePreference = mode
                                    sessionManager.saveThemePreference(mode)
                                    onThemeChanged()
                                }
                            )
                            Text(text = mode, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. ANIMACIONS I UX ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Mode surface amb alt contrast propis del mode on es mostren toggle buttons
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Animacions fluides", fontWeight = FontWeight.Medium)
                // Switch que toggleja variable animacions entre habilitades i no de manera local dins el telèfon
                Switch(
                    checked = animationsEnabled,
                    onCheckedChange = {
                        animationsEnabled = it // Assignar localment al valor state
                        sessionManager.saveAnimationsEnabled(it) // Commit en el gestor Local
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- 4. TANCAR SESSIÓ ---
        // S'utilitza el component "NoelDashboardButton" per un look "Premium"
        NoelDashboardButton(
            title = "Tancar Sessió",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            // Mode "Red", vermell per cridar l'atenció i indicar acció amb conseqüència final/bloquejant
            containerColor = PremiumLogoutRed,
            onClick = {
                sessionManager.clearUserData() // Neteja tots els shared keys
                onLogoutClick() // Causa la crida per netejar la taula de back stack per deixar Login com inici
            }
        )
    }
}
