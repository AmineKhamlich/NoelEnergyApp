package com.noel.energyapp.ui.alarmes

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import com.noel.energyapp.ui.components.NoelTextField
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.HistoricAlarmaCard
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager

@Composable
fun AlarmesHistoricScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    plantaId: Int,
    onAlarmaClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    var alarmes by remember { mutableStateOf<List<IncidenciaVistaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredAlarmes = alarmes.filter {
        it.tecnicTancament?.contains(searchQuery, ignoreCase = true) == true ||
        it.comptador.contains(searchQuery, ignoreCase = true) ||
        it.descripcioComptador?.contains(searchQuery, ignoreCase = true) == true
    }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getHistoricAlarmes("Bearer $token", plantaId)
            if (response.isSuccessful) {
                alarmes = response.body() ?: emptyList()
            } else {
                Toast.makeText(context, "Error al carregar l'històric", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "HISTÒRIC D'ALARMES",
        verticalArrangement = Arrangement.Top
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                NoelTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Cercar per usuari o comptador...",
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Cercar") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (filteredAlarmes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No s'han trobat alarmes amb aquesta cerca." else "No hi ha cap alarma tancada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredAlarmes) { alarma ->
                            HistoricAlarmaCard(
                                alarma = alarma,
                                onCardClick = { onAlarmaClick(alarma.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
