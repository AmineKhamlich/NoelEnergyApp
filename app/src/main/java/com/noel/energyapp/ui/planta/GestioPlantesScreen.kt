package com.noel.energyapp.ui.planta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.ui.components.NoelScreen

@Composable
fun GestioPlantesScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onNavigateToGestioPlantes: () -> Unit,
    onNavigateToGestioUsuaris: () -> Unit
) {
// Dades falses (MOCK) de moment per veure com queda el disseny
    // A la següent fase això vindrà de l'API de C#
    var plantes by remember {
        mutableStateOf(
            listOf(
                PlantaDto(1, "NOEL 1", true),
                PlantaDto(2, "NOEL 2", false),
                PlantaDto(3, "NOEL 3", true)
            )
        )
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            items(plantes) { planta ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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

                        // L'INTERRUPTOR: Quan l'usuari el toca, canviem l'estat
                        Switch(
                            checked = planta.activa,
                            onCheckedChange = { isChecked ->
                                // Aquí en el futur farem una crida a RetrofitClient
                                // per actualitzar la base de dades

                                // Ara només actualitzem la llista visualment
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
    }
}