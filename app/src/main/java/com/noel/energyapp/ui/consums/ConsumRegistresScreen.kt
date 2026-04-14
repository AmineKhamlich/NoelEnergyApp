/**
 * FITXER: ConsumRegistresScreen.kt
 * CAPA: Interfície d'usuari → Consums (ui/consums)
 *
 * Aquesta pantalla permet a un supervisor o administrador revisar i corregir
 * els registres horaris de consum d'un comptador per a un dia concret.
 *
 * Flux d'ús:
 * 1. L'usuari selecciona un comptador de la planta al desplegable.
 * 2. L'usuari escull la data que vol consultar amb el DatePicker.
 * 3. En prémer "RECUPERAR REGISTRES", es carreguen totes les lectures horaries
 *    del comptador per a aquell dia (normalment 96 lectures de 15 minuts).
 * 4. Cada registre mostra l'hora de la lectura i el valor de consum en m³.
 *    Si un registre ha estat corregit, el valor original apareix ratllat
 *    i el valor corregit s'indica en taronja al costat.
 * 5. En tocar un registre, s'obre un diàleg de correcció on l'usuari pot:
 *    - Escriure un nou valor (es guarda a la columna ValorDifMod de la BD).
 *    - Posar Null (elimina la correcció i torna al valor original).
 *    - Cancel·lar (tanca el diàleg sense canviar res).
 *
 * Aquesta pantalla és clau per a la correcció manual de dades errònies que
 * han estat introduïdes incorrectament pel sistema SCADA.
 */
package com.noel.energyapp.ui.consums

