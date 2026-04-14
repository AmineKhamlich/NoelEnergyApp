/**
 * FITXER: ChangePasswordScreen.kt
 * CAPA: Interfície d'usuari → Login (ui/login)
 *
 * Aquesta pantalla serveix de pas obligat quan el "Login" ha estat efectuat però el backend retorna el parametre `mustChangePassword=true`.
 * La funció no admet que tan i l'usuari esquivi la condició. Restarà en un espai segrestat prent part visual, fora del NavHost bàsic.
 *
 * Funcions clau:
 * 1. Solicita a l'usuari la seva anterior contrasenya i dues vegades com a verificació de tipatge la nova creació seguritzada.
 * 2. Comprovació lògica frontal (longitud mínima de la pwd introduïda i descarte del predeterminat '123456').
 * 3. En cas favorable connecta al context API i rep update booleà true del call Request amb la fi de borrar de sessió l'estat obligatori local del dispositiu com a Cache local and bypass it in future load context.
 */
package com.noel.energyapp.ui.login

// Funcionalitat general i interacció toast en pantalla flotant (UI Pop) i inputs.
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// Mòdul iconos materials estàndards (Lupa/Gota/Ullet text pass ocult).
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
// Components propis estructurals arquitectonics Model & Networking Api Client Request calls definitions.
import com.noel.energyapp.data.ChangePasswordRequest
import com.noel.energyapp.network.RetrofitClient
// Components gràfics propis customitzats base theme estandaritzats d'UI elements form App i Screens generals no repeat layout params
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
// Variables emmagatzemades segurament preferits Context app values management keys access function methods definition scope usage calls 
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(
    paddingValues: PaddingValues,                // Retall global top and bottom for default system OS Android borders offset overlapping UI avoidance rendering process
    onPasswordChangedSuccessfully: () -> Unit,   // Redirecció encarregat NavHost d'accés lliure endinsat a plantes principal (Dashboard list)
    onLogoutClick: () -> Unit                    // Opcional abort operació tancant pròpia sessió iniciada sense finalització configuració
) {
    // --- 1. CONFIGURACIÓ I ESTATS ---
    // Context local UI de l'OS. Reté àmbit rutines. Localiza i enllaça prefereixes usuari dades pre-desades app scope locals context local storage local 
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    // Estats per emmagatzemar el text de password antic, nou i repetir en temps real tecleig.
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estats booleans per controlar si la respectiva contrasenya dels tres formats és visible "str flat normal" o està oculta "asteriscs security mode punts ocultació" (Eye trailing icons controls flag).
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Estats de control d'interfície (animació botó càrrega server response wait load UI spinner visual state) i String messages errors.
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Mostrat només si String object not null on surface UI compose error text logic display label component draw string layout view .

    // NoelScreen és el component custom de framework. Title defineix l'interior de AppBar en la TopAppBar standard noel componement base UI App general function definition
    NoelScreen(
        paddingValues = paddingValues,
        title = "CANVI OBLIGATORI", // Informant del motiu per el qual s'ha saltat fornit
        verticalArrangement = Arrangement.Top // Fila cap avall per les caixes i botó
    ) {
        // Envolta form dins Column que creix progressivament, afegint barra d'scroll i salt imepadding quan es desplegui pad OS intern visualment tecles inferiors.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .imePadding(), // Evita que el teclat quan puja es rellisqui menjant part dels botons baixant en la caixa darrere invisible per UI de tancat i enviar la operació (Accept i Submit).
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Text informatiu secundari per l'usuari amb color apagat secondary text UI context form background theme color style configuration predefinition values palette
            Text(
                text = "Per motius de seguretat, has d'establir una nova contrasenya abans d'accedir a l'aplicació.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- CAMP 1: CONTRASENYA ACTUAL PWD ESCRIT ANTIC PER COMUNICAR A L'API COM VERIFICACIÓ D'APLICANT IDENTITAT COMPROBATORIA---
            NoelTextField(
                value = oldPassword, // Bind bi direccional local State
                onValueChange = { oldPassword = it },
                label = "Contrasenya Actual",
                // ES POSA MARCA D'ERROR VERMELL CONDICIONALMENT SI: falta omplir-lo O el servidor respon clar i explícit amb error old logic process no coincindent C# validator logic false boolean value (invalid user password in database matching logic code compare test method check value result).
                isError = (errorMessage == "Tots els camps són obligatoris." && oldPassword.isBlank()) ||
                        (errorMessage == "La contrasenya actual és incorrecta."),
                // Si flag booleà oldPasswordVisible true mostra text normal llegible (None format type mode param apply visual transform). Si false converteix lletra a Punts cec de manera immediata temps real form input event (transform Password method object logic class modifier return state transformation UI layer paint process layout).
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), // Salt al text baix
                trailingIcon = { // Elements al final del l'espai rectangle del Input com els Icon Button 
                    // Botó IconButton contenint un icono Vector.
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) { // Nega i canvia estat d'ocult boolean modifier state re composició .
                        Icon(
                            // Carrega tipus icona creuada o plana eye obert (icon.visible.vector) en funciò logic de variable modifier state UI state check value rendering re drawing.
                            imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Commutar visibilitat"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMP 2: NOVA CONTRASENYA PER POSAR I CONFIGURAR SUBSTITUCIÓ ACTIVA AL NOU VALOR NOU DB RECORD ---
            NoelTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nova Contrasenya (Mínim 6 caràcters)",
                // CONDICIONALS MULTIPLES PER COMPROBAR VERITATS I LLAÇAR ESTAT D'ERRATA VISUAL I TALL PROCESS UI VISUAL FEEDBACK PER PINTURA CAMP DE ROIG VERMELL COLOR MARCAT D'ESTAT D'ERROR I ALERTAR: 
                // falta omplir, no coincidir, petita o defecte default pattern check.
                isError = (errorMessage == "Tots els camps són obligatoris." && newPassword.isBlank()) ||
                        (errorMessage == "Les contrasenyes noves no coincideixen.") ||
                        (errorMessage?.contains("6 caràcters") == true) ||
                        (errorMessage?.contains("123456") == true),   // Error comú control defecte predefinida de reset a BD "123456".
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), // Pass a Repetició nou
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

            // --- CAMP 3: REPETIR LA NOVA PER A SEGURETAT CONTRA EQUIVOCS DE TECLAT A CEGUES ---
            NoelTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Repeteix la Nova Contrasenya",
                // ERROR només salta buit o discordar la de baix check form UI modifier logic values check state composition rules
                isError = (errorMessage == "Tots els camps són obligatoris." && confirmPassword.isBlank()) ||
                        (errorMessage == "Les contrasenyes noves no coincideixen."),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Dona botó de teclat Accept Final o Enviar directament el Form 
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Commutar visibilitat"
                        )
                    }
                }
            )

            // Espai fixat de texte inferior al Form i Missatge Text roig d'error d'explicació verbal humana text object per usuari indicant detall logic pre debug.
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it, // Missatge assignat guardat pre call en var var String or Call Request response error parse value .
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓ DE GUARDAR ENVIADOR FINAL DE RESULTAT ALS SERVIDORS DE DADES NOEL---
            NoelButton(
                text = "Guardar i Continuar",
                isLoading = isLoading, // Variable progressió activada bloquedor interacció progress view spinner circular de la UI.
                onClick = {
                    // MÈTODES DE VERIFICCIÓ FORMULARIS FRONT LOCAL ABANS PROCESSADOR EXTERNES:
                    
                    // 1. Sempre es neteja per garantir estat fresh pre call validation code.
                    errorMessage = null

                    // 2. Si algun és de tamany zero/buit per evitar cridades ximplis a C# i network load and cost in time for waiting responses logic handling backend service delay.
                    if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Tots els camps són obligatoris."
                        return@NoelButton // Retorn abortiu i talla continuacio sense caure scope i exception 
                    }

                    // 3. Revisió estàndard de mínim en front longitud de mida (C# Backend ten a 6).
                    if (newPassword.length < 6) {
                        errorMessage = "La nova contrasenya ha de tenir mínim 6 caràcters."
                        return@NoelButton
                    }

                    // 4. Mantenint les regles de default (No 123456 preconfig d'obligació de tancat canvi DB no el permet per re accept).
                    if (newPassword == "123456") {
                        errorMessage = "No pots utilitzar la contrasenya '123456'."
                        return@NoelButton
                    }

                    // 5. Comparativa d'igualats en string local value assign rules between input text user strings logic evaluation value form checks.
                    if (newPassword != confirmPassword) {
                        errorMessage = "Les contrasenyes noves no coincideixen."
                        return@NoelButton
                    }

                    // 6. El darrer i principal Submit de Backend Corrutine Post HTTP call using API logic endpoints calls method definition request 
                    scope.launch {
                        isLoading = true
                        try {
                            // Demana elements essencias pel backend Dto com un context session parameter of credentials ID int object.
                            val token = sessionManager.fetchAuthToken() ?: ""
                            val userId = sessionManager.fetchUserId()
                            val request = ChangePasswordRequest(userId, oldPassword, newPassword)

                            // Posta o Canvi HTTP action 
                            val response = RetrofitClient.instance.changePassword("Bearer $token", request)

                            if (response.isSuccessful) { // status codes (2xx range of success operations OK)
                                // Notificació toast exit i acció posterior local management values
                                Toast.makeText(context, "Contrasenya actualitzada!", Toast.LENGTH_SHORT).show()
                                sessionManager.clearMustChangePasswordFlag() // Esborrem al telèfon la referència "mustChange" i queda clean pre accés correcte dash view
                                onPasswordChangedSuccessfully() // callback Router Dashboard start 
                            } else {
                                // Casos HTTP errors 4xx Request client logic value fault. Exemple "el old Password d'on provenim a l'API falla amb DB matching i per tant es Bad Request i denega new assign" per Auth security rules verification user id request check call .
                                errorMessage = "La contrasenya actual és incorrecta."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error de connexió al servidor."
                        } finally {
                            isLoading = false // Para spinner end processing task 
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botó flotant i desconnectat pre disenyat de color error com eixida i abort accions netes cancelant .
            TextButton(onClick = onLogoutClick) {
                Text("Cancel·lar i Tancar Sessió", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}