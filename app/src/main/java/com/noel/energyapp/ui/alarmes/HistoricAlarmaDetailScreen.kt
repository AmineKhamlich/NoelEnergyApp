package com.noel.energyapp.ui.alarmes

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
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
    var isLoading by remember { mutableStateOf(true) }
    var showPhotoDialog by remember { mutableStateOf(false) }

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

    // NOU SISTEMA: Ara la base de dades conté directament Rutes d'arxius, muntem un servidor estàtic
    val imageUrl = remember(alarma?.foto) {
        if (!alarma?.foto.isNullOrBlank()) {
            val nomFoto = alarma!!.foto!!.substringAfterLast("/")
            "http://172.20.1.46/api/imatges/$nomFoto"
        } else {
            null
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
                            Icon(Icons.Default.CheckCircle, null, tint = greenColor, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("TANCADA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = greenColor)
                        }
                        Surface(color = contentColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(a.gravetat, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = contentColor)
                        }
                    }
                }

                DetailSection(title = "Localització") {
                    DetailRow("📍 Ubicació", a.ubicacio)
                    if (!a.descripcioComptador.isNullOrBlank()) DetailRow("⚙️ Comptador", a.descripcioComptador)
                }

                DetailSection(title = "Cronologia") {
                    DetailRow("🔴 Data notificació", a.dataCreacio ?: "—")
                    DetailRow("✅ Data tancament", a.dataTancament ?: "—")
                    if (a.tempsTranscorregut.isNotBlank()) DetailRow("⏱ Durada", a.tempsTranscorregut)
                    if (!a.tecnicTancament.isNullOrBlank()) DetailRow("👤 Tancat per", a.tecnicTancament)
                }

                DetailSection(title = "Consum i Límits") {
                    DetailRow("💧 Consum dia alarma", String.format("%.2f m³", a.consumDiaAlarma))
                    DetailRow("⚠️ Límit H", "${a.limitH ?: "—"} m³")
                    DetailRow("🚨 Límit HH", "${a.limitHH ?: "—"} m³")
                }

                if (!a.descripcio.isNullOrBlank()) {
                    TextSection(title = "Descripció de la incidència", text = a.descripcio!!)
                }

                if (!a.descripcioSolucio.isNullOrBlank()) {
                    TextSection(title = "Solució adoptada", text = a.descripcioSolucio!!)
                }

                if (imageUrl != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Fotografia adjunta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
                            Text("Toca la imatge per veure-la ampliada", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.5f))
                            
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Foto incidència",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clickable { showPhotoDialog = true },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            if (showPhotoDialog && imageUrl != null) {
                ZoomableImageDialog(
                    imageUrl = imageUrl,
                    onDismiss = { showPhotoDialog = false }
                )
            }
        }
    }
}

@Composable
fun ZoomableImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = maxOf(1f, scale * zoom)
                        offset += pan
                    }
                }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Foto ampliada",
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(percent = 50))
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DarkSlate, modifier = Modifier.padding(bottom = 8.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = DarkSlate.copy(alpha=0.1f))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DarkSlate.copy(alpha=0.7f), modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkSlate, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun TextSection(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DarkSlate, modifier = Modifier.padding(bottom = 8.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = DarkSlate.copy(alpha=0.1f))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = DarkSlate)
        }
    }
}
