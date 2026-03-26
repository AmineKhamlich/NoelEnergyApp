package com.noel.energyapp.ui.alarmes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.noel.energyapp.data.TancarIncidenciaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import com.noel.energyapp.data.GenericResponse
import com.noel.energyapp.ui.components.NoelTextField


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
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // 1. Llançador de la Càmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoBitmap = bitmap
        }
    }

    // 2. NOU: Llançador per demanar permís de càmera a l'usuari (Pop-up de seguretat)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si l'usuari diu "Sí", intentem obrir la càmera
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "No s'ha trobat cap app de càmera al dispositiu", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Si diu "No", l'avisem
            Toast.makeText(context, "Cal donar permís per poder fer la fotografia", Toast.LENGTH_LONG).show()
        }
    }

    fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        // Utilitzem NO_WRAP per evitar salts de línia (\n) que trenquen el JSON a l'enviar-ho a C#
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
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

            // CAMP DE DESCRIPCIÓ AMB EL TEU ESTIL NOEL
            NoelTextField(
                value = descripcio,
                onValueChange = { descripcio = it },
                label = "Descripció",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                isError = descripcio.isBlank()
            )

            // CAMP DE SOLUCIÓ AMB EL TEU ESTIL NOEL
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

            Text("Adjuntar Fotografia", style = MaterialTheme.typography.titleMedium)

            if (fotoBitmap == null) {
                OutlinedButton(
                    onClick = {
                        // COMPROVACIÓ DE SEGURETAT DE LA CÀMERA
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            // Si ja teníem permís d'abans, obrim directe
                            try {
                                cameraLauncher.launch(null)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No s'ha trobat cap app de càmera", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Si no tenim permís, llancem el pop-up de l'Android
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
                    Image(
                        bitmap = fotoBitmap!!.asImageBitmap(),
                        contentDescription = "Foto",
                        modifier = Modifier.height(200.dp)
                    )
                    IconButton(
                        onClick = { fotoBitmap = null },
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
                            val base64Image = fotoBitmap?.let { encodeBitmapToBase64(it) }
                            val request = TancarIncidenciaDto(
                                idIncidencia = incidenciaId,
                                descripcioIncidencia = descripcio,
                                solucioAdaptada = solucio,
                                fotoBase64 = base64Image
                            )
                            val response = RetrofitClient.instance.tancarIncidencia("Bearer $token", request)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "Incidència Tancada!", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                Toast.makeText(context, "Error al tancar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
}