// Importació per mostrar missatges curts Toast
import android.widget.Toast
// Importació dels components bàsics de Jetpack Compose
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
// Importació dels models de dades i client de xarxa de l'App
import com.noel.energyapp.data.DimCntDto
import com.noel.energyapp.data.FactCntHistorianDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.*
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
// Importació de les classes per treballar amb dates i hores
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Necessari per a les APIs experimentals de Material3 (DatePickerDialog, ExposedDropdownMenuBox)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumRegistresScreen(
    paddingValues: PaddingValues, // Marges del sistema Android
    plantaId: Int,                // ID numèric de la planta (per a futures crides filtrades)
    plantaNom: String,            // Nom de la planta per obtenir els comptadors associats
    onBackClick: () -> Unit       // Callback per tornar enrere a la pantalla de detall de la planta
) {
    // Obté el context d'Android necessari per als Toasts i el SessionManager
    val context = LocalContext.current
    // Crea l'àmbit de coroutines per a les crides de xarxa asíncrones
    val scope = rememberCoroutineScope()
    // Instancia el SessionManager per accedir al token JWT de la sessió activa
    val sessionManager = remember { SessionManager(context) }
    // Recupera el token JWT; string buit si no hi ha sessió activa
    val token = sessionManager.fetchAuthToken() ?: ""

    // Estat de la llista de comptadors disponibles per a la planta
    var comptadors by remember { mutableStateOf<List<DimCntDto>>(emptyList()) }
    // Estat del comptador escollit al desplegable (null fins que es carreguen)
    var comptadorSeleccionat by remember { mutableStateOf<DimCntDto?>(null) }
    // Estat booleà que controla si s'estan carregant els registres
    var isLoading by remember { mutableStateOf(false) }
    // Estat booleà que controla si el desplegable de comptadors és obert
    var isDropdownExpanded by remember { mutableStateOf(false) }
    // Estat de la data seleccionada per a la consulta, per defecte avui
    var dataSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    // Estat booleà que controla si es mostra el diàleg de selecció de data
    var showDatePicker by remember { mutableStateOf(false) }

    // Estat de la llista de registres horaris retornats per l'API
    var llistaRegistres by remember { mutableStateOf<List<FactCntHistorianDto>>(emptyList()) }
    // Estat del registre que l'usuari ha tocat i vol editar (null si no s'edita cap)
    var registreAEditar by remember { mutableStateOf<FactCntHistorianDto?>(null) }
    // Estat del text del camp de text del valor nou en el diàleg d'edició
    var nouValorEdicio by remember { mutableStateOf("") }

    // Formater de dates per mostrar la data en format llegible (ex: "14/04/2026")
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Carrega els comptadors de la planta una sola vegada en muntar el composable
    LaunchedEffect(plantaNom) {
        try {
            // Crida a l'API per obtenir la llista de comptadors de la planta
            val response = RetrofitClient.instance.getComptadorsPerPlanta("Bearer $token", plantaNom)
            if (response.isSuccessful) {
                comptadors = response.body() ?: emptyList() // Actualitza la llista de comptadors
                // Selecciona automàticament el primer comptador si la llista no és buida
                if (comptadors.isNotEmpty()) comptadorSeleccionat = comptadors.first()
            }
        } catch (e: Exception) {
            // Informa d'error si no es poden carregar els comptadors
            Toast.makeText(context, "Error carregant comptadors", Toast.LENGTH_SHORT).show()
        }
    }

    // Diàleg de selecció de la data de consulta (MaterialDatePicker de Material3)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            // Converteix la data seleccionada actual a milisegons per inicialitzar el picker
            initialSelectedDateMillis = dataSeleccionada.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false }, // Tanca sense canviar la data
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Converteix els milisegons a LocalDate en la zona horària local
                        dataSeleccionada = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false // Tanca el diàleg de selecció de data
                }) { Text("Acceptar") }
            }
        ) { DatePicker(state = datePickerState) } // Renderitza el component de selecció
    }

    // Diàleg de correcció del valor d'un registre specific
    // Es mostra quan l'usuari toca una fila de la llista de registres
    if (registreAEditar != null) {
        AlertDialog(
            onDismissRequest = { registreAEditar = null }, // Tanca el diàleg en clicar fora
            title = { Text("Corregir Lectura") },
            text = {
                Column {
                    // Mostra el valor original del registre per context
                    Text("Valor original SQL: ${registreAEditar?.valorDiferencial}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    // Camp de text on l'usuari escriu el valor corregit
                    OutlinedTextField(
                        value = nouValorEdicio,
                        onValueChange = { nouValorEdicio = it }, // Actualitza el text en escriure
                        label = { Text("Nou valor (ValorDifMod)") },
                        placeholder = { Text("Ex: 12.5") }, // Exemple de format esperat
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true // Un sol camp de text en una sola línia
                    )
                }
            },
            confirmButton = {
                // Botó de confirmació per desar el nou valor corregit
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumOrangeEnd), // Color taronja per a diferenciació visual
                    onClick = {
                        // Substitueix la coma per punt per permetre ambdós formats decimals
                        val valor = nouValorEdicio.replace(",", ".").toFloatOrNull()
                        if (valor != null) {
                            scope.launch {
                                try {
                                    // Envia la correcció a l'API amb el nou valor en m³
                                    val resp = RetrofitClient.instance.corregirValor("Bearer $token", registreAEditar!!.id, valor)
                                    if (resp.isSuccessful) {
                                        Toast.makeText(context, "Registre corregit!", Toast.LENGTH_SHORT).show()
                                        // Refresca automàticament la llista per reflectir la correcció
                                        val refresh = RetrofitClient.instance.getRegistresPerDia("Bearer $token", comptadorSeleccionat!!.id, dataSeleccionada.toString())
                                        if (refresh.isSuccessful) llistaRegistres = refresh.body() ?: emptyList()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al desar la correcció", Toast.LENGTH_SHORT).show()
                                }
                                registreAEditar = null // Tanca el diàleg després de guardar
                            }
                        } else {
                            // Informa l'usuari si el valor introduït no és un número vàlid
                            Toast.makeText(context, "Introdueix un número vàlid", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Desar", color = Color.White) }
            },
            dismissButton = {
                // Fila amb dos botons: un per anul·lar la correcció i un per cancel·lar
                Row {
                    // Botó per esborrar la correcció manual (posa ValorDifMod a NULL a la BD)
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                // Envia null com a 'nouValor' per eliminar la correcció existent
                                val resp = RetrofitClient.instance.corregirValor("Bearer $token", registreAEditar!!.id, null)
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Registre anul·lat", Toast.LENGTH_SHORT).show()
                                    // Refresca la llista per reflectir la restauració al valor original
                                    val refresh = RetrofitClient.instance.getRegistresPerDia("Bearer $token", comptadorSeleccionat!!.id, dataSeleccionada.toString())
                                    if (refresh.isSuccessful) llistaRegistres = refresh.body() ?: emptyList()
                                }
                            } catch (e: Exception) {
                                // Silencia l'error ja que la UI reflectirà l'estat correctament
                            }
                            registreAEditar = null // Tanca el diàleg
                        }
                    }) { Text("Posar Null", color = PremiumLogoutRed) } // Vermell per indicar eliminació
                    // Botó per tancar el diàleg sense fer cap canvi
                    TextButton(onClick = { registreAEditar = null }) { Text("Cancel·lar") }
                }
            }
        )
    }

    // Renderitza l'estructura base de la pantalla
    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ REGISTRES",            // Títol de la capçalera de la pantalla
        verticalArrangement = Arrangement.Top   // Els elements comencen des de dalt
    ) {
        // Columna scrollable que conté el formulari de filtre i la llista de registres
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()), // Permet scroll vertical
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espai entre elements de la columna
        ) {
            // --- TARGETA DE FILTRE (Comptador + Data + Botó) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Desplegable per seleccionar el comptador de la planta
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = comptadorSeleccionat?.descripcio ?: "Seleccionant...", // Text actual
                            onValueChange = {},
                            readOnly = true, // No és editable; s'usa el desplegable
                            label = { Text("Comptador") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        // Menú desplegable amb tots els comptadors disponibles
                        ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                            comptadors.forEach { c ->
                                DropdownMenuItem(text = { Text(c.descripcio ?: "") }, onClick = {
                                    comptadorSeleccionat = c  // Selecciona el comptador escollit
                                    isDropdownExpanded = false // Tanca el desplegable
                                })
                            }
                        }
                    }

                    // Targeta de selecció de data (obre el DatePicker en tocar-la)
                    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Data a consultar", style = MaterialTheme.typography.labelSmall)
                            Text(dataSeleccionada.format(formatter), fontWeight = FontWeight.Bold) // Mostra la data en format dd/MM/yyyy
                        }
                    }

                    // Botó que executa la crida a l'API per obtenir els registres del dia
                    NoelButton(
                        text = if (isLoading) "BUSCANT..." else "RECUPERAR REGISTRES",
                        isLoading = isLoading, // Controla el mode de càrrega del botó
                        onClick = {
                            scope.launch {
                                isLoading = true // Activa la rodeta de càrrega
                                try {
                                    // Crida a l'API per obtenir tots els registres horaris del comptador per al dia
                                    val response = RetrofitClient.instance.getRegistresPerDia(
                                        "Bearer $token",
                                        comptadorSeleccionat?.id ?: 0, // ID del comptador (0 si no n'hi ha cap seleccionat)
                                        dataSeleccionada.toString()     // Data en format ISO: "YYYY-MM-DD"
                                    )
                                    if (response.isSuccessful) {
                                        llistaRegistres = response.body() ?: emptyList()
                                        // Informa si no hi ha dades per al dia i comptador seleccionats
                                        if (llistaRegistres.isEmpty()) Toast.makeText(context, "No hi ha dades per aquest dia", Toast.LENGTH_SHORT).show()
                                    }
                                } finally { isLoading = false } // Desactiva sempre la rodeta de càrrega
                            }
                        }
                    )
                }
            }

            // --- LLISTA DE REGISTRES (Es renderitza quan hi ha dades carregades) ---
            if (llistaRegistres.isNotEmpty()) {
                // Títol de la secció que indica el nombre de registres obtinguts
                Text("Lectures del dia (${llistaRegistres.size})", style = MaterialTheme.typography.titleSmall)

                // Targeta que conté totes les files de registres horaris
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Per cada registre de la llista, renderitza una fila clicable
                        llistaRegistres.forEachIndexed { index, reg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        registreAEditar = reg // Estableix el registre a editar
                                        // Pre-omple el camp d'edició amb el valor corregit si existeix, o buit
                                        nouValorEdicio = reg.valorDifMod?.toString() ?: ""
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Calcula l'etiqueta de temps (hora:minut) per a la fila
                                val timeLabel = try {
                                    if (!reg.fechaFin.isNullOrEmpty()) {
                                        // Parseja la data/hora del registre
                                        val parsed = java.time.LocalDateTime.parse(reg.fechaFin)
                                        val m = parsed.minute
                                        // Arrodoneix els minuts a la franja de 15 minuts més propera per sota
                                        val minuteRounded = (m / 15) * 15
                                        val snapped = parsed.withMinute(minuteRounded).withSecond(0)
                                        // Formata l'hora en format HH:mm (ex: "14:00", "14:15")
                                        snapped.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                    } else {
                                        "Punt #${index + 1}" // Fallback si la data és null
                                    }
                                } catch (e: Exception) {
                                    "Punt #${index + 1}" // Fallback si el parseig falla
                                }

                                // Etiqueta de l'hora del registre en color primari
                                Text(
                                    text = timeLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Fila derecha amb els valors original i corregit
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Valor original de la BD (ratllat si hi ha una correcció manual)
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.3f", reg.valorDiferencial)} m³",
                                        style = MaterialTheme.typography.bodyLarge,
                                        // Ratllar el text si el registre ha estat corregit
                                        textDecoration = if (reg.valorDifMod != null) TextDecoration.LineThrough else null,
                                        // Grisa si ha estat corregit, normal si no
                                        color = if (reg.valorDifMod != null) Color.Gray else MaterialTheme.colorScheme.onSurface
                                    )

                                    // Valor corregit (ValorDifMod), mostrat en taronja al costat del ratllat
                                    if (reg.valorDifMod != null) {
                                        Spacer(Modifier.width(8.dp)) // Espai entre el valor original i el corregit
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.3f", reg.valorDifMod)} m³",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = PremiumOrangeEnd // Color taronja per indicar que és una correcció
                                        )
                                    }
                                }
                            }
                            // Divisor horitzontal entre files (no es posa a l'última fila)
                            if (index < llistaRegistres.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }

            // Espai extra al final per evitar que l'última fila quedi tapada per la barra de navegació
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}
