package com.noel.energyapp.ui.consums

import android.R.attr.enabled
import android.R.attr.fontWeight
import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import android.content.ContextWrapper
import androidx.compose.ui.input.key.type
import androidx.core.view.WindowInsetsControllerCompat

import com.noel.energyapp.data.ConsumFiltratDto // CORREGIT EL TYPO AQUÍ
import com.noel.energyapp.data.DimCntDto
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.components.NoelButton
import com.noel.energyapp.ui.theme.*
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

fun android.content.Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumGraficaScreen(
    paddingValues: PaddingValues,
    plantaNom: String,
    onBackClick: () -> Unit // Ho mantenim per a la fletxa de dalt a l'esquerra de la gràfica horitzontal
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""
    val isDark = isAppInDarkTheme()

    var isLandscapeActive by remember { mutableStateOf(false) }

    val activity = context.findActivity()
    DisposableEffect(isLandscapeActive) {
        if (activity != null) {
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)

            if (isLandscapeActive) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val window = activity?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    var comptadors by remember { mutableStateOf<List<DimCntDto>>(emptyList()) }
    var comptadorSeleccionat by remember { mutableStateOf<DimCntDto?>(null) }

    // CORREGIT EL TYPO AQUÍ. Ara l'Android Studio sabrà quin tipus de dades són.
    var consumData by remember { mutableStateOf<List<ConsumFiltratDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    var dataInici by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var dataFi by remember { mutableStateOf(LocalDate.now()) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    LaunchedEffect(plantaNom) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getComptadorsPerPlanta("Bearer $token", plantaNom)
                if (response.isSuccessful) {
                    comptadors = response.body() ?: emptyList()
                    if (comptadors.isNotEmpty()) {
                        comptadorSeleccionat = comptadors.first()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error carregant comptadors", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val carregarGrafica = {
        val meterId = comptadorSeleccionat?.id
        if (meterId != null) {
            if (dataFi.isBefore(dataInici)) {
                Toast.makeText(context, "La data fi no pot ser anterior a l'inici", Toast.LENGTH_SHORT).show()
            } else {
                isLoading = true
                consumData = emptyList()
                scope.launch {
                    try {
                        val startStr = dataInici.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val endStr = dataFi.format(DateTimeFormatter.ISO_LOCAL_DATE)

                        val response = RetrofitClient.instance.getConsumFiltrat(
                            token = "Bearer $token",
                            idComptador = meterId,
                            start = startStr,
                            end = endStr
                        )

                        if (response.isSuccessful) {
                            val dades = response.body() ?: emptyList()
                            if (dades.isNotEmpty()) {
                                consumData = dades
                                isLandscapeActive = true
                            } else {
                                Toast.makeText(context, "Sense dades per aquestes dates", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            }
        } else {
            Toast.makeText(context, "Si us plau, escull un comptador", Toast.LENGTH_SHORT).show()
        }
    }

    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dataInici.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        dataInici = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartPicker = false
                }) { Text("Acceptar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

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

    val colorText = if (isDark) Color.White else Color(0xFF333333)
    val colorSubtext = colorText.copy(alpha = 0.7f)
    val colorBarra = PremiumBlueEnd
    if (isLandscapeActive && consumData.isNotEmpty()) {
        val bgGradient = if (isDark) {
            Brush.verticalGradient(listOf(DarkBackground, Color.Black))
        } else {
            Brush.verticalGradient(listOf(LightWaterBlue, MaterialTheme.colorScheme.background))
        }

        // 1. Càlcul de mides per garantir llegibilitat i scroll
        val barWidth = 48.dp
        val spacing = 18.dp
        val totalGraphWidth = (barWidth + spacing) * consumData.size + spacing

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .systemBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // --- Capçalera ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isLandscapeActive = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Enrere", tint = colorText)
                    }
                    Text(
                        text = comptadorSeleccionat?.descripcio ?: plantaNom,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorText
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "TOTAL: ${String.format("%.2f", consumData.sumOf { it.consum })} m³",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorText
                    )
                }

                // --- Contenidor de la Gràfica amb Scroll ---
                val graphScrollState = rememberScrollState()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .horizontalScroll(graphScrollState) // L'scroll actua sobre el Box
                ) {
                    Canvas(
                        modifier = Modifier
                            .requiredWidth(totalGraphWidth) // FORÇEM l'amplada real per activar scroll
                            .fillMaxHeight()
                            .padding(bottom = 20.dp) // Espai inferior per no tocar el final
                    ) {
                        // Marge de seguretat: 40dp a dalt pels números i 80dp a baix per les dates
                        val topMargin = 40.dp.toPx()
                        val bottomMargin = 80.dp.toPx()
                        val canvasHeight = size.height - topMargin - bottomMargin
                        
                        val barPx = barWidth.toPx()
                        val spacePx = spacing.toPx()
                        val maxConsum = consumData.maxOfOrNull { it.consum }?.toFloat() ?: 1f

                        val textPaintDates = android.graphics.Paint().apply {
                            color = colorSubtext.toArgb()
                            textSize = 24f
                            isAntiAlias = true
                        }
                        val textPaintValue = android.graphics.Paint().apply {
                            color = colorText.toArgb()
                            textSize = 28f
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }

                        consumData.forEachIndexed { index, data ->
                            val consumFloat = data.consum.toFloat()
                            val barHeight = if (maxConsum > 0) (consumFloat / maxConsum) * canvasHeight else 0f
                            
                            val xPosition = spacePx + index * (barPx + spacePx)
                            val yBase = topMargin + canvasHeight // Base de la gràfica
                            val yTop = yBase - barHeight         // Cim de la barra

                            // Dibuix de la barra
                            drawRoundRect(
                                color = colorBarra,
                                topLeft = Offset(xPosition, yTop),
                                size = Size(barPx, barHeight.coerceAtLeast(4f)),
                                cornerRadius = CornerRadius(12f, 12f)
                            )

                            // Valor m³ (Dalt de la barra)
                            drawContext.canvas.nativeCanvas.apply {
                                val valueStr = String.format("%.2f", consumFloat)
                                textPaintValue.textAlign = android.graphics.Paint.Align.CENTER
                                drawText(valueStr, xPosition + barPx/2, yTop - 15f, textPaintValue)
                            }

                            // Data (Sota la barra, rotada)
                            drawContext.canvas.nativeCanvas.apply {
                                save()
                                val dateX = xPosition + barPx/2
                                val dateY = yBase + 25f
                                rotate(-90f, dateX, dateY)
                                textPaintDates.textAlign = android.graphics.Paint.Align.RIGHT
                                drawText(data.data.take(10), dateX, dateY, textPaintDates)
                                restore()
                            }
                        }
                    }
                }
            }
        }
    } else {
        // CORREGIT ELS PARÀMETRES DEL NOELSCREEN
        NoelScreen(
            paddingValues = paddingValues,
            title = "CONSUM M³",
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = comptadorSeleccionat?.descripcio ?: "Comptador...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecciona un Comptador") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()                            )
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                comptadors.forEach { comptador ->
                                    DropdownMenuItem(
                                        text = { Text(comptador.descripcio ?: "") },
                                        onClick = {
                                            comptadorSeleccionat = comptador
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { showStartPicker = true }) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Inici", style = MaterialTheme.typography.labelSmall)
                                    Text(dataInici.format(formatter), fontWeight = FontWeight.Bold)
                                }
                            }
                            OutlinedCard(modifier = Modifier.weight(1f).clickable { showEndPicker = true }) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Fi", style = MaterialTheme.typography.labelSmall)
                                    Text(dataFi.format(formatter), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        NoelButton(
                            text = if (isLoading) "CARREGANT..." else "MOSTRAR GRÀFICA",
                            isLoading = isLoading,
                            onClick = { carregarGrafica() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
}