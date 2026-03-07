package com.noel.energyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoelScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    title: String? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    onBackClick: (() -> Unit)? = null,
    hasMenu: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isAppScreen = title != null

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

                    Text(
                        text = "⚙️ Ajustos (Pròximament)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.padding(paddingValues),
            // CORRECCIÓ: El Scaffold torna a ser fons blanc, adeu a la franja blava de baix!
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (title != null) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            if (onBackClick != null) {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Tornar enrere",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        actions = {
                            if (hasMenu) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Obrir Menú",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->

            // Creem una caixa que contingui el fons i el contingut
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Truc de disseny: Pintem un quadrat blau només a dalt de tot
                // Això es veurà a través dels cantons arrodonits de la targeta blanca
                if (isAppScreen) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp) // Només cal un tros petit
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                // CORRECCIÓ: Utilitzem un Surface.
                // Surface és intel·ligent: si ell és blanc, les lletres de dins seran negres automàticament.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = if (isAppScreen) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) else RoundedCornerShape(0.dp),
                    color = MaterialTheme.colorScheme.background // Blanc
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .padding(top = if (isAppScreen) 24.dp else 0.dp), // Aire superior per no tocar la corba
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