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

val rolsDisponibles = listOf("ADMIN", "SUPERVISOR", "TÈCNIC")

@Composable
fun GestioUsuarisScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToGestioPlantes: () -> Unit,
    onNavigateToGestioUsuaris: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    // --- ESTATS REALS DE L'API ---
    var usuaris by remember { mutableStateOf<List<UsuariResumDto>>(emptyList()) }
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Estats dels Pop-ups
    var usuariSeleccionat_per_rol by remember { mutableStateOf<UsuariResumDto?>(null) }
    var usuariSeleccionat_per_plantes by remember { mutableStateOf<UsuariResumDto?>(null) }
    var usuariAResetejar by remember { mutableStateOf<UsuariResumDto?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Funció per recarregar dades fresques de l'API
    fun recarregarDades() {
        scope.launch {
            isLoading = true
            try {
                val resUsuaris = RetrofitClient.instance.getUsuaris("Bearer $token")
                if (resUsuaris.isSuccessful) {
                    usuaris = resUsuaris.body() ?: emptyList()
                }

                val resPlantes = RetrofitClient.instance.getPlantes("Bearer $token")
                if (resPlantes.isSuccessful) {
                    plantes = resPlantes.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        recarregarDades()
    }

    // Filtre del cercador: Ara busca pel "nick" (nomUsuari)
    val usuarisFiltrats = usuaris.filter {
        it.nomUsuari.contains(searchQuery, ignoreCase = true)
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ D'USUARIS",
        hasMenu = true,
        onBackClick = onBackClick,
        onNavigateToGestioPlantes = onNavigateToGestioPlantes,
        onNavigateToGestioUsuaris = onNavigateToGestioUsuaris,
        verticalArrangement = Arrangement.Top
    ) {
        // --- CAPÇALERA: BUSCADOR I BOTÓ NOU USUARI ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar usuari...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = { showCreateUserDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nou Usuari", tint = Color.White)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // --- LLISTA D'USUARIS FILTRADA ---
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(usuarisFiltrats) { usuari ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Mostrem el Nick en gran
                                    Text(
                                        text = usuari.nomUsuari,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    // I el nom real en petitet a sota (si en té)
                                    if (usuari.nom.isNotBlank() || usuari.cognom.isNotBlank()) {
                                        Text(
                                            text = "${usuari.nom} ${usuari.cognom}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isActiu = usuari.actiu == true
                                    Text(
                                        text = if (isActiu) "ACTIU" else "INACTIU",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActiu) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Switch(
                                        checked = isActiu,
                                        onCheckedChange = { isChecked ->
                                            scope.launch {
                                                usuaris = usuaris.map {
                                                    if (it.id == usuari.id) it.copy(actiu = isChecked) else it
                                                }
                                                try {
                                                    val request = UpdateUsuariDto(usuari.id, null, isChecked, null, null)
                                                    RetrofitClient.instance.actualitzarUsuari("Bearer $token", request)
                                                } catch (e: Exception) {
                                                    recarregarDades()
                                                    Toast.makeText(context, "Error al guardar l'estat", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { usuariSeleccionat_per_rol = usuari },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text(text = usuari.rol)
                                }

                                Button(
                                    onClick = { usuariSeleccionat_per_plantes = usuari },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "PLANTES")
                                }

                                IconButton(onClick = { usuariAResetejar = usuari }) {
                                    Icon(Icons.Default.LockReset, contentDescription = "Restablir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POP-UP: RESTABLIR CONTRASENYA ---
    if (usuariAResetejar != null) {
        AlertDialog(
            onDismissRequest = { usuariAResetejar = null },
            title = { Text("Restablir Contrasenya") },
            text = { Text("Estàs segur que vols restablir la contrasenya de '${usuariAResetejar?.nomUsuari}'? Es canviarà a '123456' i se l'obligarà a canviar-la.") },
            confirmButton = {
                Button(
                    onClick = {
                        val usernameTarget = usuariAResetejar?.nomUsuari ?: ""
                        scope.launch {
                            try {
                                val response = RetrofitClient.instance.resetPassword(mapOf("username" to usernameTarget))
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Contrasenya restablerta a 123456", Toast.LENGTH_SHORT).show()
                                    recarregarDades()
                                } else {
                                    Toast.makeText(context, "Error a l'intentar restablir", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                            } finally {
                                usuariAResetejar = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("SÍ, RESTABLIR") }
            },
            dismissButton = { TextButton(onClick = { usuariAResetejar = null }) { Text("Cancel·lar") } }
        )
    }

    // --- POP-UP: CREAR NOU USUARI ---
    if (showCreateUserDialog) {
        var nouNick by remember { mutableStateOf("") }
        var nouNomReal by remember { mutableStateOf("") }
        var nouCognom by remember { mutableStateOf("") }
        var nouRol by remember { mutableStateOf("SUPERVISOR") }

        // Creem els enllaços per saltar d'un camp a l'altre
        val nomFocusRequester = remember { FocusRequester() }
        val cognomFocusRequester = remember { FocusRequester() }

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text("Crear Nou Usuari") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("La contrasenya s'establirà a '123456' automàticament.", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                    OutlinedTextField(
                        value = nouNick,
                        onValueChange = { nouNick = it },
                        label = { Text("Nom d'usuari") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = nouNomReal,
                        onValueChange = { nouNomReal = it },
                        label = { Text("Nom real") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = nouCognom,
                        onValueChange = { nouCognom = it },
                        label = { Text("Cognom real") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    Text("Selecciona el Rol:", style = MaterialTheme.typography.labelMedium)
                    rolsDisponibles.forEach { rolNom ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (nouRol == rolNom),
                                onClick = { nouRol = rolNom }
                            )
                            Text(text = rolNom)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nouNick.isBlank() || nouNomReal.isBlank() || nouCognom.isBlank()) {
                        Toast.makeText(context, "Tots els camps són obligatoris", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        try {
                            // Enviem el nou DTO sense password, però amb nom i cognom!
                            val request = CrearUsuariDto(
                                username = nouNick,
                                nom = nouNomReal,
                                cognom = nouCognom,
                                rol = nouRol,
                                idsPlantes = emptyList()
                            )
                            val response = RetrofitClient.instance.crearUsuari("Bearer $token", request)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Usuari creat correctament!", Toast.LENGTH_SHORT).show()
                                recarregarDades()
                                showCreateUserDialog = false
                            } else {
                                // Aquí és on el C# ens retorna l'error de duplicat!
                                Toast.makeText(context, "Aquest nom d'usuari ja existeix!", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("CREAR") }
            },
            dismissButton = { TextButton(onClick = { showCreateUserDialog = false }) { Text("Cancel·lar") } }
        )
    }

    // --- POP-UP 1: SELECCIÓ DE ROL ---
    if (usuariSeleccionat_per_rol != null) {
        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_rol = null },
            title = { Text("Canviar Rol") },
            text = {
                Column {
                    rolsDisponibles.forEach { rolNom ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            RadioButton(
                                selected = (usuariSeleccionat_per_rol?.rol == rolNom),
                                onClick = {
                                    val user = usuariSeleccionat_per_rol!!
                                    scope.launch {
                                        try {
                                            val request = UpdateUsuariDto(user.id, nouRol = rolNom, actiu = null, canviPasswordObligatori = null, idsPlantes = null)
                                            val res = RetrofitClient.instance.actualitzarUsuari("Bearer $token", request)
                                            if (res.isSuccessful) {
                                                recarregarDades()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error al canviar rol", Toast.LENGTH_SHORT).show()
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
            confirmButton = { TextButton(onClick = { usuariSeleccionat_per_rol = null }) { Text("Cancel·lar") } }
        )
    }

    // --- POP-UP 2: ASSIGNACIÓ DE PLANTES ---
    if (usuariSeleccionat_per_plantes != null) {
        val user = usuariSeleccionat_per_plantes!!
        var plantesSeleccionades by remember { mutableStateOf(user.idsPlantes?.toSet() ?: emptySet()) }

        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_plantes = null },
            title = { Text("Plantes Assignades") },
            text = {
                Column {
                    Text("Pots donar accés a les plantes d'aquesta llista:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                    plantes.filter { it.activa }.forEach { planta ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = plantesSeleccionades.contains(planta.id_planta),
                                onCheckedChange = { isChecked ->
                                    plantesSeleccionades = if (isChecked) {
                                        plantesSeleccionades + planta.id_planta
                                    } else {
                                        plantesSeleccionades - planta.id_planta
                                    }
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
                                idUsuari = user.id,
                                nouRol = null,
                                actiu = null,
                                canviPasswordObligatori = null,
                                idsPlantes = plantesSeleccionades.toList()
                            )
                            val response = RetrofitClient.instance.actualitzarUsuari("Bearer $token", request)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Accessos actualitzats!", Toast.LENGTH_SHORT).show()
                                recarregarDades()
                            } else {
                                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                        } finally {
                            usuariSeleccionat_per_plantes = null
                        }
                    }
                }) { Text("GUARDAR") }
            },
            dismissButton = { TextButton(onClick = { usuariSeleccionat_per_plantes = null }) { Text("Cancel·lar") } }
        )
    }
}