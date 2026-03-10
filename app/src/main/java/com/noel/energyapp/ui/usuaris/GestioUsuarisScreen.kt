package com.noel.energyapp.ui.usuaris

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.noel.energyapp.ui.components.NoelScreen

// 1. Dades Temporals (Mock) per poder provar la pantalla sense la Base de Dades
data class UsuariAdminDto(val id_usuari: Int, val nom_usuari: String, val actiu: Boolean, val id_rol: Int)
data class PlantaAssignacioDto(val id_planta: Int, val nom_planta: String, val assignada: Boolean)

// Diccionari de rols tal com els tens a la BD
val mapRols = mapOf(1 to "ADMIN", 2 to "SUPERVISOR", 3 to "TECNIC")

@Composable
fun GestioUsuarisScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToGestioPlantes: () -> Unit,
    onNavigateToGestioUsuaris: () -> Unit
) {
    // 1. Dades Temporals
    var usuaris by remember {
        mutableStateOf(
            listOf(
                UsuariAdminDto(1, "admin_principal", true, 1),
                UsuariAdminDto(2, "supervisor_linia1", true, 2),
                UsuariAdminDto(3, "tecnic_manteniment", false, 3),
                UsuariAdminDto(4, "joan_garcia", true, 2),
                UsuariAdminDto(5, "maria_lopez", true, 3)
            )
        )
    }

    // Estats per controlar si mostrem els Pop-ups (Dialogs)
    var usuariSeleccionat_per_rol by remember { mutableStateOf<UsuariAdminDto?>(null) }
    var usuariSeleccionat_per_plantes by remember { mutableStateOf<UsuariAdminDto?>(null) }

    // Estats per a les noves funcions de seguretat i cerca
    var searchQuery by remember { mutableStateOf("") }
    var usuariAResetejar by remember { mutableStateOf<UsuariAdminDto?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    // FILTRE: Només mostrem els usuaris que coincideixin amb el buscador
    val usuarisFiltrats = usuaris.filter {
        it.nom_usuari.contains(searchQuery, ignoreCase = true)
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ D'USUARIS",
        hasMenu = true, // Mantenim l'hamburguesa per comoditat de l'Admin
        onBackClick = onBackClick,
        onNavigateToGestioPlantes = onNavigateToGestioPlantes,
        onNavigateToGestioUsuaris = onNavigateToGestioUsuaris,
        verticalArrangement = Arrangement.Top
    ) {
        // --- CAPÇALERA: BUSCADOR I BOTÓ CREAR ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Buscador
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

            // Botó flotant per crear nou usuari
            FloatingActionButton(
                onClick = { showCreateUserDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nou Usuari", tint = Color.White)
            }
        }

        // --- LLISTA D'USUARIS (FILTRADA) ---
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
                            Text(
                                text = usuari.nom_usuari,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (usuari.actiu) "ACTIU" else "INACTIU",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (usuari.actiu) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Switch(
                                    checked = usuari.actiu,
                                    onCheckedChange = { isChecked ->
                                        usuaris = usuaris.map {
                                            if (it.id_usuari == usuari.id_usuari) it.copy(actiu = isChecked) else it
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // NOU: Afegim el botó de Reset Pass al costat dels altres
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
                                Text(text = mapRols[usuari.id_rol] ?: "ROLS")
                            }

                            Button(
                                onClick = { usuariSeleccionat_per_plantes = usuari },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "PLANTES")
                            }

                            // BOTÓ DE RESET PASSWORD (NOMÉS ADMIN)
                            IconButton(
                                onClick = { usuariAResetejar = usuari }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = "Restablir Contrasenya",
                                    tint = MaterialTheme.colorScheme.error // Vermell per perill
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POP-UP: CONFIRMACIÓ RESET PASSWORD ---
    if (usuariAResetejar != null) {
        AlertDialog(
            onDismissRequest = { usuariAResetejar = null },
            title = { Text("Restablir Contrasenya") },
            text = { Text("Estàs segur que vols restablir la contrasenya de l'usuari '${usuariAResetejar?.nom_usuari}'? Es canviarà a '123456' i se l'obligarà a canviar-la quan entri.") },
            confirmButton = {
                Button(
                    onClick = {
                        // AQUÍ CRIDAREM A L'API: api/usuari/reset-password
                        usuariAResetejar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("SÍ, RESTABLIR")
                }
            },
            dismissButton = {
                TextButton(onClick = { usuariAResetejar = null }) { Text("Cancel·lar") }
            }
        )
    }

    // --- POP-UP: CREAR NOU USUARI ---
    if (showCreateUserDialog) {
        var nouNom by remember { mutableStateOf("") }
        var nouPassword by remember { mutableStateOf("") }
        var nouRol by remember { mutableStateOf(2) } // Per defecte Supervisor (2)

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text("Crear Nou Usuari") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nouNom,
                        onValueChange = { nouNom = it },
                        label = { Text("Nom d'usuari") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = nouPassword,
                        onValueChange = { nouPassword = it },
                        label = { Text("Contrasenya inicial") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )
                    Text("Selecciona el Rol:", style = MaterialTheme.typography.labelMedium)
                    mapRols.forEach { (id, nom) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (nouRol == id),
                                onClick = { nouRol = id }
                            )
                            Text(text = nom)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // AQUÍ CRIDAREM A L'API: api/usuari/crear
                    showCreateUserDialog = false
                }) {
                    Text("CREAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUserDialog = false }) { Text("Cancel·lar") }
            }
        )
    }

    // ... (La resta de pop-ups de Rol i Plantes es queden igual que els tenies) ...
    // --- POP-UP 1: SELECCIÓ DE ROL ---
    if (usuariSeleccionat_per_rol != null) {
        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_rol = null },
            title = { Text("Selecciona el Rol") },
            text = {
                Column {
                    mapRols.forEach { (id, nom) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            RadioButton(
                                selected = (usuariSeleccionat_per_rol?.id_rol == id),
                                onClick = {
                                    usuaris = usuaris.map {
                                        if (it.id_usuari == usuariSeleccionat_per_rol?.id_usuari) it.copy(id_rol = id) else it
                                    }
                                    usuariSeleccionat_per_rol = null
                                }
                            )
                            Text(text = nom)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { usuariSeleccionat_per_rol = null }) { Text("Cancel·lar") } }
        )
    }

    // --- POP-UP 2: ASSIGNACIÓ DE PLANTES ---
    if (usuariSeleccionat_per_plantes != null) {
        var plantesMocks by remember { mutableStateOf(listOf(PlantaAssignacioDto(1, "NOEL 1", true), PlantaAssignacioDto(3, "NOEL 3", false))) }
        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_plantes = null },
            title = { Text("Plantes Assignades") },
            text = {
                Column {
                    Text("Només es mostren les plantes actualment ACTIVES.", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    plantesMocks.forEach { planta ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = planta.assignada,
                                onCheckedChange = { isChecked ->
                                    plantesMocks = plantesMocks.map { if (it.id_planta == planta.id_planta) it.copy(assignada = isChecked) else it }
                                }
                            )
                            Text(text = planta.nom_planta)
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { usuariSeleccionat_per_plantes = null }) { Text("GUARDAR") } },
            dismissButton = { TextButton(onClick = { usuariSeleccionat_per_plantes = null }) { Text("Cancel·lar") } }
        )
    }
}