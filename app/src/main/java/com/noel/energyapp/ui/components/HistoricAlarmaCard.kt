/**
 * FITXER: HistoricAlarmaCard.kt
 * CAPA: Interfície d'usuari → Components (ui/components)
 *
 * Versió reduïda i inactiva de l'AlarmaCard. Aquesta es fa servir exclusivament 
 * a la pantalla d'Històric (Alarmes tancades). 
 *
 * Funcionalitats:
 * 1. Omet el botó blau actiu per evitar modificacions d'incidents resolts.
 * 2. Exposa el Tècnic que ha executat el tancament així com dates comparatives inicial-final limits properties assignment text handling logic styles sizes definitions mapping definition checking constraint object method values text object logic offsets variable boolean limits.
 */
package com.noel.energyapp.ui.components

import com.noel.energyapp.ui.theme.isAppInDarkTheme as isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
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
// Dto tipologia Incident API C# JSON
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.ui.theme.DarkSlate
import com.noel.energyapp.ui.theme.StatusGreen
import com.noel.energyapp.ui.theme.SurfaceLight

@Composable
fun HistoricAlarmaCard(
    alarma: IncidenciaVistaDto,       // Model Incident Rebut de col·lecció Dto object property modifier styles mapping offsets checking
    onCardClick: () -> Unit = {}      // Mètode clicable sencera Card rules limit definition modifier variables object variables definition
) {
    // Colors dinàmics igual que els del Dashboard / Card actives type definition size structure text assignment logic offset boolean property rules checks method sizes sizes constraint properties definitions layout offset.
    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) Color.White.copy(alpha = 0.05f) else SurfaceLight
    val contentColor = if (isDark) Color.White else DarkSlate
    val borderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Transparent
    val greenColor   = StatusGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }, // Tota la card és botó
        colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 3.dp),
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── CAPÇALERA (Tancada status) ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador verd clar success
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
                Surface( // Quadre gris suau que recorda de quina gravetat provenia limits format offset definition style handler logic values.
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
            Text( // Punt fixe text structure parameter definition string limit parameter rule styles format method properties logic value sizing handling Boolean limit value handler properties constraint checking sizes variables text sizes String definition limit layout type mapping
                "📍 ${alarma.ubicacio}", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            // Equip causant de la fallada si en té description (Null safe).
            if (!alarma.descripcioComptador.isNullOrBlank()) {
                Text(
                    alarma.descripcioComptador, style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── DATES TANCAMENT  ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Columna Esquerra "Notificacio base date"
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
                // Columna Dreta "Data Finalitzacio check limits boolean text layout variables limits parameter mapping definition type assignment limit"
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

            // Espaiat i temps the method duration properties check definition 
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

            // ── CONSUM I LÍMITS CONGELATS AL MOMENT L'ALARMA DATA FRAME ────────────────────────────────AAAA───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info volum litratge limits definition format rules value definitions rules logic value assignment properties checking
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
                // Info Límits The limits mapping object 
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

            // ── TÈCNIC QUI HO VA FER BÉ TANCAR ───────────────────────────────────────────────────
            if (!alarma.tecnicTancament.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                // Signat label handler properties size
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
