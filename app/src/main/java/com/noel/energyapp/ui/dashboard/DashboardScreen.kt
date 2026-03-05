package com.noel.energyapp.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.util.SessionManager
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit, userName: String?) {
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
                // Afegim "Bearer " davant del token tal com demana .NET
                val response = RetrofitClient.instance.getPlantes("Bearer $token")
                if (response.isSuccessful) {
                    plantes = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Error de xarxa
                 e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }


    // Scaffold ens dona l'estructura bàsica de la pantalla (TopBar, contingut, etc.)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Noel Energy") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Salutació
            Text(
                text = "Hola, $userName",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // LazyColumn és com el RecyclerView d'Android, ideal per llistes llargues
                LazyColumn(modifier = Modifier
                    .weight(1f)      // AQUEST diu: "agafa tot l'espai que sobri a la columna"
                    .fillMaxWidth()  // AQUEST diu: "ocupa tota l'amplada de la pantalla"
                ) {
                    items(plantes) { planta ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
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

            // Botó per tancar sessió (ja l'implementarem del tot més endavant)
            Button(
                onClick = { onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Tancar Sessió")
            }
        }
    }
}