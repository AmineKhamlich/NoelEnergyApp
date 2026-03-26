package com.noel.energyapp.ui.alarmes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.AlarmaCard
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AlarmesActivesScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    plantaId: Int,
    onNavigateToTancarAlarma: (Int) -> Unit // Passem l'ID de l'alarma a la pantalla de tancar
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    var alarmes by remember { mutableStateOf<List<IncidenciaVistaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Carreguem les dades només obrir la pantalla
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getAlarmesActives("Bearer $token", plantaId)
            if (response.isSuccessful) {
                alarmes = response.body() ?: emptyList()
            } else {
                Toast.makeText(context, "Error carregant alarmes", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de connexió al servidor", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "ALARMES ACTIVES",
        verticalArrangement = Arrangement.Top
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (alarmes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hi ha alarmes actives", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarmes) { alarma ->
                    AlarmaCard(alarma = alarma, onGestionarClick = { onNavigateToTancarAlarma(alarma.id) })
                }
            }
        }
    }
}