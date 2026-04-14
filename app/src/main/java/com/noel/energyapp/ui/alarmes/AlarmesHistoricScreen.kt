/**
 * FITXER: AlarmesHistoricScreen.kt
 * CAPA: Interfície d'usuari → Alarmes (ui/alarmes)
 *
 * Aquesta pantalla mostra l'historial d'alarmes ja tancades d'una planta concreta.
 * Permet buscar i filtrar les alarmes pel nom del tècnic que les va tancar,
 * pel nom curt del comptador o per la descripció del comptador.
 *
 * En entrar a la pantalla, es fa una crida a l'API per obtenir totes les alarmes
 * tancades de la planta indicada. Les alarmes es mostren amb el component
 * 'HistoricAlarmaCard' i en tocar-ne una es navega a la pantalla de detall.
 *
 * L'estat de la pantalla pot ser: carregant → llista buida (o sense resultats de cerca) → llista.
 */
package com.noel.energyapp.ui.alarmes

// Importació per mostrar missatges curts tipus Toast
import android.widget.Toast
// Importació dels components bàsics de Jetpack Compose
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// Importació dels models de dades, clients i components de l'App
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.HistoricAlarmaCard
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelTextField
import com.noel.energyapp.util.SessionManager

// Composable de la pantalla de l'historial d'alarmes tancades
@Composable
fun AlarmesHistoricScreen(
    paddingValues: PaddingValues,    // Marges del sistema (barres de navegació)
    onBackClick: () -> Unit,         // Callback per tornar enrere
    plantaId: Int,                   // ID de la planta de la qual es mostra l'historial
    onAlarmaClick: (Int) -> Unit     // Callback que rep l'ID de l'alarma i navega al detall
) {
    // Obté el context d'Android per al SessionManager i els Toasts
    val context = LocalContext.current
    // Instancia el SessionManager per recuperar el token JWT de la sessió local
    val sessionManager = remember { SessionManager(context) }
    // Obté el token JWT per autenticar les peticions a l'API
    val token = sessionManager.fetchAuthToken() ?: ""

    // Estat que emmagatzema totes les alarmes tancades rebudes de l'API
    var alarmes by remember { mutableStateOf<List<IncidenciaVistaDto>>(emptyList()) }
    // Estat booleà que controla la visibilitat de la rodeta de càrrega
    var isLoading by remember { mutableStateOf(true) }
    // Estat que conté el text que l'usuari escriu al camp de cerca
    var searchQuery by remember { mutableStateOf("") }

    // Llista filtrada calculada dinàmicament cada vegada que canvia 'searchQuery' o 'alarmes'
    // No és un estat sinó una derivació: no cal remember perquè es recalcula a cada composició
    val filteredAlarmes = alarmes.filter {
        // Filtra per tècnic de tancament (insensible a majúscules/minúscules)
        it.tecnicTancament?.contains(searchQuery, ignoreCase = true) == true ||
        // O per nom curt del comptador
        it.comptador.contains(searchQuery, ignoreCase = true) ||
        // O per la descripció llegible del comptador
        it.descripcioComptador?.contains(searchQuery, ignoreCase = true) == true
    }

    // Crida a l'API que s'executa una sola vegada en muntar el composable
    LaunchedEffect(Unit) {
        try {
            // Obté l'historial d'alarmes tancades de la planta especificada
            val response = RetrofitClient.instance.getHistoricAlarmes("Bearer $token", plantaId)
            if (response.isSuccessful) {
                // Actualitza la llista completa d'alarmes; llista buida si la resposta ve null
                alarmes = response.body() ?: emptyList()
            } else {
                // Informa d'error si el servidor respon amb codi d'error HTTP
                Toast.makeText(context, "Error al carregar l'històric", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Informa d'error si no hi ha connexió de xarxa
            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
        } finally {
            // Amaga la rodeta de càrrega sempre, hagi anat bé o malament
            isLoading = false
        }
    }

    // Renderitza l'estructura base de la pantalla (capçalera, fons, contenidor)
    NoelScreen(
        paddingValues = paddingValues,
        title = "HISTÒRIC D'ALARMES",          // Títol de la capçalera de la pantalla
        verticalArrangement = Arrangement.Top   // Elements des de dalt
    ) {
        // Si s'estan carregant les dades, mostra la rodeta de progrés centrada
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator() // Rodeta de progrés indeterminada
            }
        } else {
            // Un cop les dades han carregat, mostra la barra de cerca i la llista
            Column(modifier = Modifier.fillMaxSize()) {

                // Camp de text per filtrar les alarmes de la llista
                NoelTextField(
                    value = searchQuery,                          // Text actual del camp de cerca
                    onValueChange = { searchQuery = it },        // Actualitza l'estat en escriure
                    label = "Cercar per usuari o comptador...", // Text de l'etiqueta flotant
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Cercar") }, // Icona de lupa
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (filteredAlarmes.isEmpty()) {
                    // Mostra un missatge diferent si no hi ha resultats de cerca o si la llista és buida
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            // Text condicional: si hi ha filtre actiu, indica que no hi ha resultats
                            text = if (searchQuery.isNotEmpty()) "No s'han trobat alarmes amb aquesta cerca." else "No hi ha cap alarma tancada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    // Llista vertical scrollable amb les alarmes filtrades
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp), // Espai al final de la llista
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Espai de 12dp entre targetes
                    ) {
                        // Per cada alarma filtrada, renderitza una targeta de l'historial
                        items(filteredAlarmes) { alarma ->
                            HistoricAlarmaCard(
                                alarma = alarma,
                                onCardClick = { onAlarmaClick(alarma.id) } // Navega al detall en tocar
                            )
                        }
                    }
                }
            }
        }
    }
}
