package com.noel.energyapp.ui.ajustos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelDashboardButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.PremiumBlueStart
import com.noel.energyapp.ui.theme.PremiumLogoutRed
import com.noel.energyapp.util.SessionManager

@Composable
fun AjustosScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onThemeChanged: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var themePreference by remember { mutableStateOf(sessionManager.fetchThemePreference()) }
    var animationsEnabled by remember { mutableStateOf(sessionManager.fetchAnimationsEnabled()) }
    val userRealName = sessionManager.fetchUserRealName() ?: sessionManager.fetchUserName() ?: "Usuari"
    val userRole = sessionManager.fetchUserRole() ?: "Sense Rol"
    NoelScreen(
        paddingValues = paddingValues,
        title = "PERFIL I AJUSTOS",
        verticalArrangement = Arrangement.Top
    ) {
        // --- 1. DADES DEL PERFIL (Millorat el contrast) ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = userRealName.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = PremiumBlueStart.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = " ROL: $userRole ",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = PremiumBlueStart,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        // --- 2. TEMA DE L'APLICACIÓ (Arreglat el problema de la foto) ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TEMA DE L'APLICACIÓ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("AUTO", "LIGHT", "DARK").forEach { mode ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                themePreference = mode
                                sessionManager.saveThemePreference(mode)
                                onThemeChanged()
                            }
                        ) {
                            RadioButton(
                                selected = themePreference == mode,
                                onClick = {
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Animacions fluides", fontWeight = FontWeight.Medium)
                Switch(
                    checked = animationsEnabled,
                    onCheckedChange = {
                        animationsEnabled = it
                        sessionManager.saveAnimationsEnabled(it)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        // --- 4. TANCAR SESSIÓ ---
        NoelDashboardButton(
            title = "Tancar Sessió",
            //subtitle = "Sortir del compte de manera segura",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            // Per evitar que la caixa sembli doble en estils clars per causa de la transparència
            containerColor = PremiumLogoutRed,
            onClick = {
                sessionManager.clearUserData()
                onLogoutClick()
            }
        )
    }
}
