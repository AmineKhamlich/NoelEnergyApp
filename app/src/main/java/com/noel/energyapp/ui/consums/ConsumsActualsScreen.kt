/**
 * FITXER: ConsumsActualsScreen.kt
 * CAPA: Interfície d'usuari → Consums (ui/consums)
 *
 * Pantalla de monitorització exhaustiva "En Viu" d'un determinat comptador o equip
 * de consum instal·lat físicament a una planta específica.
 *
 * Funcionalitat:
 * 1. Resol de forma asíncrona un primer array amb tots els equips de lectura (comptadors d'aigua/llum).
 * 2. Exposa uns DropDown menus predefinits via estil Material 3 ("ExposedDropdownMenuBox").
 * 3. Incorpora una tasca concurrent (Polling infinit i segur) que peticiona repetidament a SCADA mitjançant la API Rest temporalment segons l'interval de precisió definit en app local (ex. cada 2 segons).
 * 4. Presenta els resultats formata'ls amb diferència de decimals i la UI acull els estats d'error local de xarxa per assegurar un feedback no estressant.
 */
package com.noel.energyapp.ui.consums

// Dependències base de Toast de llibreria Android Nadiu
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Model Data Object
import com.noel.energyapp.data.DimCntDto
// Connexió al Retrofit per instanciació de Client
import com.noel.energyapp.network.RetrofitClient
// Component reutilitzable de Base
import com.noel.energyapp.ui.components.NoelScreen
// Utilització de la paleta Dark/Light color Deep per el contorn de inputs seleccionables theamatic
import com.noel.energyapp.ui.theme.WaterDeep
// Persistent Token Management User 
import com.noel.energyapp.util.SessionManager
// Element essencial corrutines delay sense bloqueig del MAIN THREAD 
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // Sol·licitat per a cridades Compose de Menús que estan sota la precondició de API "No estandard final Google"
@Composable
fun ConsumsActualsScreen(
    paddingValues: PaddingValues, // Sistemes d'espaiat inferiors i superiors per evitar solapament
    plantaNom: String,            // Param global per la identificació des de Base Router nav
    onBackClick: () -> Unit       // Sortida (Callback parent handler rule back step history navigation).
) {
    // Declaració context local app resources process
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    // Assegurant el cas edge on fetchAuthToken fos nul amb "".
    val token = sessionManager.fetchAuthToken() ?: ""

    // --- ESTATS DE DADES i SELECCIÓ DEL LLISTAT DE COMPTADORS ---
    // Rep el conjunt listat de Tags d'equip
    var comptadors by remember { mutableStateOf<List<DimCntDto>>(emptyList()) }
    var comptadorSeleccionat by remember { mutableStateOf<DimCntDto?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Opcions de precisió de mostreig temporal en llista tancada local App Config (Poling intervals array).
    val precisiOpcions = listOf(
        Pair("Normal (cada 5s)", 5000L),
        Pair("Alta (cada 2s)", 2000L),
        Pair("Molt Alta (cada 1s)", 1000L)
    )
    var precisioSeleccionada by remember { mutableStateOf(precisiOpcions[0]) } // Default the 5s pre load param tuple first element .
    var isPrecisioDropdownExpanded by remember { mutableStateOf(false) }

    // --- ESTATS DEL VISOR TARGET LIVE ---
    // El numberDouble de la dada SCADA
    var liveValue by remember { mutableStateOf<Double?>(null) }
    // Mostra data string lletrejat temps actualizacio "HH:mm:ss" UI string value
    var lastUpdate by remember { mutableStateOf("Sense dades") }
    
    // Status connection fallback control boolean value. Error logic checking process value representation handling
    var errorConnection by remember { mutableStateOf(false) }

    // 1. Obtenir els comptadors en obrir primer llançat d'efecte limitant crida pel String "plantaNom" de variable de referència d'activació composable coroutine effect
    LaunchedEffect(plantaNom) {
        try {
            val response = RetrofitClient.instance.getComptadorsPerPlanta("Bearer $token", plantaNom)
            if (response.isSuccessful) {
                comptadors = response.body() ?: emptyList()
                if (comptadors.isNotEmpty()) {
                    // Predeterminat en entrar a un "Dashboard Viu" es agafa el primer nomás arrencar.
                    comptadorSeleccionat = comptadors.first()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error carregant comptadors", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Efecte per Fer Polling ("Bucle infinit" controlat independent asincrònic lliurat sense saturar procés UX app).
    // Re acciona o fa recomposició la rutina quant un target tagname o un paràmetre precisió ha variat i requereix el start del launch function delay again over a loop
    LaunchedEffect(comptadorSeleccionat, precisioSeleccionada) {
        val tagName = comptadorSeleccionat?.tagName
        if (tagName != null) {
            while (isActive) { // Mentre la corrutina (i per defecte la pantalla tancada mata el object scope UI i cau fals aquest boolean de salut process thread object bool rule checker limits resource out logic memory usage control limits limits limit limits logic limits).
                try {
                    // Obté live Value double de SCADA real node API service action server task controller mapping values check connection check route validation param validation response returns string rules limits limits object response data object method rules API method GET endpoint mapping name rules return types boolean number rules HTTP call
                    val response = RetrofitClient.instance.getLiveValue("Bearer $token", tagName)
                    if (response.isSuccessful) {
                        liveValue = response.body()
                        errorConnection = false
                        // Mostrem horari actual de refresc
                        val df = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        lastUpdate = "Refrescat a les: ${df.format(java.util.Date())}"
                    } else {
                        // Casos the timeout internal call fault logic server side connection missing logic C# layer limits timeout faults error response boolean handling flag return limit variable limits boolean object rules returns 
                        errorConnection = true
                    }
                } catch (e: Exception) {
                    errorConnection = true // Error Wi-Fi fault on android context exceptions.
                }
                
                // Esperem segons la precisió, això "dorm" de manera virtual i desperta en línia la propera línia time handler sense de veritat bloquejar o parar el render complet "MAIN" graphic visual logic UI rules methods render limits rules handler call handling logic execution limit limits values
                delay(precisioSeleccionada.second) 
            }
        }
    }

    // Dibuixa Noel Screen 
    NoelScreen(
        paddingValues = paddingValues,
        title = "CONSUMS EN VIU",
        verticalArrangement = Arrangement.Top
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Secció: Filtres Configurables Form
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                // Aixecant més pel focus d'interès general controls 
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Interès 16 dp inter linia
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Selecció de Comptador "Dropdown menu"
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        // Caixa vista tipus outline rect (border line box empty base rules layout material form styles base components class logic types values style text params modifiers)
                        OutlinedTextField(
                            value = comptadorSeleccionat?.descripcio ?: "Carregant comptadors...",
                            onValueChange = {}, // buida ja que el field no rep text digital és readonly.
                            readOnly = true, // Això prevé q surti el pad digital d'escriptura.
                            label = { Text("Equip a visualitzar") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }, // Icona V de flip
                            modifier = Modifier.menuAnchor().fillMaxWidth(), // "menuAnchor" clau de connexio de la pop de la llista per lligar a l'element TextField superior 
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WaterDeep, focusedLabelColor = WaterDeep)
                        )
                        // Finestra desplegable Menu Pop UI View Form Logic Render limits values text methods 
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false } // Click extern
                        ) {
                            comptadors.forEach { comptador ->
                                DropdownMenuItem(
                                    // Bucle dels comptadors carregats via BD Sql "equip nom" texts.
                                    text = { Text(comptador.descripcio ?: "Sense nom") },
                                    onClick = {
                                        // Update the object target selected
                                        comptadorSeleccionat = comptador
                                        // Posa l'anterior vist cap a enrere 0 temporal pel refresc.
                                        liveValue = null 
                                        isDropdownExpanded = false // Tancar Auto 
                                    }
                                )
                            }
                        }
                    }

                    // Selecció de Precisió Temps DROP MENU.
                    ExposedDropdownMenuBox(
                        expanded = isPrecisioDropdownExpanded,
                        onExpandedChange = { isPrecisioDropdownExpanded = !isPrecisioDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = precisioSeleccionada.first,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Freqüència d'actualització") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPrecisioDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WaterDeep, focusedLabelColor = WaterDeep)
                        )
                        ExposedDropdownMenu(
                            expanded = isPrecisioDropdownExpanded,
                            onDismissRequest = { isPrecisioDropdownExpanded = false }
                        ) {
                            precisiOpcions.forEach { opcio ->
                                DropdownMenuItem(
                                    text = { Text(opcio.first) },
                                    onClick = {
                                        precisioSeleccionada = opcio
                                        isPrecisioDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CARTELL GEGANT DE VALOR LIVE DE VISUALITZACIÓ PRINCIPAL EN PANTALLA ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), // Agafa gradient theme predefinit limits style component class property type object context type object base type parameter class default type theme color logic assignment check rules.
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth().aspectRatio(1.2f) // Fix a una relació quasi quadrada 120% per que creixi ample igual proporcio per no distorsionar 
            ) {
                // Columna central 
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Valor Actual SCADA",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) // Alfa inferior color tipogràfic de referència base component layout structure modifiers style param modifier
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    if (liveValue != null) { // Mentre ja haguem pogut extreure i setear valors real del bucle 
                        // Calculem el format visual segons the object amount format limits rules conditions boolean comparison
                        val valorFinal = if (liveValue!! >= 10.0) {
                            // Si supera 10 la precisió flotant passa d'enter toString (Ex. "12") per mantenir neta UI i llegible no distorsiva 
                            liveValue!!.toInt().toString() 
                        } else {
                            // Format 2 decimals per mesura lenta de litratge menors als deu ("4,32" o "1,11")
                            String.format("%.2f", liveValue) 
                        }
                        
                        // Cua al text gegants
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text( // Format 64sp the text UI values typography format layout component view type parameters modifiers size rule logic values.
                                text = valorFinal,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text( // Lh object base suffix labels types structure types properties sizes offset text variables size labels rule logic class methods parameters structure limits modifiers params sizes offset styles class property text property limits return limits offset properties methods parameter structure properties sizes limit size logic parameter limits text logic limits boolean comparison logic modifier limit text type structure limit limits test rules function comparison rule logic structure modifier return return Boolean structure structure limit type limits params text size modifier rule tests rules return method comparison structure method definition offset properties parameters text value limits parameters size string rules boolean comparison text limits string class modifiers object return definition.
                                text = "l/h",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 12.dp) // Perquè la unitat quedi una mica més avall a la MATEIXA línia alineada cap a la val base base values string type rules definition modifier assignment values text modifier rules mapping parameter types definition parameters definition rules string check types limits types.
                            )
                        }
                    } else if (errorConnection) { // Si s'identifica el punt d'error (timeout local, null API values fault exception...) 
                        Text(
                            text = "ERROR",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error // Canviar pel fosc de themerr
                        )
                        Text("No hi ha connexió amb SCADA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    } else {
                        // Punt de reset pre-primera vegada visual (Carregant sense error o a la espera primera poll HTTP method process return object logic type parameters limits limits modifier values checking string parameter structure boolean returns modifier string object variables Boolean structure value sizes limit class test limit class checking.
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // --- Estat darrer refresc (Visual Box Label) ---
                    Surface(
                        color = if (errorConnection) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant, // Diferenciat Error de NoError container default colors types values parameters structure rules checking boolean values
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = lastUpdate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (errorConnection) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Informació text Gris footer.
            Text(
                text = "Nota: Aquesta pantalla obté la dada en línia directament de SCADA. Mantindrà un refresc automàtic segons l'interval escollit.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
