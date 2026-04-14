/**
 * FITXER: NoelTextField.kt
 * CAPA: Interfície d'usuari → Components (ui/components)
 *
 * Caixa de text reutilitzable pre-formatada segons directrius de disseny de l'aplicació.
 *
 * Funcionalitats:
 * 1. Base estilística 'OutlinedTextField' pre definint vores arrodonides, full-width.
 * 2. Adaptativitat màgica al dark/light theme integrant colorScheme parameters correctius.
 * 3. Gestió inherent del teclat, opcions lineals i controls d'error.
 */
package com.noel.energyapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun NoelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false, // Dispara l'estilització vermella/alerta de les vores i label.
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default, // Per tipus numèrics o propòsits especials
    keyboardActions: KeyboardActions = KeyboardActions.Default, // Comportament del botó ENTER
    trailingIcon: @Composable (() -> Unit)? = null, // Usat típicament per al botó "veure contrasenya"
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    // 🗑️ Hem eliminat la crida manual a isSystemInDarkTheme()
    // i les variables de Color.White i Color.Black. El Theme ho farà per nosaltres automàticament!

    // Wrapper nadiu
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(), // Es farcida l'ampla sencera.
        enabled = enabled,
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.medium, // Les vores arrodonides estàndard definides al theme local.
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,

        // 🎨 AQUESTA ÉS LA MÀGIA DEL THEME 🎨
        colors = OutlinedTextFieldDefaults.colors(
            // El text que escriu l'usuari intercanvia negre fons clar i blanc fons fosc:
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

            // L'etiqueta flotant (el label superior del contorn field box):
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

            // El color del marc (Això crearà l'efecte nítid d'interfície!):
            focusedBorderColor = MaterialTheme.colorScheme.primary, // Blau Noel o Cyan actiu
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), // Transperència atenuada fons en no focus.

            // Color del text d'error (sempre vermell/ataronjat segons el teu tema per mantenir alertes visuals del camp check).
            errorTextColor = MaterialTheme.colorScheme.error,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )
}