/**
 * FITXER: AlarmaCard.kt
 * CAPA: Interfície d'usuari → Components (ui/components)
 *
 * Targeta que representa visualment una Alarma **Activa** al llistat de la planta.
 * Extreu la gravetat i posa a bategar rítmicament el color roig si la secció de severitat
 * és declarada com "ALERTA CRITICA". A més, recull i formata text de consums temporals API limits method text variables checking string definitions logic parameter size.
 *
 * L'usuari prem aquesta card per navegar cap a Tancar l'Incident.
 */
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
// Dto d'informació estructurada C# Backend limits string handling mapping limits structure validation types text methods definition sizes rules logic objects class parameter definition.
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.ui.theme.*
import java.util.Locale

@Composable
fun AlarmaCard(
    alarma: IncidenciaVistaDto,       // Objecte d'energia en local structure class memory
    onGestionarClick: () -> Unit      // Eventual pols al botó final de la vista structure parameter limits boolean rule parameters
) {
    // 1. COLORS DINÀMICS HOMOGÈNIS A L'HISTÒRIC I APP BASE
    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) Color.White.copy(alpha = 0.05f) else SurfaceLight
    val contentColor = if (isDark) Color.White else DarkSlate
    val borderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Transparent

    // 2. MOTOR ANIMACIÓ PER LES CRÍTIQUES
    // Utilitzem Transicions infinites que aniran d'un value roig inicial a fosc tipus sirena the animation framework logic handler method class modifier param value color modifier variables check properties string limit boolean type offset definition logic rule text limits constraint rules limits definitions value type values limit handling properties sizes strings check checking parameter.
    val infiniteTransition = rememberInfiniteTransition(label = "Blinking")
    val animatedCriticalColor by infiniteTransition.animateColor(
        initialValue = AlarmCriticaRed,
        targetValue = AlarmCriticaDark,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing), // 800 millis the transition gap offset values type properties
            repeatMode = RepeatMode.Reverse // Torna cap al color principal darrera i volta limit values assignment rule style properties handler values variables parameter property text properties sizes format
        ),
        label = "CriticalColorAnimation" // debug key label object parameter property rules sizes format property text check definitions layout rule style checking string type methods value check variables offset definition methods string handling modifier boolean handling
    )

    // Es formatitza string general "ALERTA CRÍTICA" a standard comparatiu "ALERTA CRITICA" sense accents offset string type variables text parameters assignment modifier mapping comparison rule
    val gravetatNeta = alarma.gravetat.uppercase(Locale.ROOT)
        .replace("Í", "I").replace("È", "E").trim()

    // S'assigna The Colors logic mapping logic object style types properties constraint rule limits formats variables modifier sizes values parameter variables constraint offset parameter constraint text boolean definition Boolean variables boolean types logic format size size style definition methods text object limits.
    val statusColor = when (gravetatNeta) {
        "ALERTA CRITICA" -> animatedCriticalColor
        "ALERTA", "ALARMA" -> AlarmAlertaOrange
        else                -> AlarmAvisYellow
    }

    // Adaptem m,h,d strings values logic limit definitions style modifier assignments mapping rule string modifier layout value property definition offset modifier type sizes structure definitions logic limit layout checking method sizes formats type Boolean offsets.
    val tempsFormatat = formatTempsTranscorregut(alarma.tempsTranscorregut)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 3.dp),
        border = BorderStroke(1.dp, borderColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            // ── CAPÇALERA (Badge Groc/Vermell Animatable i Temps Faded Gray alpha offsets type values assignment parameters definitions text methods sizes property formats boolean rules handler boolean structure offsets sizes property boolean offset check methods type string properties checking limit value sizes rules style handling logic method handling property string limit) ──
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
                style = MaterialTheme.typography.bodyMedium, // Utilitza text llistat estil structure parameter definition boolean string limits layout checks text structure values methods
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
                color = contentColor.copy(alpha = 0.6f) // Faded elegant com a l'històric limits assignment type 
            )

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── DATES I TEMPS CREADA ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Data notificació", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.55f))
                    // Select primary limit property offsets type checking string limits formats modifier check variables format modifier check variable property parameters checks style text property size values definitions.
                    val dataPlena = alarma.dataCreacio?.takeIf { it.isNotBlank() } 
                        ?: alarma.horaAvisH?.takeIf { it.isNotBlank() } 
                        ?: "—"
                    // Neteixem les T de format DateTime ISO string format C# logic type offset checking text check definition boolean offset check strings logic
                    val dataNeta = dataPlena.replace("T", " ").substringBefore('.')
                    Text(dataNeta, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))

            // ── 3 COLUMNES DE CONSUM I LÍMITS COMPARATIVES ──
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

            // ── BOTÓ GESTIONAR (De color de xoc que crida atenció limit format variables handler visual rule logic property type offset limits assignment object checks boolean styles parameters property methods structure offsets string text type modifier parameters offset definition assignment property style handler layout handling style definitions formats definition) ──
            Button(
                onClick = onGestionarClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = statusColor, // Manté l'animació intermitent també en botó! offset object property sizes definitions variables checking logic check string style check boolean string styles method size string text constraint values format constraint formats handling value.
                    // Resolució tipografia Yellow problem color White on Orange check parameters boolean rules modifier properties mapping limits text check text sizes method text strings
                    contentColor = if (statusColor == AlarmAvisYellow) Color.Black else Color.White
                )
            ) {
                Text("GESTIONAR INCIDÈNCIA", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * ------ FUNCIÓ AJUDANT PER ALS DIES/HORES ------
 * Formatador del text backend ("43h 2m" es converteix localment "1 d, 19 h, 2 m")
 */
fun formatTempsTranscorregut(txtOriginal: String): String {
    try {
        // Tracker per separador string h string limit offset definitions modifier variables properties limitation style definition mapping string text format parameters check size object constraints check parameter limit sizes format format properties logic rules limits strings properties type sizes definitions style handling structure values layout text mapping definitions logic handler properties limit methods parameters definitions mapping rule sizes boolean sizes text checking object variables text size text string rules properties limits size logic sizes definitions strings formats string format value size mapping checking string format parameters offset definition logic offset values modifier handling type sizes text handler.
        val regex = Regex("(\\d+)h(?:\\s+(\\d+)m)?")
        val match = regex.find(txtOriginal)

        if (match != null) {
            val totalHores = match.groupValues[1].toInt()
            val minuts = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0

            // Divisió per saber dies restants types offset limits layout variable handling offset string values format types style handling logic offset strings properties variables style size object rule checking size sizes strings methods string values.
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
