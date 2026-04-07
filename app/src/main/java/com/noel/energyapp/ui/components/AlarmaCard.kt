package com.noel.energyapp.ui.components

import com.noel.energyapp.ui.theme.isAppInDarkTheme as isSystemInDarkTheme
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.ui.theme.*
import java.util.Locale

@Composable
fun AlarmaCard(
    alarma: IncidenciaVistaDto,
    onGestionarClick: () -> Unit
) {
    // 1. COLORS DINÀMICS HOMOGÈNIS A L'HISTÒRIC
    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) Color.White.copy(alpha = 0.05f) else SurfaceLight
    val contentColor = if (isDark) Color.White else DarkSlate
    val borderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Transparent

    // 2. MOTOR ANIMACIÓ PER LES CRÍTIQUES
    val infiniteTransition = rememberInfiniteTransition(label = "Blinking")
    val animatedCriticalColor by infiniteTransition.animateColor(
        initialValue = AlarmCriticaRed,
        targetValue = AlarmCriticaDark,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CriticalColorAnimation"
    )

    val gravetatNeta = alarma.gravetat.uppercase(Locale.ROOT)
        .replace("Í", "I").replace("È", "E").trim()

    val statusColor = when (gravetatNeta) {
        "ALERTA CRITICA" -> animatedCriticalColor
        "ALERTA", "ALARMA" -> AlarmAlertaOrange
        else                -> AlarmAvisYellow
    }

    val tempsFormatat = formatTempsTranscorregut(alarma.tempsTranscorregut)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 3.dp),
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── CAPÇALERA (Badge Groc/Vermell i Temps Faded Gray) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = alarma.gravetat.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = contentColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Fa $tempsFormatat",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── TÍTOL I DESCRIPCIÓ ──
            Text(
                text = alarma.detallAlarma,
                style = MaterialTheme.typography.bodyMedium, // Ara utilitza el mateix estil que l'històric
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            // ── UBICACIÓ I EQUIP ──
            Text(
                "📍 ${alarma.ubicacio}", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "⚙️ ${alarma.comptador}", style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.6f) // Faded elegant com a l'històric
            )

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── DATES I TEMPS ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Data notificació", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.55f))
                    val dataPlena = alarma.dataCreacio?.takeIf { it.isNotBlank() } 
                        ?: alarma.horaAvisH?.takeIf { it.isNotBlank() } 
                        ?: "—"
                    val dataNeta = dataPlena.replace("T", " ").substringBefore('.')
                    Text(dataNeta, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── 3 COLUMNES DE CONSUM I LÍMITS ──
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Consum Actual", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.55f))
                    Text("${String.format("%.2f", alarma.consumRealAvui)} m³", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Consum Total", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.55f))
                    Text("${String.format("%.2f", alarma.consumDiaAlarma)} m³", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Límits H/HH", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.55f))
                    Text("${alarma.limitH ?: "—"} / ${alarma.limitHH ?: "—"} m³", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── BOTÓ GESTIONAR (De color de xoc) ──
            Button(
                onClick = onGestionarClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = statusColor,
                    contentColor = if (statusColor == AlarmAvisYellow) Color.Black else Color.White
                )
            ) {
                Text("GESTIONAR INCIDÈNCIA", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ------ FUNCIÓ AJUDANT PER ALS DIES/HORES ------
fun formatTempsTranscorregut(txtOriginal: String): String {
    try {
        val regex = Regex("(\\d+)h(?:\\s+(\\d+)m)?")
        val match = regex.find(txtOriginal)

        if (match != null) {
            val totalHores = match.groupValues[1].toInt()
            val minuts = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0

            val dies = totalHores / 24
            val horesRestants = totalHores % 24

            return if (dies > 0) {
                "$dies d, $horesRestants h, $minuts m"
            } else {
                "$horesRestants h, $minuts m"
            }
        }
    } catch (e: Exception) { }
    return txtOriginal
}
