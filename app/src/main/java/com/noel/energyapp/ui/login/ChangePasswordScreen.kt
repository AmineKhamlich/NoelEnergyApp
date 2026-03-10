package com.noel.energyapp.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.ChangePasswordRequest
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(
    paddingValues: PaddingValues,
    onPasswordChangedSuccessfully: () -> Unit,
    onLogoutClick: () -> Unit // Si es penedeix i vol sortir
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // NO posem ni menú ni botó de tornar enrere. Està bloquejat aquí.
    NoelScreen(
        paddingValues = paddingValues,
        title = "CANVI OBLIGATORI",
        hasMenu = false,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Per motius de seguretat, has d'establir una nova contrasenya abans d'accedir a l'aplicació.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        NoelTextField(
            value = oldPassword,
            onValueChange = { oldPassword = it },
            label = "Contrasenya Actual",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        NoelTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "Nova Contrasenya (Mínim 6 caràcters)",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        NoelTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Repeteix la Nova Contrasenya",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        NoelButton(
            text = "Guardar i Continuar",
            isLoading = isLoading,
            onClick = {
                // VALIDACIONS (Les mateixes que tenim al C#)
                if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "Tots els camps són obligatoris."
                    return@NoelButton
                }
                if (newPassword.length < 6) {
                    errorMessage = "La nova contrasenya ha de tenir mínim 6 caràcters."
                    return@NoelButton
                }
                if (newPassword == "123456") {
                    errorMessage = "No pots utilitzar la contrasenya per defecte."
                    return@NoelButton
                }
                if (newPassword != confirmPassword) {
                    errorMessage = "Les noves contrasenyes no coincideixen."
                    return@NoelButton
                }

                // CRIDA A L'API
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    try {
                        val token = sessionManager.fetchAuthToken() ?: ""
                        val userId = sessionManager.fetchUserId()
                        val request = ChangePasswordRequest(userId, oldPassword, newPassword)

                        val response = RetrofitClient.instance.changePassword("Bearer $token", request)

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Contrasenya actualitzada!", Toast.LENGTH_SHORT).show()
                            // Alliberem a l'usuari de l'obligació de canviar la contrasenya
                            sessionManager.clearMustChangePasswordFlag()
                            onPasswordChangedSuccessfully() // Naveguem al Dashboard
                        } else {
                            errorMessage = "Error: Comprova que la contrasenya actual sigui correcta."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de connexió al servidor."
                    } finally {
                        isLoading = false
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLogoutClick) {
            Text("Cancel·lar i Tancar Sessió", color = MaterialTheme.colorScheme.error)
        }
    }
}