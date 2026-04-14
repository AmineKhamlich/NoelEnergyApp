/**
 * FITXER: HistoricAlarmaDetailScreen.kt
 * CAPA: Interfície d'usuari → Alarmes (ui/alarmes)
 *
 * Aquesta pantalla mostra informació detallada d'una incidència resolta o tancada.
 * S'hi arriba des del llistat històric i està protegida de qualsevol modificació per l'usuari.
 * 
 * Funcionalitats clau:
 * 1. Extracció directa de l'objecte "Alarma" des del Backend segons `alarmaId`.
 * 2. Exposició a través de Targetes/Cards de contingut divers (Localització, Cronologia i Limits de SCADA).
 * 3. Mostreig d'informació textual addicional sobre les descripcions de les causes i de les solucions aportades pel Tècnic.
 * 4. Recuperació des de ruta host via Glide/Coil de fotografies vinculades, disposades a fer Zoom multi-touch llançant un View amplificat (Dialog interactiu customitzat amb Gestos Zoom i Pan).
 */
package com.noel.energyapp.ui.alarmes

// Eines de presentadors per emergents Toast, text i columnat ui components basic framework native ui rendering
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
// Material Icons tancat exitos i tance window close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Control zoom gesture x/y position offset handling zoom pan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
// Transform matrix and layer options modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Finestres popup system base custom dialogs
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
// Llibreria per dibuixar Imatges per URL remote server direct draw lazy caching 
import coil.compose.AsyncImage
// Model llistat incidencia
import com.noel.energyapp.data.IncidenciaVistaDto
import com.noel.energyapp.network.RetrofitClient
// Components gràfics propis customitzats base theme estandaritzats d'UI
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.DarkSlate
import com.noel.energyapp.ui.theme.StatusGreen
import com.noel.energyapp.ui.theme.SurfaceLight
import com.noel.energyapp.util.SessionManager

@Composable
fun HistoricAlarmaDetailScreen(
    paddingValues: PaddingValues, // Sistemes d'espaiat inferiors i superiors propis d'Android Navbar topbar bounds layout rules.
    alarmaId: Int,                // ID Únic base dades (idIncidencia) a cercar details view .
    onBackClick: () -> Unit       // Sortida (Callback parent handler rule back step).
) {
    // Declaració context local app resources session prefs cache.
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    // Estat tipus dada de Backend (Nullable al inici fins a rebre-hi mapping complet del JSON o 400).
    var alarma by remember { mutableStateOf<IncidenciaVistaDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    // Boleà d'activació i amagat Popup Imatge Detail
    var showPhotoDialog by remember { mutableStateOf(false) }

    // Llança petició a "Unit/id" change state parameter load execution context.
    LaunchedEffect(alarmaId) {
        try {
            // Tot i agafar Totes les del llistat, filtrem local la que coincideix per el id de param
            val response = RetrofitClient.instance.getHistoricAlarmes("Bearer $token")
            if (response.isSuccessful) {
                // Busquem de la collection (Cos body Json convert list object Dtos) qui te match d'ids param 
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

    // --- NOU SISTEMA URL MUNTATGE ESTATUS SERVIDOR ---
    // Ara la base de dades conté directament Rutes d'arxius ("E:\\Web\\Imatges\\nom_foto.jpg"), 
    // l'API ha creat sub path estàtic pel hosting file web "imatges/" del C#.
    val imageUrl = remember(alarma?.foto) {
        if (!alarma?.foto.isNullOrBlank()) {
            // Traiem exclusivament despres l'última barra "\" (només nom de foto extension)
            val nomFoto = alarma!!.foto!!.substringAfterLast("/")
            // Substituïm el base path absolut servidor backend per domini accés Web Api direct serve access files directory config route setup backend program static definition path rules config server network location paths.
            "http://172.20.1.46/api/imatges/$nomFoto"
        } else {
            null
        }
    }

    NoelScreen(
        paddingValues = paddingValues,
        title = "DETALL INCIDÈNCIA #$alarmaId", // Dynamic reference string to Title.
        verticalArrangement = Arrangement.Top
    ) {
        // En curs de demana Internet
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        // Després de l'apagat isLoading, cas not trobat null filter mapping results
        } else if (alarma == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No s'ha trobat la incidència.", color = Color.Gray)
            }
        // Cas OK: Objecte present en scope local variables block memory
        } else {
            // Alarma casted a NoNull reference memory pointer object 
            val a = alarma!!
            val contentColor = DarkSlate
            val greenColor   = StatusGreen

            // Element Llista de detalls scroll vertical sencer body pad top offset limit margins style modifier default configuration list .
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 1. ENCAPÇAMENT ESTAT CERRAT ---
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
                        // Coloració fixa verd Status success. 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = greenColor, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("TANCADA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = greenColor)
                        }
                        // Gravetat històric de la fallada format fons clar contrast alfa transparency.
                        Surface(color = contentColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(a.gravetat, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = contentColor)
                        }
                    }
                }

                // --- 2. UBICATÒRIA ---
                DetailSection(title = "Localització") {
                    // Cridem helper local UI per printar keys - values lineals
                    DetailRow("📍 Ubicació", a.ubicacio)
                    if (!a.descripcioComptador.isNullOrBlank()) DetailRow("⚙️ Comptador", a.descripcioComptador)
                }

                // --- 3. TEMPORALS ---
                DetailSection(title = "Cronologia") {
                    DetailRow("🔴 Data notificació", a.dataCreacio ?: "—")
                    DetailRow("✅ Data tancament", a.dataTancament ?: "—")
                    if (a.tempsTranscorregut.isNotBlank()) DetailRow("⏱ Durada", a.tempsTranscorregut)
                    if (!a.tecnicTancament.isNullOrBlank()) DetailRow("👤 Tancat per", a.tecnicTancament)
                }

                // --- 4. DETALL SCADA DATA LIMITS PRE INCIDENTS AL MOMENT TALL (Snapshot value history table) ---
                DetailSection(title = "Consum i Límits") {
                    DetailRow("💧 Consum dia alarma", String.format("%.2f m³", a.consumDiaAlarma))
                    DetailRow("⚠️ Límit H", "${a.limitH ?: "—"} m³")
                    DetailRow("🚨 Límit HH", "${a.limitHH ?: "—"} m³")
                }

                // --- 5. CAMP TEXT OBERT 1: QUE PASSAVA ---
                if (!a.descripcio.isNullOrBlank()) {
                    TextSection(title = "Descripció de la incidència", text = a.descripcio!!)
                }

                // --- 6. CAMP TEXT OBERT 2: SOLUCIONS REPORTADAS PEL USER QUE TANCA ---
                if (!a.descripcioSolucio.isNullOrBlank()) {
                    TextSection(title = "Solució adoptada", text = a.descripcioSolucio!!)
                }

                // --- 7. CARTELL FOTOGRAFIA INCORPORADA COIL ASYNCHRONIC DIRECT REPEATING CACHE REPOSITORY METHOD RENDER IMAGE LAZY REQUEST LOADER ---
                if (imageUrl != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Fotografia adjunta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
                            Text("Toca la imatge per veure-la ampliada", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.5f))
                            
                            // Llibreria especial Android d'imatges net calls Coil.
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Foto incidència",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clickable { showPhotoDialog = true }, // Set true local State -> Llançara view overlay
                                contentScale = ContentScale.Crop // Tall ajusta l'aspect layout filling shape no size empty spacing
                            )
                        }
                    }
                }
            }

            // PopUp o "Dialog" Android Base Layer overlay drawing
            if (showPhotoDialog && imageUrl != null) {
                ZoomableImageDialog(
                    imageUrl = imageUrl,
                    onDismiss = { showPhotoDialog = false } // Apagat flag variable boolean binding update callback function event 
                )
            }
        }
    }
}

