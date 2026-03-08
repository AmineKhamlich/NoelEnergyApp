package com.noel.energyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noel.energyapp.util.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoelScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    title: String? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    onBackClick: (() -> Unit)? = null,
    hasMenu: Boolean = false,
    // NOU: Afegim les accions de navegació del menú. Són opcionals (null per defecte)
    onNavigateToGestioPlantes: (() -> Unit)? = null,
    onNavigateToGestioUsuaris: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isAppScreen = title != null

    // NOU: Instanciem el SessionManager directament a la plantilla
    // Així TOTA l'App sabrà quin rol té l'usuari sense haver de passar-ho pantalla per pantalla.
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userRole = sessionManager.fetchUserRole() ?: ""

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = hasMenu,
        drawerContent = {
            if (hasMenu) {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "MENÚ PRINCIPAL",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()

                    // Opció estàndard per a tothom
                    Text(
                        text = "⚙️ Ajustos (Pròximament)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    // MÀGIA: Només mostrem aquestes opcions si l'usuari és administrador
                    // (Assegura't que "admin" coincideix amb el que guarda la teva BD, potser és "ADMIN")
                    if (userRole.equals("ADMIN", ignoreCase = true)) {
                        HorizontalDivider()

                        Text(
                            text = "🏭 Gestió de Plantes",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // 1. Tanquem el calaix del menú
                                    scope.launch { drawerState.close() }
                                    // 2. Naveguem (si ens han passat la ruta)
                                    onNavigateToGestioPlantes?.invoke()
                                }
                                .padding(16.dp)
                        )

                        Text(
                            text = "👥 Gestió d'Usuaris",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    onNavigateToGestioUsuaris?.invoke()
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    ) {
        // ... (La resta de l'Scaffold i la interfície gràfica es queda EXACTAMENT igual que abans) ...
        // T'ho poso sencer perquè només hagis de copiar i enganxar:
        Scaffold(
            modifier = Modifier.padding(paddingValues),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (title != null) {
                    CenterAlignedTopAppBar(
                        title = { Text(text = title.uppercase(), style = MaterialTheme.typography.titleLarge) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            if (onBackClick != null) {
                                IconButton(onClick = onBackClick) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Tornar", tint = Color.White)
                                }
                            }
                        },
                        actions = {
                            if (hasMenu) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, "Obrir Menú", tint = Color.White)
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (isAppScreen) {
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(MaterialTheme.colorScheme.primary))
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = if (isAppScreen) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) else RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .padding(top = if (isAppScreen) 24.dp else 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = verticalArrangement
                    ) {
                        content()
                    }
                }
            }
        }
    }
}