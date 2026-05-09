/**
 * FITXER: NoelButton.kt
 * CAPA: Interfície d'usuari → Components (ui/components)
 *
 * Aquest fitxer defineix el botó estàndard de la UI, adaptat a l'estil Noel Energy.
 * 
 * Funcionalitats:
 * 1. Ocupa l'amplada sencera de contenidor per defecte, pre-establert a 50dp d'alt.
 * 2. Gestiona automàticament el feedback visual gràcies a l'estat intern `isLoading`, 
 *    substituint la UI del Text per un Spinner.
 */
package com.noel.energyapp.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NoelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false, // Gestiona la rodeta de càrrega automàticament evitant dobles clics
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary // Color per defecte temàtic
) {
    val animationsEnabled = LocalNoelAnimationsEnabled.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (animationsEnabled && isPressed && enabled && !isLoading) 0.98f else 1f,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            snap()
        },
        label = "NoelButtonScale"
    )

    // Dibuixa el composable nadiu de Material 3 
    Button(
        onClick = onClick,
        // Apliquem l'estil Noel: Ample total, altura de 50dp i cantons medium
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale),
        // Queda suspès funcionalment si enabled is false o s'està en curs de loading
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium, // Base shapes configurat
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        // En base al paràmetre isLoading s'intercanvia visualment l'interior
        if (isLoading) {
            // Si està carregant, mostrem la rodeta en lloc del text, de color fix blanc
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            // Si no, el text en majúscules per a un look més industrial
            Text(text = text.uppercase(), style = MaterialTheme.typography.labelLarge)
        }
    }
}
