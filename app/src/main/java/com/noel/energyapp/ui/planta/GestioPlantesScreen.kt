/**
 * FITXER: GestioPlantesScreen.kt
 * CAPA: Interfície d'usuari → Planta (ui/planta)
 *
 * Aquesta pantalla és una eina d'administració que permet activar o desactivar
 * les plantes del sistema. Les plantes desactivades no seran visibles al Dashboard
 * ni pels usuaris ni pels administradors, malgrat estiguin configurades a la BD.
 *
 * Funcionalitats:
 * 1. Obté totes les plantes registrades (actives o no) des de l'API.
 * 2. Visualitza el llistat sencer, mostrant el nom i un 'Switch' (botó alternador) d'estat.
 * 3. En modificar els Switchs, l'estat local es manipula, però els canvis no es
 *    persisteixen al backend fins a fer clic explícitament a "Guardar Canvis".
 * 4. El mètode per desar construeix un DTO només amb els IDs de les plantes amb
 *    el switch en ON i el tramet a l'API. (L'actuació per l'API posarà tota planta no inclosa en aquest array en estat inactiu).
 */
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
import androidx.compose.foundation.lazy.itemsIndexed
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
// DTOs pertanyents a l'arquitectura Rest pel tracte de les Plantes
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.data.UpdatePlantesActivesDto
// Client de Retrofit per consum HTTP
import com.noel.energyapp.network.RetrofitClient
// Elements visuals d'abstracció d'estils uniformes de l'appName
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.noelReveal
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun GestioPlantesScreen(
    paddingValues: PaddingValues, // Sistemes d'espaiat inferiors i superiors per evitar en cavalcament
    onBackClick: () -> Unit       // Callback per moure's a la pantalla anterior (gestor de NavHost)
) {
    // Definició i vinculació del context, Scope i Local Store necessari per extreure les credencials
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    // --- Estat de la pantalla ---
    // Llistat mutable que rep totes les plantes via resposta de backend
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    
    // Indicador global de dades carregant (per no mostrar llistes invàlides momentaneament viudes d'internet)
    var isLoading by remember { mutableStateOf(true) } // Rodeta inical

    // Indicador especial pel submit on no volem múltiples peticions solapades per impaciència (Disable del Guardar)
    var isSaving by remember { mutableStateOf(false) } // Rodeta al guardar

    // Descarregar plantes de l'API al carregar la pantalla - Launch efect amb clau buida només salta la primera comp.
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            try {
                // Utilitzem la crida GET que ja teníem per al Dashboard que ens retorna array amb la prop d'activa a backend
                val response = RetrofitClient.instance.getPlantes("Bearer $token")
                if (response.isSuccessful) {
                    plantes = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al carregar les plantes", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                // Prevenim dany si no tenim senyal per WiFi interna a l'App no crashejant-se i sol mostrem
                Toast.makeText(context, "Error de connexió al servidor", Toast.LENGTH_SHORT).show()
            } finally {
                // Apaguem independent del fail / catch d'èxit del HTTP code l'spinner general
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }


    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ PLANTES",
        verticalArrangement = Arrangement.Top
    ) {
        
        // Text exlicatiu referent a la pantalla d'Administrador actual
        Text(
            text = "Activa o desactiva les plantes. Les plantes apagades no apareixeran al Dashboard de cap usuari.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Si estem carregant dades de l'API, mostrem el progrés centrat
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f) // Ens asseverem d'omplir el tros disponible per portarlo d'Alignment.Center
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        // Condició per Backend respost un array [] o List buida per taula BBDD absent. Evita carregar les files i cards blanques.
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
            // Llista dinàmica Compose a mode 'ListView/RecyclerView' per les plantes, consumint l'scroll només si fora viewHeight
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(plantes) { index, planta ->
                    // Carta independent per a cada item de llistat
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noelReveal(delayMillis = (index * 50).coerceAtMost(300))
                            .padding(vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically, // Els posicions igual de la meitat en cross axis
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Text principal on el seu weight de la row és superior a la resta (1f contra null de la resta pel Switch)
                            Text(
                                text = planta.nom_planta,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Botó alternador d'estat (ON/OFF - true/false)
                            Switch(
                                checked = planta.activa,
                                onCheckedChange = { isChecked ->
                                    // Actualitzem l'estat localment al mòbil clonantejant object amb el .copy DataClass (Kotlin Feature)
                                    // evitant referències invàlides sense disparant recombinació a la view UI
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

            // --- LÒGICA REAL: Enviar l'actualització massiva a l'API ---
            // Fa un put massiu només agafant les activades del context local i traspassant aquesta info. a C# Backend.
            NoelButton(
                text = "Guardar Canvis",
                isLoading = isSaving, // Quan estigui executant el submit es torna ProgressSpinner dins ell mateix
                onClick = {
                    scope.launch {
                        isSaving = true
                        try {
                            val token = sessionManager.fetchAuthToken() ?: ""

                            // 1. Filtrem les plantes que han quedat en ON del array guardat general UI localment
                            val plantesActives = plantes.filter { it.activa }

                            // 2. N'extraiem només els IDs (Int Array) tal cual el Model C# UpdatePlantaActiva l'espera per mapejar gson
                            val idsActius = plantesActives.map { it.id_planta }

                            // 3. Muntem l'objecte DataTransferObject (Data class Model) Kotlin previ l'enviament. (Body API)
                            val request = UpdatePlantesActivesDto(idsActius)

                            // 4. Disparem el Post/Put a l'API (Suspend function corrutine no bloquejant l'usuari interàcies
                            val response =
                                RetrofitClient.instance.updateEstatMassiu("Bearer $token", request)

                            if (response.isSuccessful) {
                                // Avis al usuari conforme ok real a backend!
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
                            // Deu deixar el switch general isSaving un cop més false evitant que el loading no aturi mai pel Catch
                            isSaving = false
                        }
                    }
                }
            )

            // Empenyent element bottom de navegacion inferior (no tapars el butó quan els elements son molts)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
