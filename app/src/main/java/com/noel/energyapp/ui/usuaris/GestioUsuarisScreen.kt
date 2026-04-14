/**
 * FITXER: GestioUsuarisScreen.kt
 * CAPA: Interfície d'usuari → Usuaris (ui/usuaris)
 *
 * Aquesta pantalla d'administració permet la gestió integral dels usuaris del sistema.
 * 
 * Funcionalitats principals:
 * 1. Llistat d'usuaris amb busqueda intel·ligent (per @nick, nom i cognom).
 * 2. Visualització de rols i estat d'accés ràpid mitjançant indicadors visuals (Actiu/Inactiu).
 * 3. Activar/Desactivar usuaris mitjançant Switch ràpid, excepte els de rol ADMIN.
 * 4. Botó per reassignar el Rol a través d'un popup amb RadioButtons (ADMIN, SUPERVISOR, TECNIC).
 * 5. Botó d'assignació de referència per permetre l'accés d'un subjecte a determinades Plantes existents mitjançant Checkboxes.
 * 6. Botó d'ajust per forçar el restabliment d'una contrasenya d'un usuari oblidadís (Default a "123456").
 * 7. Botó global flotant per a crear usuaris predefinint el seu nick, nom complet i rol mitjançant crida JSON.
 *
 * NOTA DE SEGURETAT: Per defecte qualsevol acció interactua paral·lelament amb l'API Backend per garantir veracitat de DB abans de permetre UI.
 */
package com.noel.energyapp.ui.usuaris

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// Conjunt d'icones funcionals per creació, cerca i restabliment de cadenat virtual
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Control d'interfície bàsics
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// DTOs i Client HTTP pel tracte entre App C#
import com.noel.energyapp.data.CrearUsuariDto
import com.noel.energyapp.data.PlantaDto
import com.noel.energyapp.data.UpdateUsuariDto
import com.noel.energyapp.data.UsuariResumDto
import com.noel.energyapp.network.RetrofitClient
// Components gràfics propis
import com.noel.energyapp.ui.components.NoelScreen
import com.noel.energyapp.ui.theme.StatusGreenLight
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

// Llista de rols permesos al sistema SCADA de Noel per a la gestió UI
// ---------------------------------------------------------
// EINES DE TRADUCCIÓ (UI <-> Base de Dades)
// ---------------------------------------------------------
val rolsDisponibles = listOf("ADMIN", "SUPERVISOR", "TÈCNIC") // Llista visual amb els accents normatius

// Funció que converteix el que ve de la BD (ex: TECNIC) a lo que veu l'usuari final per major comprensió (ex: TÈCNIC)
fun rolDBToVisual(rolDB: String): String {
    return if (rolDB.equals("TECNIC", ignoreCase = true)) "TÈCNIC" else rolDB.uppercase()
}

// Funció que converteix el que veu l'usuari formaltajat en pantalla al què entén exactament la BD a la crida
fun rolVisualToDB(rolUI: String): String {
    return if (rolUI == "TÈCNIC") "TECNIC" else rolUI
}
// ---------------------------------------------------------

