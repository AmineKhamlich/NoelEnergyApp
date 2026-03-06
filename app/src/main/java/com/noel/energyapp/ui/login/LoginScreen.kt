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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.network.RetrofitClient
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    paddingValues: PaddingValues,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit // NOU: Callback per navegar a la pantalla de recuperació
) {
    val context = LocalContext.current // Necessari per mostrar missatges tipus "Toast"
    val scope = rememberCoroutineScope() // Crea l'espai per executar corutines

    // --- MILLORA DEL TECLAT: Creem el FocusRequester per saltar d'un camp a l'altre ---
    val passwordFocusRequester = remember { FocusRequester() }

    // Variables per guardar el que l'usuari escriu
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estats per a la gestió de la resposta
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Variable per mostrar la contrasenya si l'usuari vol
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. LOGOTIP O TÍTOL DESTACAT ---
        // Aquí podríem carregar una Image(painterResource(R.drawable.logo_noel)...)
        Text(
            text = "NOEL",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "ENERGY MANAGEMENT",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // -- 2. CAMP D'USUARI ---
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuari") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading, // Desactivat mentre està carregant
            singleLine = true, // Evita que es facin salts de linia
            // MILLORA TECLAT: Posem acció de "Next"
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() } // Salta al password
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. CAMP DE CONTRASENYA AMB ULL ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasenya") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            enabled = !isLoading, // Desactivat mentre està carregant
            singleLine = true, // Evita que es facin salts de linia
            // Lògica per mostrar/amagar contrasenya (substitueix el PasswordVisualTransformation fix anterior)
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            // MILLORA TECLAT: Posem acció de "Done" (fet)
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { /* Aquí podríem executar el login directament si volguéssim */ }
            ),
            trailingIcon = {
                // Triem la icona segons l'estat de passwordVisible
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contrasenya")
                }
            }
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

        // --- 4. BOTÓ D'ENTRAR ---
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

                                    // CRIDEM AL CALLBACK! Això avisarà a la MainActivity perquè ens mogui de pantalla
                                    onLoginSuccess()

                                } else {
                                    errorMessage = "Usuari o contrasenya incorrectes"
                                }
                            } catch (e: Exception) {
                                // Error de xarxa (servidor apagat, port tancat, etc.)
                                errorMessage = "Error de connexió al servidor"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium, // Cantons arrodonits per a un disseny més modern
            ) {
                Text("INICIAR SESSIÓ")
            }

            // --- 5. BOTÓ RECUPERAR CONTRASENYA ---
            TextButton(
                onClick = { onForgotPasswordClick() }, // Crida la navegació a ForgotPasswordScreen
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "He oblidat la meva contrasenya",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}