/**
 * FITXER: ForgotPasswordScreen.kt
 * CAPA: Interfície d'usuari → Login (ui/login)
 *
 * Aquesta pantalla serveix de via de recuperació en cas que l'usuari no recordi la contrasenya.
 * La solució temporal implementada és enviar el @nick al backend, i si l'usuari és vàlid,
 * el servidor estableix automàticament "123456" com a contrasenya temporal i força a 
 * canviar-la de nou la propera vegada que entri (flag 'canviPasswordObligatori' a la BD).
 * 
 * Funcionalitats:
 * 1. Proporciona formularis per inserir el nom de l'usuari.
 * 2. Connecta amb un Endpoint API per forçar un reinici d'estat a Backend d'aquesta pwd passades en context global a base DTO Json data API endpoints connection string rules methods class function params variable declarations .
 */
package com.noel.energyapp.ui.login

// Eines de presentadors per emergents Toast, text i columnat ui components basic framework native ui rendering
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// Retrofit configuració pel tracte dels paquets http calls rest server web data responses mapping methods
import com.noel.energyapp.network.RetrofitClient
// Noel Custom Premium App design files import definitions
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
// Gestor cache persistència telèfon 
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    paddingValues: PaddingValues,                // Retall bàsic pels System Insets OS
    onPasswordChangedSuccessfully: () -> Unit,   // Opcional en el futur quan implementem Flow directe de recuperar a pass nou.
    onBackToLogin: () -> Unit,                   // Fletxa de retrocés de flux lliure via NavHost routing
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Preparat el session Manager si requerim interacció de guardament. (No requereix token on aquest endpoint backend API rules permit call anonymous form request pre auth verification method checks).
    val sessionManager = remember { SessionManager(context) }

    // Memoritzador d'estats textuals usuaris target strings.
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estats per controlar la visibilitat bool logic object types class definitions component properties
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Valor d'username per reset i l'estat dels spinners carregar UI process 
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    // FEM SERVIR LA PLANTILLA MESTRE custom screen
    NoelScreen(
        paddingValues = paddingValues,
        title = "CANVI OBLIGATORI", // Segueix marc genèric com de problemàtica en contrasenya
        verticalArrangement = Arrangement.Top // Align top in Column child context layout rules modifiers padding properties
    ) {
        
        // Explicació per usabilitat pre formulari target
        Text(text = "Recuperació", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(text = "Has oblidat la contrasenya?", style = MaterialTheme.typography.labelLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Escriu el teu usuari.") // Instrucció bàsica simple label. 

        Spacer(modifier = Modifier.height(32.dp))

        // Camp d'usuari (Amb Plantilla del framework app base estandard code template prebuilt components styles)
        NoelTextField(
            value = username,
            onValueChange = { username = it }, // canvia variable form context live re comp updates
            label = "Nom d'Usuari",
            enabled = !isLoading // desactiva accions no requerides paral·leles durant la processó async call and hold logic blocking ui states boolean flags management property assignments
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botó principal Restablir de la petició C# server async task await 
        NoelButton(
            text = "Restablir",
            isLoading = isLoading,
            onClick = {
                if (username.isBlank()) { // Basic checks client prevent network spam empty
                    Toast.makeText(context, "Escriu el nom d'usuari", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        isLoading = true
                        try {
                            val token = sessionManager.fetchAuthToken()
                            if (token.isNullOrBlank()) {
                                Toast.makeText(
                                    context,
                                    "El restabliment l'ha de fer un administrador o supervisor.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@launch
                            }
                            // Utilitza diccionari json ràpid llançat post object
                            val response = RetrofitClient.instance.resetPassword("Bearer $token", mapOf("username" to username))
                            if (response.isSuccessful) {
                                // Cas reeixit. El backend ha canviat la contraseña a "123456" de manera autònoma i envia true a l'status bool flag DB table property column bit mapping. 
                                Toast.makeText(context, "Contrasenya restablerta correctament", Toast.LENGTH_LONG).show()
                                onBackToLogin() // Retorna al login page component view router pass rule callback logic jump stack rules 
                            } else {
                                // Exemple status 404/400 (Bad req/Not Found) on la API fa match validation fault de target string object val name param i no existeix usuari al database records query checks tests 
                                Toast.makeText(context, "L'usuari no existeix", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false // Desbloqueja l'UI bloquejat a "true" start call button click function properties states bool modifier 
                        }
                    }
                }
            }
        )
        
        // Link flat text per abandonar pantalla de volta lliure. Redundància útil fora hard system hardware buttons native gestures actions logic control flow
        TextButton(onClick = onBackToLogin) {
            Text(text = "Tornar al login")
        }
    }
}
