package com.noel.energyapp.ui.usuaris

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noel.energyapp.ui.components.NoelScreen

// 1. Dades Temporals (Mock) per poder provar la pantalla sense la Base de Dades
data class UsuariAdminDto(val id_usuari: Int, val nom_usuari: String, val actiu: Boolean, val id_rol: Int)
data class PlantaAssignacioDto(val id_planta: Int, val nom_planta: String, val assignada: Boolean)

// Diccionari de rols tal com els tens a la BD
val mapRols = mapOf(1 to "ADMIN", 2 to "SUPERVISOR", 3 to "TÈCNIC")

@Composable
fun GestioUsuarisScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToGestioPlantes: () -> Unit,
    onNavigateToGestioUsuaris: () -> Unit
) {
    // Llista d'usuaris (Estat reactiu)
    var usuaris by remember {
        mutableStateOf(
            listOf(
                UsuariAdminDto(1, "admin_principal", true, 1),
                UsuariAdminDto(2, "supervisor_linia1", true, 2),
                UsuariAdminDto(3, "tecnic_manteniment", false, 3)
            )
        )
    }

    // Estats per controlar si mostrem els Pop-ups (Dialogs)
    var usuariSeleccionat_per_rol by remember { mutableStateOf<UsuariAdminDto?>(null) }
    var usuariSeleccionat_per_plantes by remember { mutableStateOf<UsuariAdminDto?>(null) }

    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ D'USUARIS",
        hasMenu = true, // Mantenim l'hamburguesa per comoditat de l'Admin
        onBackClick = onBackClick,
        onNavigateToGestioPlantes = onNavigateToGestioPlantes,
        onNavigateToGestioUsuaris = onNavigateToGestioUsuaris,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Gestiona els rols, l'accés i les plantes assignades a cada usuari.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // LAZYCOLUMN: El nostre "RecyclerView" modern i eficient
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(usuaris) { usuari ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // FILA 1: Nom de l'usuari i Interruptor Actiu/Inactiu
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
                                        // Actualitzem l'estat visual de l'usuari
                                        usuaris = usuaris.map {
                                            if (it.id_usuari == usuari.id_usuari) it.copy(actiu = isChecked) else it
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // FILA 2: Botons per canviar Rol i assignar Plantes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Espai entre botons
                        ) {
                            // Botó de ROL
                            Button(
                                onClick = { usuariSeleccionat_per_rol = usuari },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(text = mapRols[usuari.id_rol] ?: "DESCONEGUT")
                            }

                            // Botó de PLANTES
                            Button(
                                onClick = { usuariSeleccionat_per_plantes = usuari },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "PLANTES")
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POP-UP 1: SELECCIÓ DE ROL ---
    // Només es dibuixa si hi ha un usuari seleccionat
    if (usuariSeleccionat_per_rol != null) {
        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_rol = null }, // Si clica fora, es tanca
            title = { Text("Selecciona el Rol") },
            text = {
                Column {
                    mapRols.forEach { (id, nom) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (usuariSeleccionat_per_rol?.id_rol == id),
                                onClick = {
                                    // Actualitzem l'usuari i tanquem el pop-up
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
            confirmButton = {
                TextButton(onClick = { usuariSeleccionat_per_rol = null }) { Text("Cancel·lar") }
            }
        )
    }

    // --- POP-UP 2: ASSIGNACIÓ DE PLANTES ---
    if (usuariSeleccionat_per_plantes != null) {

        // CRITERI DE NEGOCI: L'API només ens retornarà les plantes que estiguin ACTIVES (ON)
        // Lògica PRO: Un usuari de BD no pot veure plantes desactivades, així que ni tan sols les llistem aquí.
        var plantesMocks by remember {
            mutableStateOf(
                listOf(
                    PlantaAssignacioDto(1, "NOEL 1", true),
                    PlantaAssignacioDto(3, "NOEL 3", false)
                    // La NOEL 2 no surt perquè suposem que està OFF a Gestió de Plantes
                )
            )
        }

        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_plantes = null },
            title = { Text("Plantes Assignades") },
            text = {
                Column {
                    Text(
                        "Només es mostren les plantes actualment ACTIVES.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    plantesMocks.forEach { planta ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = planta.assignada,
                                onCheckedChange = { isChecked ->
                                    plantesMocks = plantesMocks.map {
                                        if (it.id_planta == planta.id_planta) it.copy(assignada = isChecked) else it
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
                    // Aquí enviarem la llista de plantes seleccionades a l'API de C#
                    usuariSeleccionat_per_plantes = null
                }) {
                    Text("GUARDAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { usuariSeleccionat_per_plantes = null }) { Text("Cancel·lar") }
            }
        )
    }
}