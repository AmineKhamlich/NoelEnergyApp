package com.noel.energyapp.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(onLogout: () -> Unit, userName: String?) {
    // Scaffold ens dona l'estructura bàsica de la pantalla (TopBar, contingut, etc.)
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Benvingut/da a Noel Energy,",
                style = MaterialTheme.typography.headlineSmall
            )

            // Mostrem el nom que hem guardat prèviament al SessionManager
            Text(
                text = userName ?: "Usuari",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botó per tancar sessió (ja l'implementarem del tot més endavant)
            Button(onClick = { onLogout() }) {
                Text("Tancar Sessió")
            }
        }
    }
}