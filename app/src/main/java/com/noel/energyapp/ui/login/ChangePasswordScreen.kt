package com.noel.energyapp.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.ChangePasswordRequest
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

/**
 * Pantalla de canvi de contrasenya obligatori.
 * S'utilitza quan un usuari entra per primer cop o se li ha resetejat la password.
 */
@Composable
fun ChangePasswordScreen(
    paddingValues: PaddingValues,
    onPasswordChangedSuccessfully: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // --- 1. CONFIGURACIÓ I ESTATS ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    // Estats per emmagatzemar el text introduït per l'usuari
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estats booleans per controlar si la contrasenya és visible o està oculta (punts)
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Estats de control d'interfície (carregant i errors)
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // NoelScreen és el nostre component base. Aquí hasMenu = false per evitar que l'usuari escapoli.
    NoelScreen(
        paddingValues = paddingValues,
        title = "CANVI OBLIGATORI",
        hasMenu = false,
        verticalArrangement = Arrangement.Top
    ) {
        // Fem que el contingut sigui scrollable per si el teclat ocupa massa espai
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .imePadding(), // Evita que el teclat tapi els botons inferiors
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Text informatiu per l'usuari
            Text(
                text = "Per motius de seguretat, has d'establir una nova contrasenya abans d'accedir a l'aplicació.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- CAMP 1: CONTRASENYA ACTUAL ---
            NoelTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = "Contrasenya Actual",
                // ES POSA VERMELL SI: falta omplir-lo O el servidor diu que és incorrecta
                isError = (errorMessage == "Tots els camps són obligatoris." && oldPassword.isBlank()) ||
                        (errorMessage == "La contrasenya actual és incorrecta."),
                // Si oldPasswordVisible és true, mostrem text normal. Si és false, mostrem punts.
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                trailingIcon = {
                    // Botó de l'ullet per alternar la visibilitat
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(
                            imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Commutar visibilitat"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMP 2: NOVA CONTRASENYA ---
            NoelTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nova Contrasenya (Mínim 6 caràcters)",
                // ES POSA VERMELL SI: falta omplir-lo O no coincideix O és massa curta O és la de defecte
                isError = (errorMessage == "Tots els camps són obligatoris." && newPassword.isBlank()) ||
                        (errorMessage == "Les contrasenyes noves no coincideixen.") ||
                        (errorMessage?.contains("6 caràcters") == true) ||
                        (errorMessage?.contains("123456") == true),
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Commutar visibilitat"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMP 3: REPETIR NOVA CONTRASENYA ---
            NoelTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Repeteix la Nova Contrasenya",
                // ES POSA VERMELL SI: falta omplir-lo O no coincideix amb la nova
                isError = (errorMessage == "Tots els camps són obligatoris." && confirmPassword.isBlank()) ||
                        (errorMessage == "Les contrasenyes noves no coincideixen."),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Commutar visibilitat"
                        )
                    }
                }
            )

            // Missatge d'error visual si n'hi ha cap
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓ DE GUARDAR ---
            NoelButton(
                text = "Guardar i Continuar",
                isLoading = isLoading,
                onClick = {
                    // 1. Primer de tot, netejem errors anteriors
                    errorMessage = null

                    // 2. Validació de camps buits
                    if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Tots els camps són obligatoris."
                        return@NoelButton
                    }

                    // 3. Validació de longitud
                    if (newPassword.length < 6) {
                        errorMessage = "La nova contrasenya ha de tenir mínim 6 caràcters."
                        return@NoelButton
                    }

                    // 4. Validació de seguretat (No permetre la de defecte)
                    if (newPassword == "123456") {
                        errorMessage = "No pots utilitzar la contrasenya '123456'."
                        return@NoelButton
                    }

                    // 5. EL FIX DEL BUG: Validació de coincidència
                    // Aquesta comprovació ha de ser l'última abans de cridar a l'API
                    if (newPassword != confirmPassword) {
                        errorMessage = "Les contrasenyes noves no coincideixen."
                        return@NoelButton
                    }

                    // 6. Si hem arribat aquí, tot és correcte. Cridem a l'API.
                    scope.launch {
                        isLoading = true
                        try {
                            val token = sessionManager.fetchAuthToken() ?: ""
                            val userId = sessionManager.fetchUserId()
                            val request = ChangePasswordRequest(userId, oldPassword, newPassword)

                            val response = RetrofitClient.instance.changePassword("Bearer $token", request)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Contrasenya actualitzada!", Toast.LENGTH_SHORT).show()
                                sessionManager.clearMustChangePasswordFlag()
                                onPasswordChangedSuccessfully()
                            } else {
                                // El servidor ens diu que la password "vella" no és la que toca
                                errorMessage = "La contrasenya actual és incorrecta."
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

            // Botó per si l'usuari vol tancar la sessió sense canviar la password
            TextButton(onClick = onLogoutClick) {
                Text("Cancel·lar i Tancar Sessió", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}