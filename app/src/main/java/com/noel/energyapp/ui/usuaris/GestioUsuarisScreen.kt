package com.noel.energyapp.ui.usuaris

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.CrearUsuariDto
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.data.UpdateUsuariDto
import com.noel.energyapp.data.UsuariResumDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

// Llista de rols permesos al sistema SCADA de Noel
// ---------------------------------------------------------
// EINES DE TRADUCCIÓ (UI <-> Base de Dades)
// ---------------------------------------------------------
val rolsDisponibles = listOf("ADMIN", "SUPERVISOR", "TÈCNIC") // Llista visual amb accents

// Converteix el que ve de la BD (ex: TECNIC) al que veu l'usuari (ex: TÈCNIC)
fun rolDBToVisual(rolDB: String): String {
    return if (rolDB.equals("TECNIC", ignoreCase = true)) "TÈCNIC" else rolDB.uppercase()
}

// Converteix el que veu l'usuari (ex: TÈCNIC) al que entén la BD (ex: TECNIC)
fun rolVisualToDB(rolUI: String): String {
    return if (rolUI == "TÈCNIC") "TECNIC" else rolUI
}
// ---------------------------------------------------------

@Composable
fun GestioUsuarisScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToGestioPlantes: () -> Unit,
    onNavigateToGestioUsuaris: () -> Unit
) {
    // --- 1. CONFIGURACIÓ I CONTEXT ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    // --- 2. ESTATS DE LES DADES ---
    var usuaris by remember { mutableStateOf<List<UsuariResumDto>>(emptyList()) }
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- 3. ESTATS DELS POP-UPS (DIÀLEGS) ---
    var usuariSeleccionat_per_rol by remember { mutableStateOf<UsuariResumDto?>(null) }
    var usuariSeleccionat_per_plantes by remember { mutableStateOf<UsuariResumDto?>(null) }
    var usuariAResetejar by remember { mutableStateOf<UsuariResumDto?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Funció interna per refrescar la llista d'usuaris i plantes des de l'API
    fun recarregarDades() {
        scope.launch {
            isLoading = true
            try {
                // Obtenim usuaris i plantes en paral·lel per eficiència
                val resUsuaris = RetrofitClient.instance.getUsuaris("Bearer $token")
                val resPlantes = RetrofitClient.instance.getPlantes("Bearer $token")

                if (resUsuaris.isSuccessful) usuaris = resUsuaris.body() ?: emptyList()
                if (resPlantes.isSuccessful) plantes = resPlantes.body() ?: emptyList()

            } catch (e: Exception) {
                Toast.makeText(context, "Error de connexió al servidor", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Carreguem les dades només entrar a la pantalla
    LaunchedEffect(Unit) {
        recarregarDades()
    }

    // --- 4. LÒGICA DE FILTRATGE INTEL·LIGENT ---
    // Ara busquem tant pel Nick (@username) com pel Nom o Cognom real
    val usuarisFiltrats = usuaris.filter {
        it.nomUsuari.contains(searchQuery, ignoreCase = true) ||
                it.nom.contains(searchQuery, ignoreCase = true) ||
                it.cognom.contains(searchQuery, ignoreCase = true)
    }

    // --- 5. ESTRUCTURA VISUAL (NoelScreen) ---
    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ D'USUARIS",
        hasMenu = true,
        onBackClick = onBackClick,
        onNavigateToGestioPlantes = onNavigateToGestioPlantes,
        onNavigateToGestioUsuaris = onNavigateToGestioUsuaris,
        verticalArrangement = Arrangement.Top
    ) {
        // --- BARRA DE RECERCA I BOTÓ D'AFEGIR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar per nom o nick...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = { showCreateUserDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nou Usuari", tint = Color.White)
            }
        }

        // --- CONTINGUT PRINCIPAL ---
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                items(usuarisFiltrats) { usuari ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // FILA SUPERIOR: Noms i Switch d'Actiu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Determinem què mostrem en gran (Prioritat Nom Real)
                                    val títolCard =
                                        if (usuari.nom.isNotBlank()) "${usuari.nom} ${usuari.cognom}" else usuari.nomUsuari

                                    Text(
                                        text = títolCard,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Si estem mostrant el nom real a dalt, mostrem el @nick a sota
                                    if (usuari.nom.isNotBlank()) {
                                        Text(
                                            text = "@${usuari.nomUsuari}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Estat Actiu / Inactiu
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isActiu = usuari.actiu == true

                                    // Decidim si el Switch ha d'estar actiu o bloquejat
                                    // Si l'usuari de la llista és ADMIN, no permetem tocar l'estat d'activació
                                    val esAdmin = usuari.rol.equals("ADMIN", ignoreCase = true)

                                    Text(
                                        text = if (isActiu) "ACTIU" else "INACTIU",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            esAdmin -> Color.Gray // Si és admin, ho posem en gris (protegit)
                                            isActiu -> Color(0xFF4CAF50) // Verd si és actiu
                                            else -> MaterialTheme.colorScheme.error // Vermell si és inactiu
                                        },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Switch(
                                        checked = isActiu,
                                        enabled = !esAdmin,
                                        onCheckedChange = { isChecked ->
                                            scope.launch {
                                                // Optimisme visual
                                                usuaris = usuaris.map {
                                                    if (it.id == usuari.id) it.copy(actiu = isChecked) else it
                                                }
                                                try {
                                                    val request = UpdateUsuariDto(
                                                        usuari.id,
                                                        null,
                                                        isChecked,
                                                        null,
                                                        null
                                                    )
                                                    RetrofitClient.instance.actualitzarUsuari(
                                                        "Bearer $token",
                                                        request
                                                    )
                                                } catch (e: Exception) {
                                                    recarregarDades()
                                                    Toast.makeText(
                                                        context,
                                                        "Error al servidor",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        },
                                        // Podem posar colors diferents quan està bloquejat
                                        colors = SwitchDefaults.colors(
                                            disabledCheckedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f), // Verd claret si està ON però bloquejat
                                            disabledCheckedThumbColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // FILA INFERIOR: Botons d'acció (Rol, Plantes, Reset)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botó per canviar el Rol (mostra el rol actual)
                                Button(
                                    onClick = { usuariSeleccionat_per_rol = usuari },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text(
                                        text = usuari.rol,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                // Botó per assignar plantes
                                Button(
                                    onClick = { usuariSeleccionat_per_plantes = usuari },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "PLANTES",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                // Botó vermell per reset de contrasenya (123456)
                                IconButton(onClick = { usuariAResetejar = usuari }) {
                                    Icon(
                                        Icons.Default.LockReset,
                                        contentDescription = "Reset Password",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 6. POP-UPS DE GESTIÓ ---

    // DIÀLEG: RESET PASSWORD
    if (usuariAResetejar != null) {
        AlertDialog(
            onDismissRequest = { usuariAResetejar = null },
            title = { Text("Restablir Contrasenya") },
            text = { Text("Estàs segur que vols restablir la contrasenya de '${usuariAResetejar?.nomUsuari}'? Es canviarà a '123456' i se l'obligarà a canviar-la en el proper inici.") },
            confirmButton = {
                Button(
                    onClick = {
                        val usernameTarget = usuariAResetejar?.nomUsuari ?: ""
                        scope.launch {
                            try {
                                val response =
                                    RetrofitClient.instance.resetPassword(mapOf("username" to usernameTarget))
                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Contrasenya restablerta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    recarregarDades()
                                }
                            } finally {
                                usuariAResetejar = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("SÍ, RESTABLIR") }
            },
            dismissButton = {
                TextButton(onClick = {
                    usuariAResetejar = null
                }) { Text("Cancel·lar") }
            }
        )
    }

    // DIÀLEG: CREAR NOU USUARI
    if (showCreateUserDialog) {
        var nouNick by remember { mutableStateOf("") }
        var nouNomReal by remember { mutableStateOf("") }
        var nouCognom by remember { mutableStateOf("") }
        var nouRol by remember { mutableStateOf("TÈCNIC") }

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text("Crear Nou Usuari") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Configuració inicial de l'empleat:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nouNick,
                        onValueChange = { nouNick = it },
                        label = { Text("Nom d'usuari (@nick)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nouNomReal,
                        onValueChange = { nouNomReal = it },
                        label = { Text("Nom real") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nouCognom,
                        onValueChange = { nouCognom = it },
                        label = { Text("Cognom real") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Selecciona el Rol d'accés:", style = MaterialTheme.typography.labelMedium)
                    rolsDisponibles.forEach { rolNom ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (nouRol == rolNom),
                                onClick = { nouRol = rolNom })
                            Text(text = rolNom)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nouNick.isBlank() || nouNomReal.isBlank()) {
                        Toast.makeText(
                            context,
                            "Mínim Nick i Nom són obligatoris",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    scope.launch {
                        try {
                            val rolPerAPI = rolVisualToDB(nouRol)
                            val request =
                                CrearUsuariDto(nouNick, nouNomReal, nouCognom, nouRol, emptyList())
                            val response =
                                RetrofitClient.instance.crearUsuari("Bearer $token", request)
                            if (response.isSuccessful) {
                                recarregarDades()
                                showCreateUserDialog = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Aquest nick ja existeix!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) { /* Error */
                        }
                    }
                }) { Text("CREAR USUARI") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateUserDialog = false
                }) { Text("Cancel·lar") }
            }
        )
    }

    /// DIÀLEG: CANVI DE ROL
    if (usuariSeleccionat_per_rol != null) {
        val usuari = usuariSeleccionat_per_rol!!
        // 1. Traduïm el rol que ve de la BD ("TECNIC") al format visual ("TÈCNIC") per poder-lo comparar
        val rolActualVisual = rolDBToVisual(usuari.rol)

        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_rol = null },
            title = { Text("Canviar Rol") },
            text = {
                Column {
                    rolsDisponibles.forEach { rolNom ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                // 2. Ara comparem visual amb visual (Així el botó es marcarà bé!)
                                selected = (rolActualVisual == rolNom),
                                onClick = {
                                    scope.launch {
                                        try {
                                            // 3. Tornem a traduir cap a la BD abans d'enviar
                                            val rolPerAPI = rolVisualToDB(rolNom)

                                            val request = UpdateUsuariDto(
                                                usuari.id,
                                                nouRol = rolPerAPI,
                                                actiu = null,
                                                canviPasswordObligatori = null,
                                                idsPlantes = null
                                            )
                                            RetrofitClient.instance.actualitzarUsuari(
                                                "Bearer $token",
                                                request
                                            )
                                            recarregarDades()
                                        } finally {
                                            usuariSeleccionat_per_rol = null
                                        }
                                    }
                                }
                            )
                            Text(text = rolNom)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    usuariSeleccionat_per_rol = null
                }) { Text("Tancar") }
            }
        )
    }

    // DIÀLEG: ASSIGNACIÓ DE PLANTES (Accés granular)
    if (usuariSeleccionat_per_plantes != null) {
        val user = usuariSeleccionat_per_plantes!!
        var plantesSeleccionades by remember {
            mutableStateOf(
                user.idsPlantes?.toSet() ?: emptySet()
            )
        }

        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_plantes = null },
            title = { Text("Permisos de Planta") },
            text = {
                Column {
                    Text(
                        "Selecciona a quines plantes pot accedir ${user.nomUsuari}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    plantes.filter { it.activa }.forEach { planta ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = plantesSeleccionades.contains(planta.id_planta),
                                onCheckedChange = { isChecked ->
                                    plantesSeleccionades =
                                        if (isChecked) plantesSeleccionades + planta.id_planta else plantesSeleccionades - planta.id_planta
                                }
                            )
                            Text(text = planta.nom_planta)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            val request = UpdateUsuariDto(
                                user.id,
                                null,
                                null,
                                null,
                                plantesSeleccionades.toList()
                            )
                            RetrofitClient.instance.actualitzarUsuari("Bearer $token", request)
                            recarregarDades()
                        } finally {
                            usuariSeleccionat_per_plantes = null
                        }
                    }
                }) { Text("GUARDAR") }
            },
            dismissButton = {
                TextButton(onClick = { usuariSeleccionat_per_plantes = null }) {
                    Text(
                        "Cancel·lar"
                    )
                }
            }
        )
    }
}