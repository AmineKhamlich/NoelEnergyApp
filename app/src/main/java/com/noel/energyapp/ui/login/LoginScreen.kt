package com.noel.energyapp.ui.login

import android.widget.Toast
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    paddingValues: PaddingValues,
    onLoginSuccess: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val passwordFocusRequester = remember { FocusRequester() }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estatCarregant by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    NoelScreen(paddingValues = paddingValues) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- 1. CAPÇALERA LOGOTIP ---
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

            // --- 2. CAMP D'USUARI ---
            NoelTextField(
                value = username,
                onValueChange = { username = it },
                label = "Usuari",
                enabled = !estatCarregant,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. CAMP DE CONTRASENYA ---
            NoelTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contrasenya",
                modifier = Modifier.focusRequester(passwordFocusRequester),
                enabled = !estatCarregant,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                }
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. BOTÓ D'ENTRAR AMB LÒGICA DE SESSIÓ ACTUALITZADA ---
            NoelButton(
                text = "Iniciar Sessió",
                isLoading = estatCarregant,
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Si us plau, omple tots els camps"
                    } else {
                        scope.launch {
                            estatCarregant = true
                            errorMessage = null
                            try {
                                val response = RetrofitClient.instance.login(LoginRequest(username, password))
                                if (response.isSuccessful) {
                                    val loginResponse = response.body()
                                    val sessionManager = com.noel.energyapp.util.SessionManager(context)

                                    loginResponse?.let { res ->
                                        // --- FIX AQUÍ: Passem els 7 arguments que demana el nou SessionManager ---
                                        sessionManager.saveUserData(
                                            userId = res.id,
                                            token = res.token ?: "",
                                            name = res.nomUsuari,           // El Nick (nom de login)
                                            realName = "${res.nom} ${res.cognom}", // El Nom Real combinat
                                            role = res.rol,
                                            assignedPlants = res.idsPlantes.joinToString(","),
                                            mustChangePassword = res.canviPasswordObligatori
                                        )

                                        // Salutació humanitzada al Toast
                                        val nomA_Saludar = if (res.nom.isNotBlank()) res.nom else res.nomUsuari
                                        Toast.makeText(context, "Benvingut, $nomA_Saludar", Toast.LENGTH_SHORT).show()

                                        onLoginSuccess(res.canviPasswordObligatori)
                                    }
                                } else {
                                    errorMessage = "Usuari o contrasenya incorrectes"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de connexió al servidor"
                            } finally {
                                estatCarregant = false
                            }
                        }
                    }
                }
            )
        }
    }
}