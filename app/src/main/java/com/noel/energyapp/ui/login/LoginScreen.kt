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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.network.RetrofitClient
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current // Necessari per mostrar missatges tipus "Toast"
    val scope = rememberCoroutineScope() // Crea l'espai per executar corutines

    // Variables per guardar el que l'usuari escriu
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estats per a la gestió de la resposta
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "NOEL ENERGY", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Camp d'usuari
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuari") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Desactivat mentre està carregant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Camp de Contrasenya
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasenya") },
            visualTransformation = PasswordVisualTransformation(), // Amaga els caràcters
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Desactivat mentre està carregant
        )

        // Si hi ha un error, el mostrem en vermell
        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            // Mostrem una rodeta de càrrega mentre esperem l'API
            CircularProgressIndicator()
        } else {
            // Botó d'entrar
            Button(
                onClick = {
                    // Lógica per a l'entrada
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Si us plau, omple tots els camps"
                    } else {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val response = RetrofitClient.instance.login(
                                    LoginRequest(
                                        username,
                                        password
                                    )
                                )

                                if (response.isSuccessful) {
                                    val loginResponse = response.body()

                                    // 1. Instanciem el Manager passant-li el Context
                                    val sessionManager =
                                        com.noel.energyapp.util.SessionManager(context)

                                    // 2. Guardem el Token, Nom i Rol a la memòria del mòbil
                                    loginResponse?.let {
                                        sessionManager.saveUserData(it.token, it.nom, it.rol)
                                    }

                                    // Rebem EL Token
                                    Toast.makeText(
                                        context,
                                        "Benvingut ${loginResponse?.nom}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Proper pas: Guardar el token i navegar el Dashborad
                                } else {
                                    errorMessage = "Usuari o contrasenya incorrectes"
                                }
                            } catch (e: Exception) {
                                // Error de xarxa (servidor apagat, port tancat, etc.)
                                errorMessage = "Error de connexió: ${e.localizedMessage}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }
        }
    }
}