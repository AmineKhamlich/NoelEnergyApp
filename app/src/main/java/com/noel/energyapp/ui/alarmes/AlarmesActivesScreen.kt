/**
 * FITXER: AlarmesActivesScreen.kt
 * CAPA: Interfície d'usuari → Alarmes (ui/alarmes)
 *
 * Aquesta pantalla mostra la llista d'alarmes actives (no tancades) de la planta
 * seleccionada. En entrar a la pantalla, es fa una crida a l'API per obtenir les
 * alarmes filtrades per la planta concreta i es mostren en una llista scrollable.
 *
 * Cada alarma es renderitza amb el component 'AlarmaCard', que mostra la gravetat,
 * la ubicació, el temps transcorregut i un botó per navegar a la pantalla de tancament.
 *
 * L'estat de la pantalla pot ser: carregant → llista buida → llista amb dades.
 */
package com.noel.energyapp.ui.alarmes

// Importació per mostrar missatges curts tipus Toast
import android.widget.Toast
// Importació dels components de disseny de Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Importació dels models de dades i clients de xarxa
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.network.RetrofitClient
// Importació dels components reutilitzables de l'App
import com.noel.energyapp.ui.components.AlarmaCard
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
// Importació per formatar dates ISO 8601
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Composable de la pantalla d'alarmes actives, rep l'ID de la planta per filtrar
@Composable
fun AlarmesActivesScreen(
    paddingValues: PaddingValues,            // Marges de la barra inferior i superior del sistema
    onBackClick: () -> Unit,                 // Funció de callback per tornar enrere a PlantaDetail
    plantaId: Int,                           // ID de la planta de la qual es volen veure les alarmes
    onNavigateToTancarAlarma: (Int) -> Unit  // Funció que rep l'ID de l'alarma i navega a la pantalla de tancament
) {
    // Obté el context d'Android necessari per al SessionManager i els Toasts
    val context = LocalContext.current
    // Crea l'àmbit de coroutines per poder fer crides de xarxa de forma asíncrona
    val scope = rememberCoroutineScope()
    // Instancia el SessionManager per obtenir el token JWT de la sessió activa
    val sessionManager = remember { SessionManager(context) }
    // Recupera el token JWT; si no hi ha sessió, usa un string buit
    val token = sessionManager.fetchAuthToken() ?: ""

    // Estat que emmagatzema la llista d'alarmes obtingudes de l'API
    var alarmes by remember { mutableStateOf<List<IncidenciaVistaDto>>(emptyList()) }
    // Estat booleà que controla si s'ha de mostrar la rodeta de càrrega
    var isLoading by remember { mutableStateOf(true) }

    // LaunchedEffect s'executa una sola vegada quan el composable es munta per primera vegada
    // Equivalent a un 'onResume' o 'onViewCreated' en arquitectures tradicionals d'Android
    LaunchedEffect(Unit) {
        try {
            // Crida a l'API per obtenir les alarmes actives de la planta especificada
            val response = RetrofitClient.instance.getAlarmesActives("Bearer $token", plantaId)
            if (response.isSuccessful) {
                // Si l'API respon correctament, actualitza la llista de la UI; llista buida si ve null
                alarmes = response.body() ?: emptyList()
            } else {
                // Si el servidor respon amb error, informa l'usuari amb un Toast
                Toast.makeText(context, "Error carregant alarmes", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Si no hi ha connexió de xarxa o ocorre un error inesperat, mostra un Toast d'error
            Toast.makeText(context, "Error de connexió al servidor", Toast.LENGTH_SHORT).show()
        } finally {
            // Sempre s'amaga la rodeta de càrrega, hagi anat bé o malament
            isLoading = false
        }
    }

    // Renderitza l'estructura base de la pantalla (capçalera, fons, scroll)
    NoelScreen(
        paddingValues = paddingValues,
        title = "ALARMES ACTIVES",            // Títol que apareix a la capçalera de la pantalla
        verticalArrangement = Arrangement.Top  // Els elements comencen des de la part superior
    ) {
        // Si s'estan carregant les dades, mostra la rodeta de progrés centrada a la pantalla
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator() // Rodeta de càrrega indeterminada del sistema Material
            }
        } else if (alarmes.isEmpty()) {
            // Si no hi ha alarmes, mostra un missatge informatiu centrat
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hi ha alarmes actives", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
            }
        } else {
            // Si hi ha alarmes, les mostra en una llista vertical scrollable
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp), // Espai al principi i al final de la llista
                verticalArrangement = Arrangement.spacedBy(12.dp) // Espai de 12dp entre cada targeta
            ) {
                // Per cada alarma de la llista, renderitza una targeta 'AlarmaCard'
                itemsIndexed(alarmes) { index, alarma ->
                    // El botó de gestionar de la targeta navega cap a la pantalla de tancament
                    AlarmaCard(
                        alarma = alarma,
                        animationDelayMillis = (index * 70).coerceAtMost(350),
                        onGestionarClick = { onNavigateToTancarAlarma(alarma.id) }
                    )
                }
            }
        }
    }
}
