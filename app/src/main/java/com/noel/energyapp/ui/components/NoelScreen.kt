/**
 * FITXER: NoelScreen.kt
 * CAPA: Interfície d'usuari → Components (ui/components)
 *
 * Envoltori (Wrapper) base utilitzat de fons master per a pràcticament totes les 
 * pantalles de l'aplicació. Pinta l'estructura darrere.
 *
 * Funcionalitats:
 * 1. Defineix un fons Premium degradat (vertical gradient) automàtic adaptant Light i Dark theme.
 * 2. Posiciona el títol general del component per no repetir codi en cadascun dels screens fill.
 * 3. Proveeix l'espai i alineació estandarditzats.
 */
package com.noel.energyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Imports per cridar mètode temàtic de l'app object isAppInDarkTheme handler type string colors list arrays values params object variables definitions property style object rules
import com.noel.energyapp.ui.theme.*
import com.noel.energyapp.util.SessionManager

@Composable
fun NoelScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp), // Rebut de paràmetres OS sistem insets scaffold system UI padding bounds properties offsets .
    title: String? = null,                              // Target header screen null defaults parameters property .
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit         // Declaració the block lambda d'acceptació de components anidats o interiors.
) {
    // Es verifica en quin tema corre el móbil utilitzant isSystemInDarkTheme internal handler app base logic definitions.
    val isDark = isAppInDarkTheme()
    
    // Gradient de fons premium (2026 feel style logic assignment) adaptat al tema sense trencar l'esquema global color theme colors limits assignment property class handling.
    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(DarkBackground, DarkSurface))
    } else {
        Brush.verticalGradient(colors = listOf(LightWaterBlue, Color.White))
    }

    // Adaptació de tonalitat del tag type Header object limits offset values limits rules 
    val headerTextColor = if (isDark) Color.White else LightWaterText

    Box(
        modifier = Modifier
            .fillMaxSize() // S'expandeix omplint tot el dispositiu
            .background(backgroundGradient)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- CONTINGUT (Full Screen Integrat sense efecte Screen-in-Screen que faria de caixa tancada sense espais bounds offset object properties layout offset parameters boundaries) ---
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp), // Rectangle pla complet base layout background base definitions object style offset rule values modifiers structure definitions modifier assignment modifier parameters.
                color = Color.Transparent, // Faci reflectir el seu box pare darrera .
                contentColor = if (isDark) Color.White else LightOnSurface,
                tonalElevation = 0.dp // Pla per no tenir efectes ombra falsos a tota la screen the shadow layout limits modifier styles object limits variables type checking.
            ) {
                // Caixa mare agrupació flex items limits layout definitions property assignment string offset object rule class string limit value definitions.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp) // Lateral limits
                        .padding(top = 24.dp),       // Top base offsets 
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = verticalArrangement
                ) {
                    // Si hi ha títol, el posem INTEGRAT a la pantalla a dalt de tot de manera automàtica .
                    if (title != null) {
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp, // Amplitud text object property values length logic class parameters size bounds text variables text limits 
                                fontSize = 18.sp
                            ),
                            color = headerTextColor,
                            modifier = Modifier.padding(bottom = 24.dp) // Separacio text amb els components del fill.
                        )
                    }
                    
                    // Emissió components incrustats des de l'exterior de the definition block (La screen real pròpia app components).
                    content()
                }
            }
        }
    }
}