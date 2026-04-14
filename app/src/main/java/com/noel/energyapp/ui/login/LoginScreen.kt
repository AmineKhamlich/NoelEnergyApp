package com.noel.energyapp.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelTextField
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.noel.energyapp.ui.theme.*

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

    val isDark = isAppInDarkTheme()

    // Conservem el fons suau per donar un toc Premium, però sense complicar la targeta
    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(DarkBackground, Color.Black))
    } else {
        Brush.verticalGradient(listOf(LightWaterBlue, MaterialTheme.colorScheme.background))
    }

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- 1. LOGO PREMIUM ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(PremiumBlueStart, PremiumBlueEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ENERGY MANAGEMENT",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 4.sp, fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- 2. FORMULARI (MOLT MÉS SIMPLE I ROBUST) ---
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                // Això fa que la targeta agafi automàticament el color blanc (clar) o gris fosc (fosc) del teu Theme!
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp), // Una mica més d'aire perquè respiri bé
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BENVINGUT",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    NoelTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Usuari",
                        enabled = !estatCarregant,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                                Icon(
                                    imageVector = image,
                                    contentDescription = "Veure contrasenya",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    NoelButton(
                        text = "Iniciar Sessió",
                        isLoading = estatCarregant,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = PremiumBlueEnd,
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
                                            val sessionManager = SessionManager(context)
                                            loginResponse?.let { res ->
                                                sessionManager.saveUserData(
                                                    userId = res.id,
                                                    token = res.token ?: "",
                                                    name = res.nomUsuari,
                                                    realName = "${res.nom} ${res.cognom}",
                                                    role = res.rol,
                                                    roleId = res.idRol, // AFEGIM L'ID DEL ROL
                                                    assignedPlants = res.idsPlantes.joinToString(","),
                                                    mustChangePassword = res.canviPasswordObligatori
                                                )
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
    }
}