@Composable
fun GestioUsuarisScreen(
    paddingValues: PaddingValues, // Sistemes d'espaiat inferiors i superiors propis d'Android Navbar
    onBackClick: () -> Unit       // Callback per a retornar navegació a la pantalla base
) {
    // --- 1. CONFIGURACIÓ I CONTEXT ---
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // SessionManager per tractar la clau i accés des de l'emmagatzematge xifrat
    val sessionManager = remember { SessionManager(context) }
    val token = sessionManager.fetchAuthToken() ?: ""

    // --- 2. ESTATS DE LES DADES ---
    var usuaris by remember { mutableStateOf<List<UsuariResumDto>>(emptyList()) }
    var plantes by remember { mutableStateOf<List<PlantaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // --- 3. ESTATS DELS POP-UPS (DIÀLEGS d'Acció per Usuari) ---
    var usuariSeleccionat_per_rol by remember { mutableStateOf<UsuariResumDto?>(null) }
    var usuariSeleccionat_per_plantes by remember { mutableStateOf<UsuariResumDto?>(null) }
    var usuariAResetejar by remember { mutableStateOf<UsuariResumDto?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    
    // Controlador de la busqueda o filtratge de llistari
    var searchQuery by remember { mutableStateOf("") }

    // Funció interna per refrescar la llista d'usuaris i de plantes en un sol glop invocat des del background Api
    fun recarregarDades() {
        scope.launch {
            isLoading = true
            try {
                // Obtenim usuaris i plantes en paral·lel o asincrònic per eficiència temporal de recàrrega
                val resUsuaris = RetrofitClient.instance.getUsuaris("Bearer $token")
                val resPlantes = RetrofitClient.instance.getPlantes("Bearer $token")

                if (resUsuaris.isSuccessful) usuaris = resUsuaris.body() ?: emptyList()
                if (resPlantes.isSuccessful) plantes = resPlantes.body() ?: emptyList()

            } catch (e: Exception) {
                // Evita tancaments sobtats d'app si WiFi cau, i avisa al moment
                Toast.makeText(context, "Error de connexió al servidor", Toast.LENGTH_SHORT).show()
            } finally {
                // Acaba amagant el globus de càrrega per ensenyar resultats o blank screen
                isLoading = false
            }
        }
    }

    // Carreguem les dades només la primera vegada en entrar a la pantalla muntant l'espai
    LaunchedEffect(Unit) {
        recarregarDades()
    }

    // --- 4. LÒGICA DE FILTRATGE INTEL·LIGENT ---
    // Aquest filtre recalcula a temps real el llistat original (no el detreu).
    // Ara busquem tant pel Nick (@username) com pel Nom o Cognom real si conté un tros per facilitar.
    val usuarisFiltrats = usuaris.filter {
        it.nomUsuari.contains(searchQuery, ignoreCase = true) ||
                it.nom.contains(searchQuery, ignoreCase = true) ||
                it.cognom.contains(searchQuery, ignoreCase = true)
    }

    // --- 5. ESTRUCTURA VISUAL (Screen pare) ---
    NoelScreen(
        paddingValues = paddingValues,
        title = "GESTIÓ D'USUARIS",
        verticalArrangement = Arrangement.Top
    ) {
        // --- BARRA DE RECERCA I BOTÓ D'AFEGIR NOU ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically // Element centrats transversalment entre recuadre i iconBotó
        ) {
            // TextField pel Input Cerca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar per nom o nick...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, // Lupa com accés visual
                modifier = Modifier.weight(1f), // Toba tot allò que pugui excepte el plus botó en l'alineació de Row
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Espai d'oxigenació entre camp i botonera propera
            Spacer(modifier = Modifier.width(8.dp))

            // Botó verdos per crear un nou usuari a DB
            FloatingActionButton(
                onClick = { showCreateUserDialog = true }, // Set true per llançar Popup inferior
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nou Usuari", tint = Color.White)
            }
        }

        // --- CONTINGUT PRINCIPAL DEL GRID LLISTAT ---
        if (isLoading) {
            // Animació simple si la càrrega des de recarregarDades transita
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Component per col·locar el loop gràfic
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                // Passant el array filtrador intel·ligent on rederigua cada element per "usuari" variable
                items(usuarisFiltrats) { usuari ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.medium,
                        // Utilització d'elevació per destacar cada caixa de cadascun empleat
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // FILA SUPERIOR de Tarjeta Usuari: Noms i Switch d'Actiu/Apagat
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Determinem què mostrem en gran (Prioritat Nom Real en lloc del user d'Ingrès)
                                    val títolCard =
                                        if (usuari.nom.isNotBlank()) "${usuari.nom} ${usuari.cognom}" else usuari.nomUsuari

                                    Text(
                                        text = títolCard,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold // Més engreixat tipogràfic per jerarquia de caixes
                                    )

                                    // Si estem mostrant el nom real a dalt per títol, mostrem el @nick sota
                                    if (usuari.nom.isNotBlank()) {
                                        Text(
                                            text = "@${usuari.nomUsuari}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Informació d'Estat: Actiu / Inactiu al final escarreton
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isActiu = usuari.actiu == true

                                    // Decidim si el Switch d'activació ha de permetre ús:
                                    // Si l'usuari de la llista és ADMIN global, no permetem jugar al tancament per autoprotecció al fronted
                                    val esAdmin = usuari.rol.equals("ADMIN", ignoreCase = true)

                                    Text(
                                        text = if (isActiu) "ACTIU" else "INACTIU",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            esAdmin -> Color.Gray // Si és admin, gris (inmutable aparent)
                                            isActiu -> StatusGreenLight // Si on us verd 
                                            else -> MaterialTheme.colorScheme.error // Vermell si ha estat deshabilitat anteriorment
                                        },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    
                                    // Cèl·lula que dicta canvi boolean
                                    Switch(
                                        checked = isActiu,
                                        enabled = !esAdmin, // Si ets l'admin en el llistat no permet el desactiva't tu sol
                                        onCheckedChange = { isChecked ->
                                            scope.launch {
                                                // Optimisme visual primer: modifico aspecte per UX àgil 
                                                usuaris = usuaris.map {
                                                    if (it.id == usuari.id) it.copy(actiu = isChecked) else it
                                                }
                                                // Transmet realment l'estat si es manté o falleix ho revertirem reloadant finalment
                                                try {
                                                    // Request nul apart d'actiu pel canvi solament indicat per DTO C# backend patch logic
                                                    val request = UpdateUsuariDto(
                                                        usuari.id,
                                                        null,
                                                        isChecked,
                                                        null,
                                                        null
                                                    )
                                                    RetrofitClient.instance.actualitzarUsuari(
                                                        "Bearer $token",
                                                        request
                                                    )
                                                } catch (e: Exception) {
                                                    recarregarDades() // Força refred del context local si error a la xarxa cau per assegurar estat d'error real
                                                    Toast.makeText(
                                                        context,
                                                        "Error al servidor",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        },
                                        // Colors propis de seguretat visual en Switch deshabilitat on el Track perd força i manté blanc color
                                        colors = SwitchDefaults.colors(
                                            disabledCheckedTrackColor = StatusGreenLight.copy(alpha = 0.5f),
                                            disabledCheckedThumbColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // FILA INFERIOR de Tarjeta Usuari: Botons d'acció (Rol, Plantes, Reset)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Botó "Tipus Canvi de rol" - en visualitzacions de container
                                Button(
                                    onClick = { usuariSeleccionat_per_rol = usuari }, // Provocarà el desplegment del alert per Roles
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text(
                                        text = usuari.rol, // Posa el propi rol ja dintre botu com titol d'ajut visual global (p.e: "ADMIN")
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                // 2. Botó per assignar permisos de plantes específiques
                                Button(
                                    onClick = { usuariSeleccionat_per_plantes = usuari },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "PLANTES",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                // 3. Botó de seguretat iconogràfic, vermell per reset de contrasenya universal al 123456 en descuit usuari
                                IconButton(onClick = { usuariAResetejar = usuari }) { // Llança dialeg d'afirmacio
                                    Icon(
                                        Icons.Default.LockReset,
                                        contentDescription = "Reset Password",
                                        tint = MaterialTheme.colorScheme.error // El faig de resalta per alerta
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 6. POP-UPS DE GESTIÓ SECUNDARIS DE PANTALLA ---

    // 6.1 DIÀLEG: RESET PASSWORD OBLIGANT A DEFAULT API
    if (usuariAResetejar != null) {
        AlertDialog(
            onDismissRequest = { usuariAResetejar = null },
            title = { Text("Restablir Contrasenya") },
            text = { Text("Estàs segur que vols restablir la contrasenya de '${usuariAResetejar?.nomUsuari}'? Es canviarà a '123456' i se l'obligarà a canviar-la en el proper inici.") },
            confirmButton = {
                Button(
                    onClick = {
                        val usernameTarget = usuariAResetejar?.nomUsuari ?: ""
                        scope.launch {
                            try {
                                // Request mapeja un diccionari amb key nom de var C# api params
                                val response =
                                    RetrofitClient.instance.resetPassword(mapOf("username" to usernameTarget))
                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Contrasenya restablerta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Com la modificació de la crida anterior no rebem info DTO per reloadar a efectes de nou, l'apliquem global.
                                    recarregarDades()
                                }
                            } finally {
                                usuariAResetejar = null // Surt d'aquesta finestra independent de catxa.
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("SÍ, RESTABLIR") }
            },
            dismissButton = {
                TextButton(onClick = {
                    usuariAResetejar = null
                }) { Text("Cancel·lar") }
            }
        )
    }

    // 6.2 DIÀLEG: CREAR NOU USUARI
    if (showCreateUserDialog) {
        // Estat referent als valors interiors que l'usuari de teclat digita quan demana fer persona nova.
        var nouNick by remember { mutableStateOf("") }
        var nouNomReal by remember { mutableStateOf("") }
        var nouCognom by remember { mutableStateOf("") }
        var nouRol by remember { mutableStateOf("TÈCNIC") } // Predisposició baixa com a defecte sense riscos per errada humana

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text("Crear Nou Usuari") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Configuració inicial de l'empleat:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nouNick,
                        onValueChange = { nouNick = it },
                        label = { Text("Nom d'usuari (@nick)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nouNomReal,
                        onValueChange = { nouNomReal = it },
                        label = { Text("Nom real") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nouCognom,
                        onValueChange = { nouCognom = it },
                        label = { Text("Cognom real") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Selecciona el Rol d'accés:", style = MaterialTheme.typography.labelMedium)
                    
                    // Foreach per les opciones globals d'estat a Radio buttons tipics de Android (sense ser Dropdown que despista mes aquí)
                    rolsDisponibles.forEach { rolNom ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (nouRol == rolNom),
                                onClick = { nouRol = rolNom })
                            Text(text = rolNom)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nouNick.isBlank() || nouNomReal.isBlank()) {
                        Toast.makeText(
                            context,
                            "Mínim Nick i Nom són obligatoris",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button // Prevé execució if validant negatiu al final d'app
                    }
                    scope.launch {
                        try {
                            // Adaptació base xarxa
                            val rolPerAPI = rolVisualToDB(nouRol)
                            // Nou DTO de crear un element
                            val request =
                                CrearUsuariDto(nouNick, nouNomReal, nouCognom, rolPerAPI, emptyList())
                            val response =
                                RetrofitClient.instance.crearUsuari("Bearer $token", request)
                            if (response.isSuccessful) {
                                recarregarDades() // Oblidem els restants posats manuals despres de succés de crear usuari
                                showCreateUserDialog = false
                            } else {
                                // Casos tìpics d'errors http: "Duplicat Nick existent de sql"
                                Toast.makeText(
                                    context,
                                    "Aquest nick ja existeix!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) { /* Error Xarxa silenciós, recollir error ja hi ha al update */
                        }
                    }
                }) { Text("CREAR USUARI") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateUserDialog = false // Taca per dalt popup
                }) { Text("Cancel·lar") }
            }
        )
    }

    /// 6.3 DIÀLEG: CANVI DE ROL D'UN EXSISTENT 
    if (usuariSeleccionat_per_rol != null) {
        val usuari = usuariSeleccionat_per_rol!!
        // 1. Traduïm pel valor original d'App per l'etiqueta mostrada: "TECNIC" referent format visual ("TÈCNIC") 
        val rolActualVisual = rolDBToVisual(usuari.rol)

        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_rol = null },
            title = { Text("Canviar Rol") },
            text = {
                Column {
                    // Crea llista d'opcions com el crear però només amb valor
                    rolsDisponibles.forEach { rolNom ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp) // Espai adequat pulsació dital
                        ) {
                            RadioButton(
                                // 2. Ara comparem visual amb visual lliurat com state
                                selected = (rolActualVisual == rolNom),
                                onClick = {
                                    scope.launch {
                                        try {
                                            // 3. Tornem a traduir directament sota cap a la BD de retorn (TÈCNIC -> TECNIC)
                                            val rolPerAPI = rolVisualToDB(rolNom)

                                            val request = UpdateUsuariDto(
                                                usuari.id,
                                                nouRol = rolPerAPI,
                                                actiu = null,
                                                canviPasswordObligatori = null,
                                                idsPlantes = null
                                            )
                                            // Actuació sobre la mateixa URL Rest updateUsr
                                            RetrofitClient.instance.actualitzarUsuari(
                                                "Bearer $token",
                                                request
                                            )
                                            recarregarDades()
                                        } finally {
                                            usuariSeleccionat_per_rol = null
                                        }
                                    }
                                }
                            )
                            Text(text = rolNom)
                        }
                    }
                }
            },
            confirmButton = {
                // Tancar aquí ho considerem accés sense confirmar, com l'onClick d'abans de radio button fa auto-submit immediat, no fa falta extra ok "Confirm."
                TextButton(onClick = {
                    usuariSeleccionat_per_rol = null
                }) { Text("Tancar") }
            }
        )
    }

    // 6.4 DIÀLEG: ASSIGNACIÓ DE PLANTES D'ACCÉS A USUARIS
    // Quan polsem el button secundari de PLANTA d'usuari a la Llista card, generem Checkpoints per totes les opcions de map d'admin base general disponibles (si estan marcades Actives a l'app).
    if (usuariSeleccionat_per_plantes != null) {
        val user = usuariSeleccionat_per_plantes!!

        // Aquesta var local reté la set d'enters(idsPlantes) pel pre-checking a l'obertura pel render gràfic
        var plantesSeleccionades by remember(user.id) {
            // 1. Agafem el text brut que envia el C# (Ex: "Noel-1, Noel-7") per unió Join com informacio visual plana com a fallback anterior en arquitectura global (ara d'us de processat front).
            val textAssignades = user.plantesAssignadesText ?: ""

            // 2. Separem els noms per comes i traiem els espais extra en ràdio blanc dels costats
            val nomsAssignats = textAssignades.split(",").map { it.trim() }

            // 3. Busquem dinàmicament a la nostra llista descarregada objecte plenes quins IDs els hi corresponen a aquests grups per reconstruir l'array d'IDs per checkboxes Checkers
            val idsCalculats = plantes
                .filter { nomsAssignats.contains(it.nom_planta) }
                .map { it.id_planta }
                .toSet()

            // Carreguem els IDs directament als Ticks calculats! State persistent comença amb referència a vells
            mutableStateOf(idsCalculats)
        }

        AlertDialog(
            onDismissRequest = { usuariSeleccionat_per_plantes = null },
            title = { Text("Permisos de Planta") },
            text = {
                Column {
                    Text(
                        "Selecciona a quines plantes pot accedir @${user.nomUsuari}:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Nomes mostra per elegir les marques de fàbrica ON generals de Base, en context, i així excloure fora de serveis (a BD o switch del module Gestió Planta)
                    plantes.filter { it.activa }.forEach { planta ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Checkbox visual preu la determinació iterada i llistada de boolean state (Ones that overlap match setIds contains ids.)
                            Checkbox(
                                checked = plantesSeleccionades.contains(planta.id_planta),
                                onCheckedChange = { isChecked ->
                                    plantesSeleccionades =
                                        // Afegeix o treu a Set immutable reconstruít segons polsador 
                                        if (isChecked) plantesSeleccionades + planta.id_planta
                                        else plantesSeleccionades - planta.id_planta
                                }
                            )
                            Text(text = planta.nom_planta)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            // Update amb només IDs Plantes en llista.
                            val request = UpdateUsuariDto(
                                user.id,
                                null,
                                null,
                                null,
                                plantesSeleccionades.toList()
                            )
                            RetrofitClient.instance.actualitzarUsuari("Bearer $token", request)
                            recarregarDades()
                        } finally {
                            usuariSeleccionat_per_plantes = null
                        }
                    }
                }) { Text("GUARDAR") }
            },
            dismissButton = {
                TextButton(onClick = { usuariSeleccionat_per_plantes = null }) {
                    Text("Cancel·lar")
                }
            }
        )
    }
}