package com.noel.energyapp.ui.dashboard

import com.noel.energyapp.ui.theme.isAppInDarkTheme as isSystemInDarkTheme
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
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.navigation.Screen
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.GlassCard
import com.noel.energyapp.ui.components.NoelPremiumButton
import com.noel.energyapp.ui.theme.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import com.noel.energyapp.util.SessionManager

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    onPlantaClick: (Int, String) -> Unit,
    userName: String?
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    var alarmes by remember { mutableStateOf<List<IncidenciaVistaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val realName = sessionManager.fetchUserRealName()
    val displayGreeting = if (!realName.isNullOrBlank()) realName else userName ?: "Usuari"
    val assignedPlants = sessionManager.fetchAssignedPlants()

    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            try {
                val rPlantes = RetrofitClient.instance.getPlantes("Bearer $token")
                if (rPlantes.isSuccessful) {
                    val totes = rPlantes.body() ?: emptyList()
                    plantes = totes.filter { it.activa && assignedPlants.contains(it.id_planta) }
                }
                val rAlarmes = RetrofitClient.instance.getAlarmesActives("Bearer $token")
                if (rAlarmes.isSuccessful) {
                    alarmes = rAlarmes.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val greetingColor = if (isDark) Color.White.copy(alpha = 0.9f) else LightOnBackground
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.5f) else LightOnSurfaceVariant

    NoelScreen(
        paddingValues = paddingValues,
        title = null
    ) {
        // --- 1. GREETING MIDA NORMAL ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Benvingut, $displayGreeting",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = greetingColor
            )
        }

        Text(
            text = "ESTAT DE LES PLANTES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = if (isDark) PremiumBlueStart.copy(alpha = 0.8f) else LightPrimary.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = if (isDark) PremiumBlueStart else LightPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                plantes.forEach { planta ->
                    PremiumPlantaCard(
                        planta = planta,
                        alarmes = alarmes,
                        onClick = { onPlantaClick(planta.id_planta, planta.nom_planta) }
                    )
                }
                Spacer(modifier = Modifier.height(110.dp)) // Espai extra per la barra inferior
            }
        }
    }
}

@Composable
private fun PremiumPlantaCard(
    planta: PlantaDto,
    alarmes: List<IncidenciaVistaDto>,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val pAlarmes = alarmes.filter { it.ubicacio.contains(planta.nom_planta, ignoreCase = true) }
    val criticalCount = pAlarmes.count { it.gravetat.contains("CRIT", ignoreCase = true) }
    val totalCount = pAlarmes.size

    // MODE FOSC: Tornem a l'estil anterior (Glassmorphism més pur)
    // MODE CLAR: Mantenim el Blau Aigua que t'ha agradat per harmonitzar
    val cardBgColor = if (isDark) {
        Color.White.copy(alpha = 0.05f) // Molt més transparent, estil Glassmorphism real
    } else {
        LightWaterBlue
    }

    val cardBorderColor = if (isDark) {
        GlassWhiteStroke.copy(alpha = 0.5f)
    } else {
        LightWaterBlueStroke
    }

    val textColor = if (isDark) Color.White else LightOnSurface
    val statusColor = if (totalCount > 0) AlarmAlertaOrange else (if (isDark) PremiumTealStart else LightPrimary)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp), // Una mica més arrodonit
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona d'estat a l'esquerra
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusColor.copy(alpha = if (isDark) 0.1f else 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (totalCount > 0) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planta.nom_planta,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = textColor
                )
                Text(
                    text = if (totalCount > 0) {
                        if (criticalCount > 0) "$criticalCount CRÍTIQUES / $totalCount TOTALS"
                        else "$totalCount INCIDÈNCIES ACTIVES"
                    } else "SISTEMA CORRECTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = statusColor.copy(alpha = 0.9f)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = (if (isDark) Color.White else Color.Black).copy(alpha = 0.2f)
            )
        }
    }
}