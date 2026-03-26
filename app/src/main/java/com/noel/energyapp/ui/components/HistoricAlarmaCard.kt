package com.noel.energyapp.ui.components

import androidx.compose.foundation.clickable
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
import com.noel.energyapp.ui.theme.DarkSlate
import com.noel.energyapp.ui.theme.StatusGreen
import com.noel.energyapp.ui.theme.SurfaceLight

@Composable
fun HistoricAlarmaCard(
    alarma: IncidenciaVistaDto,
    onCardClick: () -> Unit = {}
) {
    val cardColor    = SurfaceLight
    val contentColor = DarkSlate
    val greenColor   = StatusGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── CAPÇALERA ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle, null, tint = greenColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "TANCADA", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold, color = greenColor
                    )
                }
                Surface(
                    color = contentColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        alarma.gravetat, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── UBICACIÓ I DESCRIPCIÓ ─────────────────────────────────────
            Text(
                "📍 ${alarma.ubicacio}", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (!alarma.descripcioComptador.isNullOrBlank()) {
                Text(
                    alarma.descripcioComptador, style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── DATES ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Data notificació", style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.55f)
                    )
                    Text(
                        alarma.dataCreacio ?: "—",
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Data tancament", style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.55f)
                    )
                    Text(
                        alarma.dataTancament ?: "—",
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold
                    )
                }
            }

            if (alarma.tempsTranscorregut.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        "Temps transcurregut: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.65f)
                    )
                    Text(
                        alarma.tempsTranscorregut,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── CONSUM I SETPOINTS ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Consum dia notificació", style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.55f)
                    )
                    Text(
                        "${String.format("%.2f", alarma.consumDiaAlarma)} m³",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Límits H / HH", style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.55f)
                    )
                    Text(
                        "${alarma.limitH ?: "—"} / ${alarma.limitHH ?: "—"} m³",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── TÈCNIC ───────────────────────────────────────────────────
            if (!alarma.tecnicTancament.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tancat per: ${alarma.tecnicTancament}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.65f)
                )
            }
        }
    }
}
