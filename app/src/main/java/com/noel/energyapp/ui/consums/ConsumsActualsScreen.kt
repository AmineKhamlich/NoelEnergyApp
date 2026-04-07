package com.noel.energyapp.ui.consums

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noel.energyapp.data.DimCntDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.WaterDeep
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumsActualsScreen(
    paddingValues: PaddingValues,
    plantaNom: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    // Llista de comptadors
    var comptadors by remember { mutableStateOf<List<DimCntDto>>(emptyList()) }
    var comptadorSeleccionat by remember { mutableStateOf<DimCntDto?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Opcions de precisió (Poling)
    val precisiOpcions = listOf(
        Pair("Normal (cada 5s)", 5000L),
        Pair("Alta (cada 2s)", 2000L),
        Pair("Molt Alta (cada 1s)", 1000L)
    )
    var precisioSeleccionada by remember { mutableStateOf(precisiOpcions[0]) }
    var isPrecisioDropdownExpanded by remember { mutableStateOf(false) }

    // Valor Actual Live
    var liveValue by remember { mutableStateOf<Double?>(null) }
    var lastUpdate by remember { mutableStateOf("Sense dades") }
    
    // Status connection
    var errorConnection by remember { mutableStateOf(false) }

    // 1. Obtenir els comptadors en obrir
    LaunchedEffect(plantaNom) {
        try {
            val response = RetrofitClient.instance.getComptadorsPerPlanta("Bearer $token", plantaNom)
            if (response.isSuccessful) {
                comptadors = response.body() ?: emptyList()
                if (comptadors.isNotEmpty()) {
                    comptadorSeleccionat = comptadors.first()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error carregant comptadors", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Efecte per Fer Polling ("Bucle infinit" controlat) dependent del comptador i la precisió
    LaunchedEffect(comptadorSeleccionat, precisioSeleccionada) {
        val tagName = comptadorSeleccionat?.tagName
        if (tagName != null) {
            while (isActive) { // Mentre la corrutina (i la pantalla) estigui activa
                try {
                    val response = RetrofitClient.instance.getLiveValue("Bearer $token", tagName)
                    if (response.isSuccessful) {
                        liveValue = response.body()
                        errorConnection = false
                        // Mostrem horari actual de refresc
                        val df = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        lastUpdate = "Refrescat a les: ${df.format(java.util.Date())}"
                    } else {
                        errorConnection = true
                    }
                } catch (e: Exception) {
                    errorConnection = true
                }
                
                // Esperem segons la precisió, això "dorm" i desperta, 
                // permet ser super eficients sense bloquejar l'UI
                delay(precisioSeleccionada.second) 
            }
        }
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "CONSUMS EN VIU",
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filtres
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Selecció de Comptador
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = comptadorSeleccionat?.descripcio ?: "Carregant comptadors...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Equip a visualitzar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WaterDeep, focusedLabelColor = WaterDeep)
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            comptadors.forEach { comptador ->
                                DropdownMenuItem(
                                    text = { Text(comptador.descripcio ?: "Sense nom") },
                                    onClick = {
                                        comptadorSeleccionat = comptador
                                        liveValue = null // Resetejem visualment quan canviem
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Selecció de Precisió
                    ExposedDropdownMenuBox(
                        expanded = isPrecisioDropdownExpanded,
                        onExpandedChange = { isPrecisioDropdownExpanded = !isPrecisioDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = precisioSeleccionada.first,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Freqüència d'actualització") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPrecisioDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WaterDeep, focusedLabelColor = WaterDeep)
                        )
                        ExposedDropdownMenu(
                            expanded = isPrecisioDropdownExpanded,
                            onDismissRequest = { isPrecisioDropdownExpanded = false }
                        ) {
                            precisiOpcions.forEach { opcio ->
                                DropdownMenuItem(
                                    text = { Text(opcio.first) },
                                    onClick = {
                                        precisioSeleccionada = opcio
                                        isPrecisioDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARTELL GEGANT DE VALOR LIVE
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Valor Actual SCADA",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    if (liveValue != null) {
                        // Calculem el format segons el valor
                        val valorFinal = if (liveValue!! >= 10.0) {
                            liveValue!!.toInt().toString() // Més de 10: sense decimals
                        } else {
                            String.format("%.2f", liveValue) // Menys de 10: amb 2 decimals
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = valorFinal,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "l/h",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 12.dp) // Perquè la unitat quedi una mica més avall
                            )
                        }
                    } else if (errorConnection) {
                        Text(
                            text = "ERROR",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("No hi ha connexió amb SCADA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    } else {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Estat darrer refresc
                    Surface(
                        color = if (errorConnection) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = lastUpdate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (errorConnection) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Nota: Aquesta pantalla obté la dada en línia directament deSCADA. Mantindrà un refresc automàtic segons l'interval escollit.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
