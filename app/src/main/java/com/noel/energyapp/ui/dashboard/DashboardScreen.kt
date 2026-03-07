package com.noel.energyapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    onLogout: () -> Unit,
    userName: String?
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Estat per guardar la llista de plantes que ens doni l'API
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // El LaunchedEffect s'executa automàticament en obrir la pantalla
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            try {
                val response = RetrofitClient.instance.getPlantes("Bearer $token")
                if (response.isSuccessful) {
                    plantes = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // FEM SERVIR LA PLANTILLA MESTRE
    // Aprofitem que NoelScreen pot crear la TopBar passant-li un "title"
    // --- PLANTILLA NOEL AMB MENÚ ACTIVAT ---
    NoelScreen(
        paddingValues = paddingValues,
        title = "NOEL ENERGY",
        hasMenu = true, // Això fa aparèixer l'hamburguesa i activa el lateral
        verticalArrangement = Arrangement.Top // Al Dashboard volem que tot vagi cap a dalt
    ) {
        // Salutació alineada a l'esquerra
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text(
                text = "Hola, $userName",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // LazyColumn és com el RecyclerView d'Android, ideal per llistes llargues
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(plantes) { planta ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = planta.nom_planta, modifier = Modifier.weight(1f))
                            // Indicador de si la planta està activa
                            Badge(containerColor = if (planta.activa) Color.Green else Color.Gray) {
                                Text(text = if (planta.activa) "ON" else "OFF")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botó per tancar sessió (Ara utilitzem la plantilla i el fem vermell)
        NoelButton(
            text = "Tancar Sessió",
            onClick = { onLogout() },
            containerColor = MaterialTheme.colorScheme.error // El fem vermell
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}