/**
 * FITXER: ConsumGraficaScreen.kt
 * CAPA: Interfície d'usuari → Consums (ui/consums)
 *
 * Aquesta pantalla permet visualitzar el consum diari d'un comptador d'una planta
 * en forma de gràfica de barres per a un interval de dates seleccionat per l'usuari.
 *
 * Flux d'ús:
 * 1. En entrar, es carreguen automàticament els comptadors de la planta.
 * 2. L'usuari selecciona un comptador del desplegable i escull les dates d'inici i fi.
 * 3. En prémer "MOSTRAR GRÀFICA", es fa la crida a l'API i, si hi ha dades,
 *    la pantalla canvia automàticament a mode horitzontal (landscape) per mostrar
 *    la gràfica de barres amb scroll horitzontal.
 * 4. Cada barra representa el consum total d'un dia en m³, amb la data rotada a sota
 *    i el valor exacte escrit damunt de cada barra.
 *
 * La gràfica es dibuixa directament sobre un Canvas de Compose, sense cap biblioteca
 * externa de gràfiques. El canvas calcula les alçades de les barres proporcionalment
 * al valor màxim i permet scroll horitzontal quan hi ha moltes dades.
 *
 * En sortir de la pantalla o en tancar la vista de gràfica, l'orientació es
 * restaura automàticament a la posició normal (vertical/portrait).
 */
package com.noel.energyapp.ui.consums

// Importació d'atributs de recursos (usats per valors per defecte del compilador, innòcuus)
import android.R.attr.enabled
import android.R.attr.fontWeight
// Importació de la classe Activity per controlar l'orientació de la pantalla
import android.app.Activity
// Importació per força la orientació horitzontal en mostrar la gràfica
import android.content.pm.ActivityInfo
// Importació per mostrar missatges curts Toast
import android.widget.Toast
// Importació del Canvas de Compose per dibuixar les barres de la gràfica manualment
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// Importació de les formes geomètriques per al canvas de la gràfica
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Importació de les utilitats per controlar les barres del sistema en mode horitzontal
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import android.content.ContextWrapper
import androidx.compose.ui.input.key.type
import androidx.core.view.WindowInsetsControllerCompat
// Importació dels models de dades i clients de la xarxa
import com.noel.energyapp.data.ConsumFiltratDto
import com.noel.energyapp.data.DimCntDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.*
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
// Importació de les classes Java Time per gestionar i manipular dates
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

/**
 * Funció d'extensió per trobar l'Activity pare d'un Context Android.
 * S'usa per accedir a la finestra i controlar l'orientació de la pantalla.
 * Recorre la cadena de ContextWrapper fins trobar una Activity o retorna null.
 */
fun android.content.Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext // Retorna l'Activity quan la troba
        currentContext = currentContext.baseContext // Puja un nivell en la cadena de wrappers
    }
    return null // Retorna null si no s'ha trobat cap Activity (cas teòricament impossible en una pantalla)
}

