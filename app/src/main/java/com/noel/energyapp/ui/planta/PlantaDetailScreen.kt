package com.noel.energyapp.ui.planta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noel.energyapp.ui.components.NoelScreen

@Composable
fun PlantaDetailScreen(
    paddingValues: PaddingValues,
    plantaId: Int,
    plantaNom: String,
    onBackClick: () -> Unit,
    onNavigateToAlarmesActives: () -> Unit
) {
    // --- FEM SERVIR LA PLANTILLA MESTRE NOELSCREEN ---
    // Li passem el nom de la planta al title, i activem la fletxa i el menú
    NoelScreen(
        paddingValues = paddingValues,
        title = plantaNom,
        hasMenu = true,
        onBackClick = onBackClick, // Això activa la fletxa de tornar enrere automàticament
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Botó / Targeta: Consums Actuals (Blau corporatiu)
        DetailCard(
            text = "CONSUMS ACTUALS",
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = { /* Navegar a consums */ }
        )

        // 2. Botó / Targeta: M3 Consumits
        DetailCard(
            text = "M3 CONSUMITS",
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = { /* Navegar a m3 */ }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Botó / Targeta: Alarmes Actives (Vermell d'alerta)
        DetailCard(
            text = "ALARMES ACTIVES",
            containerColor = MaterialTheme.colorScheme.error,
            onClick = { onNavigateToAlarmesActives() }
        )

        // 4. Botó / Targeta: Històric Alarmes
        DetailCard(
            text = "HISTÒRIC ALARMES",
            containerColor = MaterialTheme.colorScheme.secondary, // Un color diferent per diferenciar
            onClick = { /* Navegar a històric */ }
        )
    }
}

@Composable
fun DetailCard(
    text: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    // --- FEM SERVIR LA TARJETA DETALLADA ---
    Card(
        modifier = Modifier
        .fillMaxWidth()
        .height(100.dp) // Fem les targetes altes i "clicables"
        .padding(vertical = 8.dp)
        .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}