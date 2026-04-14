/**
 * FITXER: TancarIncidenciaScreen.kt
 * CAPA: Interfície d'usuari → Alarmes (ui/alarmes)
 *
 * Aquesta pantalla permet a un tècnic tancar una incidència activa del sistema.
 * Mostra un formulari amb dos camps de text obligatoris (descripció del problema
 * i solució adoptada) i una opció per adjuntar una fotografia d'alta definició
 * presa amb la càmera del dispositiu en el moment del tancament.
 *
 * El flux de la càmera funciona amb FileProvider per guardar la foto a la memòria
 * cau externa del dispositiu i enviar-la directament com a arxiu binari en una
 * petició Multipart al backend, que la guarda al servidor estàtic de la API.
 *
 * Si el permís de càmera no ha estat concedit, es demana en temps d'execució
 * abans de llançar la càmera.
 *
 * En enviar el formulari, es construeix una petició multipart amb tots els camps
 * i es crida a l'API. Si l'API respon correctament, es navega automàticament
 * enrere a la llista d'alarmes actives.
 */
package com.noel.energyapp.ui.alarmes

// Importació del permís de càmera per demanar-lo en temps d'execució
import android.Manifest
// Importació per comprovar si el permís ja ha estat concedit
import android.content.pm.PackageManager
// Importació de la classe URI per referir-se a l'arxiu de la foto al sistema de fitxers
import android.net.Uri
// Importació per mostrar missatges curts a l'usuari
import android.widget.Toast
// Importació dels llançadors d'activitats: per a la càmera i per demanar permisos
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// Importació dels components bàsics de Jetpack Compose
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// Importació per comprovar l'estat del permís de càmera sense llançar un diàleg
import androidx.core.content.ContextCompat
// Importació per crear la URI pública de l'arxiu de foto via FileProvider
import androidx.core.content.FileProvider
// Importació del client HTTP de l'App
import com.noel.energyapp.network.RetrofitClient
// Importació dels components reutilitzables de disseny de l'App
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
// Importació per construir les parts del formulari multipart per a l'enviament de la foto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
// Importació del component de Coil per mostrar la foto capturada a la pantalla
import coil.compose.AsyncImage
// Importació per crear l'arxiu temporal on es guardarà la foto
import java.io.File

