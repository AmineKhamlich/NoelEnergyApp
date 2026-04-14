/**
 * FITXER: LoginScreen.kt
 * CAPA: Interfície d'usuari → Login (ui/login)
 *
 * Aquesta pantalla és el punt d'entrada principal de l'aplicació. Permet a
 * l'usuari introduir les credencials per accedir a l'interior.
 * 
 * Funcionalitats:
 * 1. Proporciona formularis per a nom d'usuari i contrasenya.
 * 2. Suporta el visionat de la contrasenya oculta.
 * 3. Valida formularis localment previ a connectar a la BD d'accés (API JSON Jwt SignIn).
 * 4. Gestiona els estats segurs usant SessionManager per emmagatzemar tokens si les
 *    credencials funcionen en el context i envia success Callback per el NavHost.
 * 5. Gestiona la recuperació i redirecció si l'anàlisi de token dóna mustChangePassword.
 */
package com.noel.energyapp.ui.login

// Importacions bàsiques de composició per estructures i caixes (UI de blocs)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
// Interacció de Teclat al mòbil
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// Icones per visibilitat de l'ullet en contrasenya oculta
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// Focus per l'avançament directe cap a la línia de text inferior automàtic en prémer botó 'Següent' de teclat mobil
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Model i API Client extern backend C#
import com.noel.energyapp.data.LoginRequest
import com.noel.energyapp.network.RetrofitClient
// Elements visuals reutilitzables genèrics de la capçalera 
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelTextField
// Gestor de claus per Token de Jwt segur i credencials de domini local
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.noel.energyapp.ui.theme.*

@Composable
fun LoginScreen(
    paddingValues: PaddingValues,                // Retall de marges evitant la intromissió a navegació/status Android
    onLoginSuccess: (Boolean) -> Unit            // Callback pel router per aprovar successos de credencial
) {
    // Configura context pel Toast o Session Manager Shared prefs.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Control referencial de Focus per a botar al text input 'password' prement NEXT des d'usuari text Input
    val passwordFocusRequester = remember { FocusRequester() }

    // Memoritzador d'estat de caixetins de text i flags intermèdies visuals i funcionals
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estatCarregant by remember { mutableStateOf(false) }     // Càrrega del spinner per la crida API internet
    var errorMessage by remember { mutableStateOf<String?>(null) } // Si existeix algun error de credentials
    var passwordVisible by remember { mutableStateOf(false) }     // Si la password ha passat visual a l'usuari amb l'ullet o no.

    // Comprovar aplicació tema (si es Fosc general) per fons Premium de Login UI adaptat
    val isDark = isAppInDarkTheme()

    // Conservem el fons suau per donar un toc Premium pre-carregant (Color gradial preestablert app).
    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(DarkBackground, Color.Black))
    } else {
        Brush.verticalGradient(listOf(LightWaterBlue, MaterialTheme.colorScheme.background))
    }

    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        // Envolta en una columna centra a eix vertical completament
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding() // En pujar-hi el teclat tot el contingut scrollable ascendeix sense emmascarar bottom button
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- 1. LOGO PREMIUM (Icona principal de branding, una simple lletra base centrada per fer un app Icon Box logo)
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

            // Subtítol de la marca just sota el símbol d'icone creat amunt.
            Text(
                text = "ENERGY MANAGEMENT", // O subdomini general "Noel Energy"
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 4.sp, fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- 2. FORMULARI CENTRAL: LOGIN (Caixa blanca superposada per agrupar inputs) ---
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp), // Redondejat complet pel bloc tipus Apple style
                // Defineix quin fons agarra l'aplicació: Surface si Theme clar será blanc, i viceversa si dark és fosc material.
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp) // Elevat amb ombra inferior com 3d pop up.
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp), // Separació àmplia perquè els marcs de la caixa de text interior respirin d'aire respecte els fons blancs / foscs
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BENVINGUT",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface 
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // TextField component importat per a la petició Username, sense restricció en asteriscs i amb opció de polsa "Seguent/Next" al teclat virtual del SO base
                    NoelTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Usuari",
                        enabled = !estatCarregant, // Disables the entire TextField component block that restricts input values editing until UI API Call finished.
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }) // Salta enfocament directe sobre textField següent gràcies al focusRequester "passwordFocus" prèviament vinculat inferiorment a Password input.
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // TextField de requeriment Password de domini
                    NoelTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contrasenya",
                        modifier = Modifier.focusRequester(passwordFocusRequester), // Focus vinculant de recanviament via tecla Next
                        enabled = !estatCarregant, // Disables during Loading Submit
                        // Si var de visibilitat bool esta actiu -> None trans visual format, si fals -> Punts de tancament visual d'escrit (dots / asters) Android based VisualTransformation object.
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Retorn de pols o Final en el pad inferior telèfon
                        trailingIcon = {
                            // Canvi vector Imatge basat estat visual de camp de la pwd en cas si passwordVisible 
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

                    // Zona de pintatge condicional "Si Tè Error al Form / Resultant Login Fallit HTTP response..."
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start) // L'error es posiciona inicial a l'esquerra interior form box.
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botó genèric base usat d'App amb un procés coroutine que executa càrrega de progress loading view internal in replacement component title fins el fi API connect finalitzarà OK.
                    NoelButton(
                        text = "Iniciar Sessió",
                        isLoading = estatCarregant,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = PremiumBlueEnd,
                        onClick = {
                            if (username.isBlank() || password.isBlank()) { // Basic null checks or spaced string checking (Empty/Blank local validation form state parameters).
                                errorMessage = "Si us plau, omple tots els camps"
                            } else {
                                scope.launch {
                                    estatCarregant = true // Atura la UI inputs / start Spinner and Clear all old error validation.
                                    errorMessage = null
                                    try {
                                        // Utilitzant Retrofit Interface Instance for Calling Post 'Login' Backend C# action Api.
                                        val response = RetrofitClient.instance.login(LoginRequest(username, password))
                                        if (response.isSuccessful) {
                                            // Processat resposta en objecte data.
                                            val loginResponse = response.body()
                                            // Conductor de Shared Params local persistency "SharedPreferences" by key/value data type (Session Manag).
                                            val sessionManager = SessionManager(context)
                                            loginResponse?.let { res ->
                                                sessionManager.saveUserData(
                                                    // Es desa info primària tokenitzada junt a info addicional que l'App requereix per funcionar l'estat UX, no solament per request secure API. Tambe s'esquiva les calls repetitives si pot persistir DTO object fields com User ID base DB. 
                                                    userId = res.id,
                                                    token = res.token ?: "",
                                                    name = res.nomUsuari,
                                                    realName = "${res.nom} ${res.cognom}",
                                                    role = res.rol,
                                                    roleId = res.idRol, // AFEGIM L'ID DEL ROL NUMÈRIC PER FER FILTRES POSTERIORS EN APP EX: (ROL.ID=1 -> ADMIN) 
                                                    assignedPlants = res.idsPlantes.joinToString(","),
                                                    mustChangePassword = res.canviPasswordObligatori
                                                )
                                                // Retorn cridà base NavHost principal root via params argument passthrough amb paràmetre true/false must change
                                                onLoginSuccess(res.canviPasswordObligatori)
                                            }
                                        } else {
                                            // Status no Successful i errata tipus Bad Request i Not Found return text (peticio completament connectada servidor en actius resposts pero login fallit).
                                            errorMessage = "Usuari o contrasenya incorrectes"
                                        }
                                    } catch (e: Exception) {
                                        // Error tipus IOException Time Out o connexio absenta hardware wifi fail
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