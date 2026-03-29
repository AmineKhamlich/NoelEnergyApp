package com.noel.energyapp.ui.planta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noel.energyapp.ui.components.NoelPremiumButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.*

@Composable
fun PlantaDetailScreen(
    paddingValues: PaddingValues,
    plantaId: Int,
    plantaNom: String,
    onBackClick: () -> Unit,
    onNavigateToAlarmesActives: () -> Unit,
    onNavigateToAlarmesHistoric: () -> Unit,
    onNavigateToConsumGrafica: () -> Unit,
    onNavigateToConsumsActuals: () -> Unit,
    onNavigateToConsumRegistres: () -> Unit,
    userRole: String?,
    userRoleId: Int // ID NUMÈRIC DEL ROL
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("🌍 Consums", "⚠️ Alarmes")

    NoelScreen(
        paddingValues = paddingValues,
        title = plantaNom,
        verticalArrangement = Arrangement.Top
    ) {
        
        // --- 1. GLASS TABS ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = DarkSurfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, GlassWhiteStroke)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(28.dp))
                            .background(if (isSelected) PremiumBlueStart.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedTabIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) PremiumBlueStart else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // --- 2. PREMIUM CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (selectedTabIndex == 0) {
                // SECCIÓ CONSUMS
                NoelPremiumButton(
                    title = "Consums Actuals",
                    subtitle = "Dades en temps real de SCADA",
                    icon = Icons.Default.WaterDrop,
                    gradient = Brush.linearGradient(listOf(PremiumBlueStart, PremiumBlueEnd)),
                    onClick = onNavigateToConsumsActuals
                )

                NoelPremiumButton(
                    title = "Anàlisi de Consum",
                    subtitle = "Gràfiques històriques i m³",
                    icon = Icons.Default.DateRange,
                    gradient = Brush.linearGradient(listOf(PremiumTealStart, PremiumTealEnd)),
                    onClick = onNavigateToConsumGrafica
                )

                // NOVEL BOTO: GESTIÓ DE REGISTRES (Basat en l'ID del Rol)
                // 1 = ADMIN
                if (userRoleId == 1) {
                    NoelPremiumButton(
                        title = "Gestió de Registres",
                        subtitle = "Consulta i correcció per dia unitari",
                        icon = Icons.Default.List,
                        gradient = Brush.linearGradient(listOf(PremiumOrangeStart, PremiumOrangeEnd)),
                        onClick = onNavigateToConsumRegistres
                    )
                }
            } else {
                // SECCIÓ ALARMES
                NoelPremiumButton(
                    title = "Alarmes Actives",
                    subtitle = "Incidències crítiques pendents",
                    icon = Icons.Default.Warning,
                    gradient = Brush.linearGradient(listOf(AlarmCriticaRed, Color(0xFF660000))),
                    onClick = onNavigateToAlarmesActives
                )

                NoelPremiumButton(
                    title = "Històric d'Alarmes",
                    subtitle = "Històric d'incidències tancades",
                    icon = Icons.Default.Notifications,
                    gradient = Brush.linearGradient(listOf(PremiumPurpleStart, PremiumPurpleEnd)),
                    onClick = onNavigateToAlarmesHistoric
                )
            }
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}