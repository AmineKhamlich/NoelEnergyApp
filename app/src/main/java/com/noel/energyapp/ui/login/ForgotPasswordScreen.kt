package com.noel.energyapp.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    paddingValues: PaddingValues,
    onBackToLogin: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // FEM SERVIR LA PLANTILLA MESTRE
    NoelScreen(paddingValues = paddingValues) {

        Text(text = "Recuperació", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(text = "Has oblidat la contrasenya?", style = MaterialTheme.typography.labelLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Escriu el teu usuari.")

        Spacer(modifier = Modifier.height(32.dp))

        // Camp d'usuari (Amb Plantilla)
        NoelTextField(
            value = username,
            onValueChange = { username = it },
            label = "Nom d'Usuari",
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botó Restablir (Amb Plantilla)
        NoelButton(
            text = "Restablir",
            isLoading = isLoading,
            onClick = {
                if (username.isBlank()) {
                    Toast.makeText(context, "Escriu el nom d'usuari", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        isLoading = true
                        try {
                            val response = RetrofitClient.instance.resetPassword(mapOf("username" to username))
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Contrasenya restablerta correctament", Toast.LENGTH_LONG).show()
                                onBackToLogin()
                            } else {
                                Toast.makeText(context, "L'usuari no existeix", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }
        )

        TextButton(onClick = onBackToLogin) {
            Text(text = "Tornar al login")
        }
    }
}