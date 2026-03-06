package com.noel.energyapp.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noel.energyapp.network.RetrofitClient
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Has oblidat la contrasenya?", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Escriu el teu usuari i et posarem la contrasenya per defecte.")

        Spacer(modifier = Modifier.height(32.dp))

        // Camp d'usuari
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nom d'Usuari") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (username.isBlank()) {
                        Toast.makeText(context, "Escriu el nom d'usuari", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            isLoading = true
                            try {
                                // Enviem l'usuari al servidor Ubuntu
                                val response =
                                    RetrofitClient.instance.resetPassword(mapOf("username" to username))
                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Contrasenya restablerta correctament",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onBackToLogin() // Tornem enrere un cop fet el reset
                                } else {
                                    Toast.makeText(
                                        context,
                                        "L'usuari no existeix",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT)
                                    .show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "RESTABLIR")
            }

            TextButton(onClick = onBackToLogin) {
                Text(text = "Tornar al login")
            }
        }
    }
}