// Anotació necessària per a l'ús d'APIs experimentals de Material3 (DatePickerDialog)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumGraficaScreen(
    paddingValues: PaddingValues,  // Marges del sistema Android
    plantaNom: String,             // Nom de la planta de la qual es generen gràfiques
    onBackClick: () -> Unit        // Callback per tornar enrere (usat per la fletxa en mode landscape)
) {
    // Obté el context d'Android necessari per als Toasts, SessionManager i per cercar l'Activity
    val context = LocalContext.current
    // Àmbit de coroutines per a les crides de xarxa asíncrones
    val scope = rememberCoroutineScope()
    // Instancia el SessionManager per accedir al token JWT de la sessió
    val sessionManager = remember { SessionManager(context) }
    // Recupera el token JWT; string buit si no hi ha sessió activa
    val token = sessionManager.fetchAuthToken() ?: ""
    // Determina si el tema actiu és fosc per adaptar els colors de la gràfica
    val isDark = isAppInDarkTheme()

    // Estat booleà que controla si la pantalla ha de mostrar la gràfica en mode horitzontal
    var isLandscapeActive by remember { mutableStateOf(false) }

    // Obté l'Activity pare per poder forçar/restaurar l'orientació de la pantalla
    val activity = context.findActivity()

    // DisposableEffect s'executa cada vegada que 'isLandscapeActive' canvia
    // Gestiona el canvi d'orientació i la visibilitat de les barres del sistema
    DisposableEffect(isLandscapeActive) {
        if (activity != null) {
            val window = activity.window
            // Obté el controlador de les barres del sistema (barra d'estat i navegació)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            if (isLandscapeActive) {
                // Força l'orientació horitzontal per a la vista de la gràfica
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                // Amaga les barres del sistema per a pantalla completa immersiva
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                // Permet que les barres reapareguin temporalment amb un lliscament des del cantó
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Restaura l'orientació per defecte del dispositiu quan es torna al formulari
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                // Mostra de nou les barres del sistema
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            // Quan el composable es destrueix (per navegació), restaura l'orientació i les barres
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val window = activity?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Estat de la llista de comptadors disponibles per a la planta seleccionada
    var comptadors by remember { mutableStateOf<List<DimCntDto>>(emptyList()) }
    // Estat del comptador seleccionat al desplegable (null fins que es carreguen els comptadors)
    var comptadorSeleccionat by remember { mutableStateOf<DimCntDto?>(null) }
    // Estat de les dades de consum obtingudes de l'API per als resultats de la gràfica
    var consumData by remember { mutableStateOf<List<ConsumFiltratDto>>(emptyList()) }
    // Estat booleà que controla si s'estan carregant les dades de la gràfica
    var isLoading by remember { mutableStateOf(false) }

    // Estat booleà que controla si el desplegable de comptadors és obert o tancat
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Estat de la data d'inici del filtre, per defecte els últims 7 dies
    var dataInici by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    // Estat de la data de fi del filtre, per defecte el dia d'avui
    var dataFi by remember { mutableStateOf(LocalDate.now()) }

    // Estat booleà que controla si es mostra el diàleg de selecció de data d'inici
    var showStartPicker by remember { mutableStateOf(false) }
    // Estat booleà que controla si es mostra el diàleg de selecció de data de fi
    var showEndPicker by remember { mutableStateOf(false) }

    // Formater de dates per mostrar les dates en format llegible (ex: "14/04/2026")
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Carrega els comptadors de la planta una sola vegada en muntar el composable
    LaunchedEffect(plantaNom) {
        scope.launch {
            try {
                // Crida a l'API per obtenir tots els comptadors associats a la planta
                val response = RetrofitClient.instance.getComptadorsPerPlanta("Bearer $token", plantaNom)
                if (response.isSuccessful) {
                    comptadors = response.body() ?: emptyList() // Actualitza la llista de comptadors
                    if (comptadors.isNotEmpty()) {
                        // Selecciona automàticament el primer comptador de la llista
                        comptadorSeleccionat = comptadors.first()
                    }
                }
            } catch (e: Exception) {
                // Informa d'error de xarxa si no es poden carregar els comptadors
                Toast.makeText(context, "Error carregant comptadors", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lambda que executa la crida a l'API per carregar les dades de la gràfica
    // S'executa en prémer el botó "MOSTRAR GRÀFICA"
    val carregarGrafica = {
        val meterId = comptadorSeleccionat?.id // ID del comptador seleccionat
        if (meterId != null) {
            if (dataFi.isBefore(dataInici)) {
                // Validació: la data de fi no pot ser anterior a la d'inici
                Toast.makeText(context, "La data fi no pot ser anterior a l'inici", Toast.LENGTH_SHORT).show()
            } else {
                isLoading = true         // Activa la rodeta de càrrega al botó
                consumData = emptyList() // Neteja les dades anteriors de la gràfica
                scope.launch {
                    try {
                        // Formata les dates en format ISO per a la petició a l'API (ex: "2026-04-14")
                        val startStr = dataInici.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val endStr = dataFi.format(DateTimeFormatter.ISO_LOCAL_DATE)

                        // Crida a l'API amb el comptador i el rang de dates seleccionat
                        val response = RetrofitClient.instance.getConsumFiltrat(
                            token = "Bearer $token",
                            idComptador = meterId,
                            start = startStr,
                            end = endStr
                        )

                        if (response.isSuccessful) {
                            val dades = response.body() ?: emptyList()
                            if (dades.isNotEmpty()) {
                                consumData = dades        // Actualitza les dades per a la gràfica
                                isLandscapeActive = true  // Canvia a mode horitzontal per mostrar la gràfica
                            } else {
                                // Informa si el servidor no retorna dades per al rang escollit
                                Toast.makeText(context, "Sense dades per aquestes dates", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Informa del codi d'error HTTP si la petició falla
                            Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Informa d'error de connexió si no hi ha xarxa
                        Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false // Desactiva la rodeta de càrrega sempre
                    }
                }
            }
        } else {
            // Informa l'usuari que ha de seleccionar un comptador primer
            Toast.makeText(context, "Si us plau, escull un comptador", Toast.LENGTH_SHORT).show()
        }
    }

    // Diàleg del càlcul de dates d'inici (MaterialDatePicker de Material3)
    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(
            // Converteix la data LocalDate a milisegons per inicialitzar el selector a la data actual
            initialSelectedDateMillis = dataInici.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false }, // Tanca sense canviar la data
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Converteix els milisegons seleccionats a LocalDate
                        dataInici = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartPicker = false // Tanca el diàleg
                }) { Text("Acceptar") }
            }
        ) { DatePicker(state = datePickerState) } // Renderitza el component de selecció de data
    }

    // Diàleg de selecció de la data de fi
    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dataFi.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataFi = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndPicker = false
                }) { Text("Acceptar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Color del text principal adaptat al tema (blanc en fosc, gris fosc en clar)
    val colorText = if (isDark) Color.White else Color(0xFF333333)
    // Color del text secundari (dates de la gràfica), més subtil que el principal
    val colorSubtext = colorText.copy(alpha = 0.7f)
    // Color de les barres de la gràfica (blau premium del tema)
    val colorBarra = PremiumBlueEnd

    if (isLandscapeActive && consumData.isNotEmpty()) {
        // --- MODE HORITZONTAL: Vista de la gràfica de barres en pantalla completa ---

        // Gradient de fons adaptat al tema actiu
        val bgGradient = if (isDark) {
            Brush.verticalGradient(listOf(DarkBackground, Color.Black))
        } else {
            Brush.verticalGradient(listOf(LightWaterBlue, MaterialTheme.colorScheme.background))
        }

        // Amplada de cada barra de la gràfica en dp
        val barWidth = 48.dp
        // Espai entre barres
        val spacing = 18.dp
        // Amplada total del canvas de la gràfica (perquè el scroll horitzontal funcioni)
        val totalGraphWidth = (barWidth + spacing) * consumData.size + spacing

        // Contenidor principal de la vista de gràfica en mode immersiu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)  // Fons gradient adaptat al tema
                .systemBarsPadding()      // Marge per a les barres del sistema si apareixen
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- CAPÇALERA DE LA VISTA HORITZONTAL ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botó "Enrere" per sortir del mode horitzontal i tornar al formulari
                    IconButton(onClick = { isLandscapeActive = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Enrere", tint = colorText)
                    }
                    // Nom del comptador seleccionat o nom de la planta com a fallback
                    Text(
                        text = comptadorSeleccionat?.descripcio ?: plantaNom,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorText
                    )
                    // Espaci flexible que empeny el total cap a la dreta
                    Spacer(modifier = Modifier.weight(1f))
                    // Consum total del període seleccionat, calculat sumant tots els valors diaris
                    Text(
                        text = "TOTAL: ${String.format("%.2f", consumData.sumOf { it.consum })} m³",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorText
                    )
                }

                // --- CONTENIDOR DE LA GRÀFICA AMB SCROLL HORITZONTAL ---
                val graphScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)                         // Ocupa tot l'espai disponible de la columna
                        .horizontalScroll(graphScrollState) // Activa el scroll horitzontal
                ) {
                    // Canvas de Compose per dibuixar les barres de la gràfica directament en píxels
                    Canvas(
                        modifier = Modifier
                            .requiredWidth(totalGraphWidth) // Amplada real que activa el scroll horitzontal
                            .fillMaxHeight()
                            .padding(bottom = 20.dp)        // Marge inferior per no tocar el final
                    ) {
                        // Marge superior per deixar espai als valors sobre les barres
                        val topMargin = 40.dp.toPx()
                        // Marge inferior per les dates rotades sota les barres
                        val bottomMargin = 80.dp.toPx()
                        // Alçada útil del canvas on es dibuixen les barres
                        val canvasHeight = size.height - topMargin - bottomMargin

                        // Converteix els dp de barra i espai a píxels físics del dispositiu
                        val barPx = barWidth.toPx()
                        val spacePx = spacing.toPx()
                        // Valor màxim de consum per normalitzar les alçades de les barres
                        val maxConsum = consumData.maxOfOrNull { it.consum }?.toFloat() ?: 1f

                        // Pintura del text de les dates (sota les barres, més petita)
                        val textPaintDates = android.graphics.Paint().apply {
                            color = colorSubtext.toArgb() // Color del text de les dates
                            textSize = 24f                 // Mida del text en píxels de canvas
                            isAntiAlias = true             // Activa l'anti-aliasing per a text net
                        }

                        // Pintura del text dels valors m³ (sobre les barres, en negreta)
                        val textPaintValue = android.graphics.Paint().apply {
                            color = colorText.toArgb()     // Color del text dels valors
                            textSize = 28f                  // Mida del text en píxels de canvas
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD // Negreta
                        }

                        // Per cada punt de dades (dia), dibuixa la seva barra
                        consumData.forEachIndexed { index, data ->
                            // Converteix el consum a Float per a les operacions amb el canvas
                            val consumFloat = data.consum.toFloat()
                            // Calcula l'alçada de la barra proporcional al màxim; mínim de 4px per visibilitat
                            val barHeight = if (maxConsum > 0) (consumFloat / maxConsum) * canvasHeight else 0f

                            // Posició X de l'esquerra de la barra (spai + índex * (barra + espai))
                            val xPosition = spacePx + index * (barPx + spacePx)
                            // Posició Y de la base de la gràfica (marge superior + alçada útil)
                            val yBase = topMargin + canvasHeight
                            // Posició Y del cim de la barra (base - alçada de la barra)
                            val yTop = yBase - barHeight

                            // Dibuixa la barra amb cantonades arrodonides en el color del tema
                            drawRoundRect(
                                color = colorBarra,
                                topLeft = Offset(xPosition, yTop),             // Cantonada superior esquerra
                                size = Size(barPx, barHeight.coerceAtLeast(4f)), // Mínim 4px d'alçada visible
                                cornerRadius = CornerRadius(12f, 12f)           // Radi de les cantonades
                            )

                            // Dibuixa el valor m³ centrat sobre la barra
                            drawContext.canvas.nativeCanvas.apply {
                                val valueStr = String.format("%.2f", consumFloat) // Formata amb 2 decimals
                                textPaintValue.textAlign = android.graphics.Paint.Align.CENTER
                                drawText(valueStr, xPosition + barPx / 2, yTop - 15f, textPaintValue)
                            }

                            // Dibuixa la data rotada -90° sota la barra per estalviar espai
                            drawContext.canvas.nativeCanvas.apply {
                                save()                                  // Guarda l'estat de transformació
                                val dateX = xPosition + barPx / 2      // Posició X centrada a la barra
                                val dateY = yBase + 25f                 // Posició Y just sota la barra
                                rotate(-90f, dateX, dateY)              // Rota el text 90° en sentit antihorari
                                textPaintDates.textAlign = android.graphics.Paint.Align.RIGHT
                                drawText(data.data.take(10), dateX, dateY, textPaintDates) // Mostra YYYY-MM-DD
                                restore()                               // Restaura l'estat de transformació
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- MODE VERTICAL: Formulari de selecció de comptador, dates i botó de generar gràfica ---
        NoelScreen(
            paddingValues = paddingValues,
            title = "CONSUM M³",               // Títol de la capçalera de la pantalla
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Targeta que agrupa tots els controls del formulari de filtre
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        // --- SELECTOR DE COMPTADOR (DESPLEGABLE) ---
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = comptadorSeleccionat?.descripcio ?: "Comptador...", // Text del comptador seleccionat
                                onValueChange = {},      // Camp de lectura (solo lectura)
                                readOnly = true,
                                label = { Text("Selecciona un Comptador") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable) // Àncora del desplegable
                                    .fillMaxWidth()
                            )
                            // Menú desplegable amb tots els comptadors de la planta
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                comptadors.forEach { comptador ->
                                    DropdownMenuItem(
                                        text = { Text(comptador.descripcio ?: "") }, // Descripció del comptador
                                        onClick = {
                                            comptadorSeleccionat = comptador // Selecciona el comptador tocat
                                            isDropdownExpanded = false       // Tanca el desplegable
                                        }
                                    )
                                }
                            }
                        }

                        // --- SELECTOR DE DATES (INICI I FI EN FILA) ---
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Targeta de selecció de la data d'inici (obre el DatePicker en tocar)
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { showStartPicker = true }) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Inici", style = MaterialTheme.typography.labelSmall)
                                    Text(dataInici.format(formatter), fontWeight = FontWeight.Bold)
                                }
                            }
                            // Targeta de selecció de la data de fi (obre el DatePicker en tocar)
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { showEndPicker = true }) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Fi", style = MaterialTheme.typography.labelSmall)
                                    Text(dataFi.format(formatter), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Botó que executa la crida a l'API i mostra la gràfica si hi ha dades
                        NoelButton(
                            text = if (isLoading) "CARREGANT..." else "MOSTRAR GRÀFICA",
                            isLoading = isLoading,
                            onClick = { carregarGrafica() } // Crida la lambda de carga de gràfica
                        )
                    }
                }

                // Espai al final per evitar que el contingut quedi tapat per la barra de navegació
                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
}