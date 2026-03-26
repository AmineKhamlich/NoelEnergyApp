package com.noel.energyapp.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
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
import com.noel.energyapp.ui.theme.AlarmAlertaOrange
import com.noel.energyapp.ui.theme.AlarmAvisYellow
import com.noel.energyapp.ui.theme.AlarmCriticaDark
import com.noel.energyapp.ui.theme.AlarmCriticaRed
import java.util.Locale

@Composable
fun AlarmaCard(
    alarma: IncidenciaVistaDto,
    onGestionarClick: () -> Unit
) {
    // 1. Preparem el motor d'animació per a les pampallugues
    val infiniteTransition = rememberInfiniteTransition(label = "Blinking")

    // Anima el color entre Vermell i Granat Fosc cada 800 mil·lisegons
    val animatedCriticalColor by infiniteTransition.animateColor(
        initialValue = AlarmCriticaRed,
        targetValue = AlarmCriticaDark,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CriticalColorAnimation"
    )

    // Netegem el text: ho passem a majúscules, traiem accents i espais per evitar errors
    val gravetatNeta = alarma.gravetat.uppercase(Locale.ROOT)
        .replace("Í", "I")
        .replace("È", "E")
        .trim()

    // 2. Assignem els colors segons el text de la base de dades
    val cardColor = when (gravetatNeta) {
        "ALERTA CRITICA" -> animatedCriticalColor // Apliquem l'animació aquí!
        "ALERTA", "ALARMA" -> AlarmAlertaOrange
        else                -> AlarmAvisYellow
    }

    val contentColor = Color.White

    // 3. Dibuixem la targeta
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CAPÇALERA: Icona + Nivell de Gravetat + Temps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Alarma", tint = contentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alarma.gravetat.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Mostrem el temps que porta oberta
                Text(
                    text = "Fa ${alarma.tempsTranscorregut}",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TÍTOL PRINCIPAL
            Text(
                text = alarma.detallAlarma,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // DADES: Planta i Comptador
            Text(text = "📍 Ubicació: ${alarma.ubicacio}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "⚙️ Equip: ${alarma.comptador}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // DADES TÈCNIQUES (Límits i Consum en m³)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Consum Avui", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.7f))
                    Text("${String.format("%.2f", alarma.consumRealAvui)} m³", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Límit H / HH", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.7f))
                    Text("${alarma.limitH ?: "-"} / ${alarma.limitHH ?: "-"} m³", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓ PER GESTIONAR
            Button(
                onClick = onGestionarClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = if (gravetatNeta == "ALERTA CRITICA") Color(0xFFD32F2F) else cardColor
                )
            ) {
                Text("GESTIONAR INCIDÈNCIA", fontWeight = FontWeight.Bold)
            }
        }
    }
}
