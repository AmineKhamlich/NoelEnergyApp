package com.noel.energyapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.IncidenciaVistaDto

@Composable
fun HistoricAlarmaCard(
    alarma: IncidenciaVistaDto
) {
    // Utilitzem colors neutres (grisos) per indicar que és una alarma tancada
    val cardColor = Color(0xFFF5F5F5) // Gris molt clar
    val contentColor = Color(0xFF455A64) // Gris blavós fosc per al text

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CAPÇALERA: Icona de fet + Estat + Data Tancament
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Tancada",
                        tint = Color(0xFF4CAF50) // Verd per indicar Check
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TANCADA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                // Data de quan es va resoldre
                Text(
                    text = "Resolta: ${alarma.dataTancament ?: "Sense data"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TÍTOL I MOTIU
            Text(
                text = alarma.detallAlarma,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // UBICACIÓ I EQUIP
            Text(
                text = "📍 ${alarma.ubicacio} | ⚙️ ${alarma.comptador}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // INFORMACIÓ DEL CONSUM QUE VA PROVOCAR L'ALARMA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Consum registrat", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
                    Text("${String.format("%.2f", alarma.consumRealAvui)} m³", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Límits configurats", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
                    Text("${alarma.limitH ?: "-"} / ${alarma.limitHH ?: "-"} m³", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
