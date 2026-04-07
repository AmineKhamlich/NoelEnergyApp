package com.noel.energyapp.ui.alarmes

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import coil.compose.AsyncImage
import java.io.File

@Composable
fun TancarIncidenciaScreen(
    paddingValues: PaddingValues,
    incidenciaId: Int,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    var descripcio by remember { mutableStateOf("") }
    var solucio by remember { mutableStateOf("") }
    
    // NOU SISTEMA ARXIUS (Rebutgem el bitmap mini)
    var fotoFile by remember { mutableStateOf<File?>(null) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var displayUri by remember { mutableStateOf<Uri?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }

    // 1. Llançador de Càmera ALTA DEFINICIÓ (Demana una URI prèvia per saber on guardar la foto final)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && pendingUri != null) {
            displayUri = pendingUri
            // La variable fotoFile ja conté l'arxiu emplenat
        }
    }

    // Identificar el nom del FileProvider creat
    val authority = "${context.packageName}.provider"

    // 2. Llançador per demanar permís
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Generem l'arxiu temporal a la memòria cau i demanem la URI
            val f = File(context.externalCacheDir, "incidencia_${System.currentTimeMillis()}.jpg")
            fotoFile = f
            val uri = FileProvider.getUriForFile(context, authority, f)
            pendingUri = uri
            try { cameraLauncher.launch(uri) } catch (e: Exception) { Toast.makeText(context, "Sense càmera", Toast.LENGTH_SHORT).show() }
        } else {
            Toast.makeText(context, "Cal permís per la foto", Toast.LENGTH_LONG).show()
        }
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "TANCAR INCIDÈNCIA #$incidenciaId",
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NoelTextField(
                value = descripcio,
                onValueChange = { descripcio = it },
                label = "Descripció",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                isError = descripcio.isBlank()
            )

            NoelTextField(
                value = solucio,
                onValueChange = { solucio = it },
                label = "Solució adoptada",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                isError = solucio.isBlank()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Adjuntar Fotografia (Alta Def. per Càmera)", style = MaterialTheme.typography.titleMedium)

            if (displayUri == null) {
                OutlinedButton(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            val f = File(context.externalCacheDir, "incidencia_${System.currentTimeMillis()}.jpg")
                            fotoFile = f
                            val uri = FileProvider.getUriForFile(context, authority, f)
                            pendingUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Càmera")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OBRIR CÀMERA")
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = displayUri,
                        contentDescription = "Foto capturada",
                        modifier = Modifier.height(200.dp)
                    )
                    IconButton(
                        onClick = { 
                            displayUri = null 
                            fotoFile = null
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Esborrar foto", tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            NoelButton(
                text = if (isLoading) "ENVIANT DADES..." else "TANCAR INCIDÈNCIA",
                enabled = !isLoading && descripcio.isNotBlank() && solucio.isNotBlank(),
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            // Creació del formulari Multipart
                            val idIncidenciaBody = incidenciaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val descBody = descripcio.toRequestBody("text/plain".toMediaTypeOrNull())
                            val solBody = solucio.toRequestBody("text/plain".toMediaTypeOrNull())
                            
                            var fotoPart: MultipartBody.Part? = null
                            if (fotoFile != null && fotoFile!!.exists() && displayUri != null) {
                                val reqFile = fotoFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                fotoPart = MultipartBody.Part.createFormData("fotoFile", fotoFile!!.name, reqFile)
                            }

                            val response = RetrofitClient.instance.tancarIncidencia("Bearer $token", idIncidenciaBody, descBody, solBody, fotoPart)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Incidència Tancada!", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                val errorStr = response.errorBody()?.string() ?: "Error desconegut"
                                Toast.makeText(context, "Error: $errorStr", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de connexió: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
}