/**
 * COMPONENT EXTRA: Diàleg d'imatge enfocada de manera Zoomable Gestual.
 */
@Composable
fun ZoomableImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss, // Accepta "Enrera" a botó maquinari o pulsació buida per apagar
        // Fà tot d'ample obviant els margins defectes i permetent que click back escsp
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        // Escala Base (sense ampliacio x1f) i Eix (Coordenades cartesianes d'enllaç per manteniment scroll dits base X, Y values params memory logic save save offset geometry types limits variables assignment property memory handling boolean properties state checking modifiers default property)
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        // Recuardi pantalla completa full size overlay UI layout layer background property definition
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Ennuvolament del context
                .pointerInput(Unit) { // Tracker u control dits multitouch screen handling pointers touch actions listener definitions system calls wrapper layout property assignment modifier pointer structure params 
                    // Gestor Zoom amb 'detectTransformGestures' 
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Fixem maxOf cap abaix per no ficar petit el frame més de original size
                        scale = maxOf(1f, scale * zoom)
                        // Anem afegint moviments lliscats per canviar offset position map UI modifier rule
                        offset += pan
                    }
                }
        ) {
            // Imatge redibuixada sota scale i offset GraphicLayer modifiers translation attributes bindings update 
            AsyncImage(
                model = imageUrl, // Tornar a carregar pero treu del caché Coil memory disc system cache.
                contentDescription = "Foto ampliada",
                modifier = Modifier
                    .matchParentSize() // Omple Box
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit // Mantingues la relació originaria aspect photo layout structure rule definition mapping rules definition mapping UI design patterns modifiers methods structure
            )
            // Buto Creueta tanca
            IconButton(
                onClick = onDismiss,
                // Alineació cantonada superior esquerra (Top End x/y modifier values bounds handling property checking type params default)
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(percent = 50))
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// FUNCIONS AJUDANTS VISUALS PER DETALL (HELPER COMPOSABLES - NO REUTLIZATS A FORA):
// -----------------------------------------------------------------------------------------

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Capçalera seccionador Title Label Form Component Logic Type layout rule layout definitions properties property parameters values properties configuration style types checks limits definitions.
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DarkSlate, modifier = Modifier.padding(bottom = 8.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = DarkSlate.copy(alpha=0.1f))
            // Contingut afegit inferior body slot definition
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        // D'esquerra a dreta format lliurador dades de text amb weight equilibrats d'espais centrals limits checks property property type assignment modifier parameters.
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
            // Similar seccionador pre configurat label style 
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DarkSlate, modifier = Modifier.padding(bottom = 8.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = DarkSlate.copy(alpha=0.1f))
            // Contingut String com a body Text form structure check rules values property parameters text structure.
            Text(text, style = MaterialTheme.typography.bodyMedium, color = DarkSlate)
        }
    }
}
