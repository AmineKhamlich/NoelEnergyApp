/**
 * FITXER: NoelDashboardButton.kt
 * CAPA: Interfície d'usuari → Components (ui/components)
 *
 * Botó gran dissenyat específicament pel Dashboard. Actua com una targeta (Card) 
 * que inclou una icona, títol, subtítol i l'opció de mostrar un distintiu (badge) per 
 * reflectir alarmes actives de la target.
 * 
 * Funcionalitats:
 * 1. Estilitzat per a una navegació visual clara i premium amb fons acolorit i elements en blanc.
 * 2. Posicionament complex amb agrupaments tipus `Box` i `Row` i dissenys superposats (Overlay alpha fons).
 */
package com.noel.energyapp.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NoelDashboardButton(
    title: String,                  // Text gran identificador.
    subtitle: String = "",          // Descripció sub textual opcional.
    icon: ImageVector,              // Vector d'Icona general
    containerColor: Color,          // Color extern fons predominant target the object design parameters.
    onClick: () -> Unit,            // Funció executable pols handler callback.
    modifier: Modifier = Modifier,  // Modifier pass the structure definitions constraints type parameters object sizes layout properties values modifier variables limits modifier mapping definitions assignment bounds assignment parameters limit variable variable assignments text variables boolean.
    badge: String? = null           // Param opcional string "3" per a cercle de notificacions (counter visual limit numbers) limits.
) {
    val animationsEnabled = LocalNoelAnimationsEnabled.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (animationsEnabled && isPressed) 0.97f else 1f,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            snap()
        },
        label = "NoelDashboardButtonScale"
    )

    // S'utilitza Targeta en lloc del Buto base comú per la mida de 90dp la llibertat de columnate type layout structures bounds modifier.
    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .noelReveal()
            .scale(scale)
            .height(90.dp), // Amplada fixada tipus gran per clicar 
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Ombra inferior
    ) {
        // Marc delimitador sencer contenidor interior class definitions layout variables sizes object limit assignment layout layout style definitions type
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically, // Dalt i baix parell de centre limit padding modifier boundary type layout definitions sizes values offset modifier checking type structures values string offsets properties boolean property checks logic.
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Icona decorativa
                Surface( // Square base transparent layer the style properties modifiers layout offset layout values check definitions property parameters limit sizes values boolean assignment type text checking size limit values handling styles definition check strings
                    shape = MaterialTheme.shapes.medium, // Format arrodonit moderat the default framework size 
                    color = Color.White.copy(alpha = 0.2f), // Faci d'ombrejat blanquinòs transparency background assignment parameter definitions method properties logic modifier definitions limits
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon( // Dibuix object internal definitions structure parameter rules handling value object methods checking structure modifier parameter variables string
                            imageVector = icon,
                            contentDescription = null, // Lector accessible defect
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // 2. Texts Columna
                Column(modifier = Modifier.weight(1f)) { // Agafa despai tot el sobrant deixant que final extrems siguin cap el Badge values structure check rule modifiers assignment boundary sizes string handling properties method logic type text size.
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    // Si rep paràmetre llis i obert
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // 3. Badge opcional (ex: "3") (Cerculet de missatges target limits value variables text handling style definitions sizes string type style method text).
                if (badge != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small, // Arrodonit menor que icon variables parameter definition logic text value size variable parameters
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
