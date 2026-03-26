package com.noel.energyapp.ui.alarmes

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.DarkSlate
import com.noel.energyapp.ui.theme.StatusGreen
import com.noel.energyapp.ui.theme.SurfaceLight
import com.noel.energyapp.util.SessionManager

@Composable
fun HistoricAlarmaDetailScreen(
    paddingValues: PaddingValues,
    alarmaId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    var alarma by remember { mutableStateOf<IncidenciaVistaDto?>(null) }
    var fotaBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    // 1. Carreguem el detall de la incidència trobant-la per ID de la llista
    LaunchedEffect(alarmaId) {
        try {
            val response = RetrofitClient.instance.getHistoricAlarmes("Bearer $token")
            if (response.isSuccessful) {
                alarma = response.body()?.firstOrNull { it.id == alarmaId }
            } else {
                Toast.makeText(context, "Error al carregar el detall", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // 2. Si la alarma té foto guardada al servidor, la demanem per ID
    LaunchedEffect(alarma) {
        val hasFoto = alarma?.foto?.isNotBlank() == true
        if (hasFoto) {
            try {
                val resp = RetrofitClient.instance.getFotoAlarma("Bearer $token", alarmaId)
                if (resp.isSuccessful) {
                    val b64 = resp.body()?.base64
                    if (!b64.isNullOrBlank()) {
                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                        fotaBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                }
            } catch (_: Exception) { /* Si no es pot carregar la foto, no bloquejem */ }
        }
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "DETALL INCIDÈNCIA #$alarmaId",
        verticalArrangement = Arrangement.Top
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (alarma == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No s'ha trobat la incidència.", color = Color.Gray)
            }
        } else {
            val a = alarma!!
            val contentColor = DarkSlate
            val greenColor   = StatusGreen

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── CAPÇALERA: ESTAT + GRAVETAT ───────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = greenColor,
                                modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("TANCADA", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = greenColor)
                        }
                        Surface(color = contentColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)) {
                            Text(a.gravetat, style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = contentColor)
                        }
                    }
                }

                // ── LOCALITZACIÓ ──────────────────────────────────────────
                DetailSection(title = "Localització") {
                    DetailRow("📍 Ubicació", a.ubicacio)
                    if (!a.descripcioComptador.isNullOrBlank())
                        DetailRow("⚙️ Comptador", a.descripcioComptador)
                }

                // ── DATES I DURADA ────────────────────────────────────────
                DetailSection(title = "Cronologia") {
                    DetailRow("🔴 Data notificació", a.dataCreacio ?: "—")
                    DetailRow("✅ Data tancament", a.dataTancament ?: "—")
                    if (a.tempsTranscorregut.isNotBlank())
                        DetailRow("⏱ Durada", a.tempsTranscorregut)
                    if (!a.tecnicTancament.isNullOrBlank())
                        DetailRow("👤 Tancat per", a.tecnicTancament)
                }

                // ── CONSUM I LÍMITS ───────────────────────────────────────
                DetailSection(title = "Consum i Límits") {
                    DetailRow("💧 Consum dia alarma", String.format("%.2f m³", a.consumDiaAlarma))
                    DetailRow("⚠️ Límit H", "${a.limitH ?: "—"} m³")
                    DetailRow("🚨 Límit HH", "${a.limitHH ?: "—"} m³")
                }

                // ── DESCRIPCIÓ DE LA INCIDÈNCIA ───────────────────────────
                if (!a.descripcio.isNullOrBlank()) {
                    TextSection(title = "Descripció de la incidència", text = a.descripcio)
                }

                // ── SOLUCIÓ ADOPTADA ──────────────────────────────────────
                if (!a.descripcioSolucio.isNullOrBlank()) {
                    TextSection(title = "Solució adoptada", text = a.descripcioSolucio)
                }

                // ── FOTOGRAFIA ────────────────────────────────────────────
                if (fotaBitmap != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Fotografia adjunta",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = contentColor)
                            Text("Toca la imatge per veure-la ampliada",
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.5f))
                            Image(
                                bitmap = fotaBitmap!!,
                                contentDescription = "Foto incidència",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clickable { showPhotoDialog = true },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else if (!a.foto.isNullOrBlank()) {
                    // Foto assignada però encara carregant o error
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(2.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center) {
                            Text("Carregant foto...", color = contentColor.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── DIÀLEG FOTO A PANTALLA GRAN ───────────────────────────────
            if (showPhotoDialog && fotaBitmap != null) {
                Dialog(onDismissRequest = { showPhotoDialog = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .clickable { showPhotoDialog = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = fotaBitmap!!,
                            contentDescription = "Foto ampliada",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

// ── Components reutilitzables ─────────────────────────────────────────────────

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val contentColor = DarkSlate
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = contentColor)
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val contentColor = DarkSlate
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold, color = contentColor,
            modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TextSection(title: String, text: String) {
    val contentColor = DarkSlate
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = contentColor)
            HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            Text(text, style = MaterialTheme.typography.bodyMedium,
                color = contentColor, lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.5)
        }
    }
}
