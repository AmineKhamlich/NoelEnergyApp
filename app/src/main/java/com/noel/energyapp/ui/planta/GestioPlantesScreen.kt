package com.noel.energyapp.ui.planta

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.data.UpdatePlantesActivesDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun GestioPlantesScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToGestioPlantes: () -> Unit,
    onNavigateToGestioUsuaris: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    // Estat de la pantalla
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) } // Rodeta inical
    var isSaving by remember { mutableStateOf(false) } // Rodeta al guardar

    // Descarregar plantes de l'API al carregar la pantalla
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            try {
                // Utilitzem la crida GET que ja teníem per al Dashboard
                val response = RetrofitClient.instance.getPlantes("Bearer $token")
                if (response.isSuccessful) {
                    plantes = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al carregar les plantes", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de connexió al servidor", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }


    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ PLANTES",
        hasMenu = true,
        onBackClick = onBackClick,
        onNavigateToGestioPlantes = onNavigateToGestioPlantes,
        onNavigateToGestioUsuaris = onNavigateToGestioUsuaris,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Activa o desactiva les plantes. Les plantes apagades no apareixeran al Dashboard de cap usuari.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Si estem carregant dades de l'API, mostrem el progrés
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (plantes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hi ha cap planta a la base de dades.")
            }
        } else {
            // Llista de plantes descarregades
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(plantes) { planta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = planta.nom_planta,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Switch(
                                checked = planta.activa,
                                onCheckedChange = { isChecked ->
                                    // Actualitzem l'estat localment al mòbil
                                    plantes = plantes.map {
                                        if (it.id_planta == planta.id_planta) it.copy(activa = isChecked)
                                        else it
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LÒGICA REAL: Enviar l'actualització massiva a l'API
            NoelButton(
                text = "Guardar Canvis",
                isLoading = isSaving,
                onClick = {
                    scope.launch {
                        isSaving = true
                        try {
                            val token = sessionManager.fetchAuthToken() ?: ""

                            // 1. Filtrem les plantes que han quedat en ON
                            val plantesActives = plantes.filter { it.activa }

                            // 2. N'extraiem només els IDs com demana C#
                            val idsActius = plantesActives.map { it.id_planta }

                            // 3. Muntem el DTO
                            val request = UpdatePlantesActivesDto(idsActius)

                            // 4. Disparem a l'API
                            val response =
                                RetrofitClient.instance.updateEstatMassiu("Bearer $token", request)

                            if (response.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Plantes actualitzades correctament!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "No s'han pogut guardar els canvis",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error de connexió al servidor",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            isSaving = false
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}