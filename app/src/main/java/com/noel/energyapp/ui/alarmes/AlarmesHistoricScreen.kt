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
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.HistoricAlarmaCard
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager

@Composable
fun AlarmesHistoricScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    var alarmes by remember { mutableStateOf<List<IncidenciaVistaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Carreguem l'històric en obrir la pantalla
    LaunchedEffect(Unit) {
        try {
            // Cridem al mètode del servidor que ja tens preparat
            val response = RetrofitClient.instance.getHistoricAlarmes("Bearer $token")
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
        hasMenu = false,
        onBackClick = onBackClick,
        verticalArrangement = Arrangement.Top
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (alarmes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hi ha cap alarma tancada encara. ✨",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarmes) { alarma ->
                    HistoricAlarmaCard(alarma = alarma)
                }
            }
        }
    }
}
