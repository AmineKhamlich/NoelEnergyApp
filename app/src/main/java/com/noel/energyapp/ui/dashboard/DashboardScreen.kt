package com.noel.energyapp.ui.dashboard

import androidx.compose.foundation.clickable
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

/**
 * Pantalla principal de l'aplicació (Dashboard).
 * Mostra les plantes a les quals l'usuari té accés segons el seu ROL i permisos.
 */
@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    onLogout: () -> Unit,
    onNavigateToGestioPlantes: () -> Unit,
    onNavigateToGestioUsuaris: () -> Unit,
    onPlantaClick: (Int, String) -> Unit,
    userName: String? // Aquest és el nick que ve de la navegació
) {
    // --- 1. CONFIGURACIÓ I GESTIÓ DE SESSIÓ ---
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Estats per la llista de plantes i càrrega
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- 2. RECUPERACIÓ D'IDENTITAT HUMANITZADA ---
    // Intentem agafar el nom real (Nom + Cognom) guardat al telèfon
    val realName = sessionManager.fetchUserRealName()
    val displayGreeting = if (!realName.isNullOrBlank()) {
        realName // Si tenim nom real, l'usem (Ex: "Joan Petit")
    } else {
        userName ?: "Usuari" // Si no, usem el nick (Ex: "admin")
    }

    // Obtenim dades de permís per al filtre
    val userRole = sessionManager.fetchUserRole() ?: ""
    val assignedPlants = sessionManager.fetchAssignedPlants()

    // --- 3. CÀRREGA DE DADES DES DE L'API ---
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            try {
                val response = RetrofitClient.instance.getPlantes("Bearer $token")
                if (response.isSuccessful) {
                    val totesLesPlantes = response.body() ?: emptyList()

                    // FILTRE SEGUR:
                    // 1. Només plantes actives al sistema.
                    // 2. Si ets ADMIN veus tot l'actiu. Si no, només les teves assignades.
                    plantes = totesLesPlantes.filter { planta ->
                        val isActiva = planta.activa
                        val isAssignada = userRole.equals("ADMIN", ignoreCase = true) ||
                                assignedPlants.contains(planta.id_planta)

                        isActiva && isAssignada
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- 4. INTERFÀCIE VISUAL (Basada en NoelScreen) ---
    NoelScreen(
        paddingValues = paddingValues,
        title = "NOEL ENERGY",
        hasMenu = true, // Activem el calaix lateral (Drawer)
        onNavigateToGestioPlantes = onNavigateToGestioPlantes,
        onNavigateToGestioUsuaris = onNavigateToGestioUsuaris,
        verticalArrangement = Arrangement.Top
    ) {
        // SALUTACIÓ: Ara més personal amb el nom real de l'empleat
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text(
                text = "Hola, $displayGreeting",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // GESTIÓ D'ESTATS DE VISTA
        if (isLoading) {
            // Roda de càrrega mentre esperem l'API
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (plantes.isEmpty()) {
            // Cas en que l'usuari no tingui cap permís o no hi hagi res actiu
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No tens cap planta activa assignada.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            // LLISTAT DE PLANTES (LazyColumn per eficiència de memòria)
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(plantes) { planta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onPlantaClick(planta.id_planta, planta.nom_planta) },
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Nom de la planta a l'esquerra
                            Text(
                                text = planta.nom_planta,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            // Indicador visual ON/OFF
                            Badge(
                                containerColor = if (planta.activa) Color(0xFF4CAF50) else Color.Gray
                            ) {
                                Text(
                                    text = if (planta.activa) "ON" else "OFF",
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÓ DE SORTIDA SEGURA
        NoelButton(
            text = "Tancar Sessió",
            onClick = { onLogout() },
            containerColor = MaterialTheme.colorScheme.error // Vermell per indicar acció destructiva/sortida
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}