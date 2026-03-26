package com.noel.energyapp.ui.components

import androidx.compose.foundation.background
import com.noel.energyapp.ui.theme.isAppInDarkTheme
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
import com.noel.energyapp.ui.theme.*
import com.noel.energyapp.util.SessionManager

@Composable
fun NoelScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    title: String? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isAppInDarkTheme()
    
    // Gradient de fons premium (2026 feel) adaptat al tema
    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(DarkBackground, DarkSurface))
    } else {
        Brush.verticalGradient(colors = listOf(LightWaterBlue, Color.White))
    }

    val headerTextColor = if (isDark) Color.White else LightWaterText

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- CONTINGUT (Full Screen Integrat sense efecte Screen-in-Screen) ---
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                color = Color.Transparent,
                contentColor = if (isDark) Color.White else LightOnSurface,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = verticalArrangement
                ) {
                    // Si hi ha títol, el posem INTEGRAT a la pantalla a dalt de tot
                    if (title != null) {
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp,
                                fontSize = 18.sp
                            ),
                            color = headerTextColor,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                    
                    content()
                }
            }
        }
    }
}