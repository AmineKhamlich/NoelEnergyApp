package com.noel.energyapp.ui.consums

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.DimCntDto
import com.noel.energyapp.data.FactCntHistorianDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.*
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumRegistresScreen(
    paddingValues: PaddingValues,
    plantaId: Int,
    plantaNom: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    // --- ESTATS DE LA PANTALLA ---
    var comptadors by remember { mutableStateOf<List<DimCntDto>>(emptyList()) }
    var comptadorSeleccionat by remember { mutableStateOf<DimCntDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var dataSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // --- ESTATS DE LA LLISTA I EDICIÓ ---
    var llistaRegistres by remember { mutableStateOf<List<FactCntHistorianDto>>(emptyList()) }
    var registreAEditar by remember { mutableStateOf<FactCntHistorianDto?>(null) }
    var nouValorEdicio by remember { mutableStateOf("") }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // 1. Càrrega inicial dels comptadors de la planta
    LaunchedEffect(plantaNom) {
        try {
            val response = RetrofitClient.instance.getComptadorsPerPlanta("Bearer $token", plantaNom)
            if (response.isSuccessful) {
                comptadors = response.body() ?: emptyList()
                if (comptadors.isNotEmpty()) comptadorSeleccionat = comptadors.first()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error carregant comptadors", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Selector de Data (DatePicker)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dataSeleccionada.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataSeleccionada = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Acceptar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // 3. DIÀLEG D'EDICIÓ (Infobox per corregir el valor)
    if (registreAEditar != null) {
        AlertDialog(
            onDismissRequest = { registreAEditar = null },
            title = { Text("Corregir Lectura") },
            text = {
                Column {
                    Text("Valor original SQL: ${registreAEditar?.valorDiferencial}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = nouValorEdicio,
                        onValueChange = { nouValorEdicio = it },
                        label = { Text("Nou valor (ValorDifMod)") },
                        placeholder = { Text("Ex: 12.5") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumOrangeEnd),
                    onClick = {
                        val valor = nouValorEdicio.replace(",", ".").toFloatOrNull()
                        if (valor != null) {
                            scope.launch {
                                try {
                                    val resp = RetrofitClient.instance.corregirValor("Bearer $token", registreAEditar!!.id, valor)
                                    if (resp.isSuccessful) {
                                        Toast.makeText(context, "Registre corregit!", Toast.LENGTH_SHORT).show()
                                        val refresh = RetrofitClient.instance.getRegistresPerDia("Bearer $token", comptadorSeleccionat!!.id, dataSeleccionada.toString())
                                        if (refresh.isSuccessful) llistaRegistres = refresh.body() ?: emptyList()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al desar la correcció", Toast.LENGTH_SHORT).show()
                                }
                                registreAEditar = null
                            }
                        } else {
                            Toast.makeText(context, "Introdueix un número vàlid", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Desar", color = Color.White) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                val resp = RetrofitClient.instance.corregirValor("Bearer $token", registreAEditar!!.id, null)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Registre anul·lat", Toast.LENGTH_SHORT).show()
                                    val refresh = RetrofitClient.instance.getRegistresPerDia("Bearer $token", comptadorSeleccionat!!.id, dataSeleccionada.toString())
                                    if (refresh.isSuccessful) llistaRegistres = refresh.body() ?: emptyList()
                                }
                            } catch (e: Exception) {
                            }
                            registreAEditar = null
                        }
                    }) { Text("Posar Null", color = PremiumLogoutRed) }
                    TextButton(onClick = { registreAEditar = null }) { Text("Cancel·lar") }
                }
            }
        )
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ REGISTRES",
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()), // El scroll envolta tot el contingut
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TARGETA DE FILTRE ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Selector Comptador
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = comptadorSeleccionat?.descripcio ?: "Seleccionant...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Comptador") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                            comptadors.forEach { c ->
                                DropdownMenuItem(text = { Text(c.descripcio ?: "") }, onClick = {
                                    comptadorSeleccionat = c
                                    isDropdownExpanded = false
                                })
                            }
                        }
                    }

                    // Selector Data
                    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Data a consultar", style = MaterialTheme.typography.labelSmall)
                            Text(dataSeleccionada.format(formatter), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Botó Buscar
                    NoelButton(
                        text = if (isLoading) "BUSCANT..." else "RECUPERAR REGISTRES",
                        isLoading = isLoading,
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    val response = RetrofitClient.instance.getRegistresPerDia(
                                        "Bearer $token",
                                        comptadorSeleccionat?.id ?: 0,
                                        dataSeleccionada.toString() // Envia YYYY-MM-DD
                                    )
                                    if (response.isSuccessful) {
                                        llistaRegistres = response.body() ?: emptyList()
                                        if (llistaRegistres.isEmpty()) Toast.makeText(context, "No hi ha dades per aquest dia", Toast.LENGTH_SHORT).show()
                                    }
                                } finally { isLoading = false }
                            }
                        }
                    )
                }
            }

            // --- LLISTA DE REGISTRES (Es mostra quan tenim dades) ---
            if (llistaRegistres.isNotEmpty()) {
                Text("Lectures del dia (${llistaRegistres.size})", style = MaterialTheme.typography.titleSmall)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        llistaRegistres.forEachIndexed { index, reg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        registreAEditar = reg
                                        nouValorEdicio = reg.valorDifMod?.toString() ?: ""
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val timeLabel = try {
                                    if (!reg.fechaFin.isNullOrEmpty()) {
                                        val parsed = java.time.LocalDateTime.parse(reg.fechaFin)
                                        val m = parsed.minute
                                        // Arrodonim a la franja de 15 minuts més propera per sota
                                        val minuteRounded = (m / 15) * 15
                                        val snapped = parsed.withMinute(minuteRounded).withSecond(0)
                                        snapped.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                    } else {
                                        "Punt #${index + 1}"
                                    }
                                } catch (e: Exception) {
                                    "Punt #${index + 1}"
                                }
                                Text(
                                    text = timeLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Valor Original (Ratllat si hi ha correcció)
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.3f", reg.valorDiferencial)} m³",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textDecoration = if (reg.valorDifMod != null) TextDecoration.LineThrough else null,
                                        color = if (reg.valorDifMod != null) Color.Gray else MaterialTheme.colorScheme.onSurface
                                    )

                                    // Valor Corregit (En taronja premium)
                                    if (reg.valorDifMod != null) {
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.3f", reg.valorDifMod)} m³",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = PremiumOrangeEnd
                                        )
                                    }
                                }
                            }
                            if (index < llistaRegistres.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}