// Composable de la pantalla de tancament d'una incidència
@Composable
fun TancarIncidenciaScreen(
    paddingValues: PaddingValues, // Marges del sistema Android (barra superior i inferior)
    incidenciaId: Int,            // ID de la incidència que es vol tancar
    onBackClick: () -> Unit,      // Callback per tornar enrere sense tancar la incidència
    onSuccess: () -> Unit         // Callback que s'executa quan el tancament és exitós
) {
    // Obté el context d'Android necessari per als Toasts, FileProvider i SessionManager
    val context = LocalContext.current
    // Crea l'àmbit de coroutines per executar la crida a l'API de manera asíncrona
    val scope = rememberCoroutineScope()
    // Instancia el SessionManager per recuperar el token JWT de la sessió local
    val sessionManager = remember { SessionManager(context) }
    // Obté el token JWT per autenticar la petició multipart de tancament
    val token = sessionManager.fetchAuthToken() ?: ""

    // Estat del text del camp de descripció del problema
    var descripcio by remember { mutableStateOf("") }
    // Estat del text del camp de solució adoptada
    var solucio by remember { mutableStateOf("") }

    // Arxiu temporal on es guardarà la foto d'alta definició capturada per la càmera
    var fotoFile by remember { mutableStateOf<File?>(null) }
    // URI pendent que s'usa per informar la càmera on ha de guardar la foto
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    // URI de la foto ja capturada per mostrar-la en previsualització a la pantalla
    var displayUri by remember { mutableStateOf<Uri?>(null) }

    // Estat booleà que controla si el botó ha de mostrar "ENVIANT..." i estar bloquejat
    var isLoading by remember { mutableStateOf(false) }

    // Llançador de la càmera d'alta definició (MODE: guardar directament a un arxiu)
    // Quan la càmera retorna el resultat, si és exitós actualitza la URI de previsualització
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture() // Contrato que guarda la foto a la URI indicada
    ) { success: Boolean ->
        if (success && pendingUri != null) {
            // La foto s'ha guardat correctament: actualitza la URI de visualització
            displayUri = pendingUri
            // fotoFile ja conté l'arxiu emplenat amb la foto real
        }
    }

    // Identificador del FileProvider declarat al AndroidManifest.xml
    // Ha de coincidir exactament amb el 'android:authorities' del manifest
    val authority = "${context.packageName}.provider"

    // Llançador per demanar el permís de càmera en temps d'execució (Android 6.0+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission() // Contracte per demanar un permís concret
    ) { isGranted: Boolean ->
        if (isGranted) {
            // El permís ha estat concedit: crea l'arxiu temporal i llança la càmera
            val f = File(context.externalCacheDir, "incidencia_${System.currentTimeMillis()}.jpg")
            fotoFile = f  // Guarda la referència a l'arxiu per enviar-lo posteriorment
            val uri = FileProvider.getUriForFile(context, authority, f) // Crea la URI segura per a la càmera
            pendingUri = uri  // Guarda la URI pendent per processar-la en el resultat
            try { cameraLauncher.launch(uri) } catch (e: Exception) { Toast.makeText(context, "Sense càmera", Toast.LENGTH_SHORT).show() }
        } else {
            // El permís ha estat denegat: informa l'usuari
            Toast.makeText(context, "Cal permís per la foto", Toast.LENGTH_LONG).show()
        }
    }

    // Renderitza l'estructura base de la pantalla amb el títol que inclou l'ID de la incidència
    NoelScreen(
        paddingValues = paddingValues,
        title = "TANCAR INCIDÈNCIA #$incidenciaId", // Títol dinàmic amb l'ID de la incidència
        verticalArrangement = Arrangement.Top
    ) {
        // Columna scrollable per evitar que el teclat amagui el botó d'enviament
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permet scroll quan el teclat apareix
                .imePadding()                          // Marge extra per no quedar tapat pel teclat
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espai de 16dp entre cada element
        ) {
            // Camp de text per descriure el problema observat in situ (mínim 3 línies)
            NoelTextField(
                value = descripcio,
                onValueChange = { descripcio = it }, // Actualitza l'estat en escriure
                label = "Descripció",
                singleLine = false,                  // Permet múltiples línies
                minLines = 3,                        // Mostra mínim 3 línies d'alçada
                maxLines = 5,                        // Limita a 5 línies d'alçada màxima
                isError = descripcio.isBlank()       // Es posa vermell si és buit
            )

            // Camp de text per escriure la solució aplicada per resoldre la incidència
            NoelTextField(
                value = solucio,
                onValueChange = { solucio = it },    // Actualitza l'estat en escriure
                label = "Solució adoptada",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                isError = solucio.isBlank()           // Es posa vermell si és buit
            )

            // Línia separadora visual entre el formulari de text i la secció de la foto
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            // Títol de la secció de fotografia
            Text("Adjuntar Fotografia (Alta Def. per Càmera)", style = MaterialTheme.typography.titleMedium)

            if (displayUri == null) {
                // Si no hi ha foto, mostra el botó per obrir la càmera
                OutlinedButton(
                    onClick = {
                        // Comprova si el permís de càmera ja ha estat concedit prèviament
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            // Permís ja concedit: crea l'arxiu i llança la càmera directament
                            val f = File(context.externalCacheDir, "incidencia_${System.currentTimeMillis()}.jpg")
                            fotoFile = f  // Referència a l'arxiu on la càmera guardarà la foto
                            val uri = FileProvider.getUriForFile(context, authority, f) // URI segura per la càmera
                            pendingUri = uri  // URI pendent per processar quan la càmera torni
                            cameraLauncher.launch(uri) // Inicia l'activitat de la càmera
                        } else {
                            // Permís no concedit: demana el permís a l'usuari
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Càmera") // Icona de càmera
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OBRIR CÀMERA")
                }
            } else {
                // Si ja hi ha una foto capturada, mostra la previsualització amb opció d'esborrar
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    // Mostra la foto capturada com a miniatura de previsualització
                    AsyncImage(
                        model = displayUri,           // URI local de la foto capturada
                        contentDescription = "Foto capturada",
                        modifier = Modifier.height(200.dp) // Alçada fixa per a la previsualització
                    )
                    // Botó per esborrar la foto i tornar al botó de càmera
                    IconButton(
                        onClick = {
                            displayUri = null   // Treu la URI de previsualització
                            fotoFile = null     // Allibera la referència a l'arxiu de foto
                        },
                        modifier = Modifier.align(Alignment.TopEnd) // Posiciona al cantó superior dret
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Esborrar foto", tint = Color.Red)
                    }
                }
            }

            // Empeny el botó d'enviament cap al final de la pantalla
            Spacer(modifier = Modifier.weight(1f))

            // Botó principal d'enviament del formulari de tancament
            NoelButton(
                // El text del botó canvia mentre s'envien les dades
                text = if (isLoading) "ENVIANT DADES..." else "TANCAR INCIDÈNCIA",
                // El botó es desactiva si s'estan enviant dades o si els camps estan buits
                enabled = !isLoading && descripcio.isNotBlank() && solucio.isNotBlank(),
                containerColor = MaterialTheme.colorScheme.primary, // Color primari del tema
                onClick = {
                    isLoading = true // Activa el mode de càrrega per bloquejar el botó
                    scope.launch {
                        try {
                            // Converteix els camps de text a RequestBody del tipus text plà
                            val idIncidenciaBody = incidenciaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val descBody = descripcio.toRequestBody("text/plain".toMediaTypeOrNull())
                            val solBody = solucio.toRequestBody("text/plain".toMediaTypeOrNull())

                            // Si hi ha foto, construeix la part 'fotoFile' del formulari multipart
                            var fotoPart: MultipartBody.Part? = null
                            if (fotoFile != null && fotoFile!!.exists() && displayUri != null) {
                                // Llegeix l'arxiu de foto com a RequestBody de tipus JPEG
                                val reqFile = fotoFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                // Crea la part multipart amb el nom de camp esperat pel backend
                                fotoPart = MultipartBody.Part.createFormData("fotoFile", fotoFile!!.name, reqFile)
                            }

                            // Envia la petició multipart completa a l'API del backend
                            val response = RetrofitClient.instance.tancarIncidencia(
                                "Bearer $token", // Token JWT d'autenticació
                                idIncidenciaBody, descBody, solBody, fotoPart // Parts del formulari
                            )

                            if (response.isSuccessful) {
                                // La incidència s'ha tancat correctament: informa i navega enrere
                                Toast.makeText(context, "Incidència Tancada!", Toast.LENGTH_SHORT).show()
                                onSuccess() // Callback que porta l'usuari de volta a la llista d'alarmes
                            } else {
                                // El servidor ha retornat un error: mostra el missatge d'error
                                val errorStr = response.errorBody()?.string() ?: "Error desconegut"
                                Toast.makeText(context, "Error: $errorStr", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            // Error de xarxa o altre error inesperat: mostra el missatge d'excepció
                            Toast.makeText(context, "Error de connexió: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false // Desactiva el mode de càrrega sempre
                        }
                    }
                }
            )
        }
    }
}
