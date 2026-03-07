package com.noel.energyapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NoelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false, // Gestiona la rodeta de càrrega automàticament
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary // Color per defecte
) {
    Button(
        onClick = onClick,
        // Apliquem l'estil Noel: Ample total, altura de 50dp i cantons medium
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        if (isLoading) {
            // Si està carregant, mostrem la rodeta en lloc del text
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            // Si no, el text en majúscules per a un look més industrial
            Text(text = text.uppercase(), style = MaterialTheme.typography.labelLarge)
        }
    }
}