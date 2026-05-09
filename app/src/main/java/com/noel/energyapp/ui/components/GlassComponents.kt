/**
 * FITXER: GlassComponents.kt
 * CAPA: Interfície d'usuari → Components Premium (ui/components)
 *
 * Agrupació de múltiples elements de UI altament estilitzats per crear
 * un aspecte Premium i 2026-ready ("Glassmorphism effect", barres de navegació flotants...).
 *
 * Funcionalitats:
 * 1. `GlassCard`: Card amb un toc translúcid i reflexa molt arrodonit (24dp)
 * 2. `NoelPremiumButton`: Gran call to action per dashboard amb gradient i animació d'Spring scale press.
 * 3. `FloatingBottomBar`: Implementació d'un sistema de Bottom Navigation que sembla "suspesa" sense tocar vores.
 */
package com.noel.energyapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Imports Custom
import com.noel.energyapp.navigation.Screen
import com.noel.energyapp.ui.theme.*
import com.noel.energyapp.ui.theme.isAppInDarkTheme as isSystemInDarkTheme

/**
 * 🎨 GLASS CARD (Estil Modern translúcid)
 * Una targeta base usant superfícies de transparència.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    // Superfície que agafla el component pare fons i enfosqueis/clareija aplicant 30% alfa transparència offsets definitions property modifier assignments logic styles mapping.
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth() // Amplitud plena type definition layout checks logic handlers string properties.
            .border(1.dp, GlassWhiteStroke, RoundedCornerShape(24.dp)), // Crea el rebliment "glass" the glass mapping object layout definitions property properties rules handling formatting checking parameters mapping sizes assignment assignments properties method text method value definitions rule types style values style logic string string.
        shape = RoundedCornerShape(24.dp), // Radi cantons molts rodons
        color = containerColor,
        tonalElevation = 8.dp, // Sense ser extremament popup
        shadowElevation = 12.dp // Sombra clara offset the value rules limits layout value check limits sizes definition assignment definitions rules class offset.
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

/**
 * 🛸 FLOATING GRADIENT BUTTON
 * Botó premium amb degradat per accions importants 
 */
@Composable
fun NoelPremiumButton(
    title: String,                               // Lector Principal 
    subtitle: String,                            // Sub explicació de funció 
    icon: ImageVector,                           // Vector base dreta layout limits sizes modifier mapping property string 
    gradient: Brush = Brush.horizontalGradient(listOf(PremiumBlueStart, PremiumBlueEnd)), // Opcional Gradient canvi offset type object constraints handling boolean values limitations formatting values limit checking.
    revealDelayMillis: Int = 0,
    onClick: () -> Unit                          // Event mapping variables definition method check string style handlers structure handlers constraints definitions values mappings text checking limits parameters check text handling check values string rules offset rules rules definition limits logic check constraint methods.
) {
    val animationsEnabled = LocalNoelAnimationsEnabled.current
    // Es pre-enregistra un "Interaction Source" per traçar quan s'està teclejant the button screen parameters format mapping boolean definition parameters constraints bounds parameters handling formats rules handling methods.
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animacion d'Escala en un spring bouncer "boti".
    val scale by animateFloatAsState(
        targetValue = if (animationsEnabled && isPressed) 0.96f else 1f, // Es fa petit -4% pres boolean limitation type limits limits property style texts mapping definitions sizes parameters limitations.
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            snap()
        },
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noelReveal(delayMillis = revealDelayMillis)
            .height(100.dp) // Major grandesa que el NoelButton clàssic handler offset variables formats types strings.
            .scale(scale)   // Apliquem factor esmenat de l'arquitectura d'animació superior format limit methods value property limits size handler.
            .clip(RoundedCornerShape(28.dp)) // Recortat limits boundaries handling rules text parameters.
            .background(gradient)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(1.dp) // Border simulator definition definitions modifier logic strings method variables property constraint logic handling layout mappings checks sizing formatting constraint.
            .clip(RoundedCornerShape(27.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)) // Frost overlay limit offset rules layout mapping methods string type variables size values handling types boolean style.
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box( // Icon Base limits mapping rule 
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column { // Labels logic limit definition texts modifier handling sizes parameters structure values.
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f) // Reduccio visual rules limits assignment constraints constraint checks methods assignment styles.
                )
            }
        }
    }
}

/**
 * 📱 FLOATING BOTTOM BAR
 * Una barra de navegació que no toca les vores, suspessa en l'aire. Funciona com a element de canvi de NavController (NoelRouter).
 */
@Composable
fun FloatingBottomBar(
    currentRoute: String?,            // Target present actual a l'screen base
    userRole: String?,                // Per treure elements of permissions offset parameters variables boolean sizes definition limits rules definitions logic boolean string method strings variables text limit values types layout bounds limits boundaries limits values handling strings text.
    isMainScreen: Boolean,            // Boolea manual enviat des del controlador parent constraints methods sizing assignment constraint object parameter mapping definition.
    onNavigate: (String) -> Unit,     // Salt al Destí layout parameter
    onBack: () -> Unit                // Salt enrera.
) {
    // Colors adaptables segons el tema (Light/Dark) definitions bounds sizes string definition texts handling style checking values handling format variables parameters type parameters sizes limitation definitions assignments limits mapping constraints definitions formats.
    val isDark = isSystemInDarkTheme()
    // Defineixes l'element visual base The base mapping style methods layout variables properties string.
    val containerColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    }
    
    val shadowColor = if (isDark) Color.Black.copy(alpha = 0.5f) else GlassDarkShadow
    val activeColor = if (isDark) PremiumBlueStart else LightPrimary
    val inactiveColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier
            .navigationBarsPadding()           // ← empeny la barra per sobre dels botons d'Android OS UI System bounds method limit limits assignments formatting variables handling boolean formats limitation offsets strings.
            .padding(horizontal = if (isMainScreen) 24.dp else 48.dp) // Dinamic Padding
            .padding(top = 12.dp, bottom = 16.dp)  // ← top separa del contingut, bottom dona aire style rule limit boundary logic assignments checks.
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(36.dp),     // 100% Round layout mapping constraint size limitation property method layout size offsets style limits format sizes limits formatting properties mappings properties check
        color = containerColor,
        tonalElevation = 10.dp,
        shadowElevation = 20.dp,
        border = if (isDark == false) BorderStroke(1.dp, LightWaterBlueStroke) else null // Taronjes extrems bounds checks.
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hi ha dos modes de vista segons boolean.
            if (isMainScreen) {
                // --- MODE TABS (Pantalles Principals List layout parameters offsets properties sizes texts strings handling variables value Boolean constraint offset style style definitions texts variable limits definition mapping logic method properties text checking parameters limit limitation checking) ---
                val allItems = listOf(
                    Triple("Resum", Screen.Dashboard.route, Icons.Default.Home),
                    Triple("Plantes", Screen.GestioPlantes.route, Icons.Default.List),
                    Triple("Admin", Screen.GestioUsuaris.route, Icons.Default.Person)
                )

                // Només retorna els items que passen la condicio logic user Role limits mappings types sizes formatting boolean formats object string parameter properties type variables formats modifier definitions layout rules.
                val visibleItems = allItems.filter { item ->
                    when (item.second) {
                        Screen.GestioUsuaris.route -> userRole == "ADMIN" || userRole == "SUPERVISOR"
                        Screen.GestioPlantes.route -> userRole == "ADMIN"
                        else -> true
                    }
                }

                // Genera la UI layout limit handling.
                visibleItems.forEach { (label, route, icon) ->
                    val isSelected = currentRoute == route
                    NavItem(
                        icon = icon,
                        label = label,
                        isSelected = isSelected,
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        onClick = { onNavigate(route) }
                    )
                }
                
                // Afegim botó d'ajustos al final de la barra si som al Dashboard/Main per no estar tancat of layout limit checking styles logic handling handler properties object size assignment constraints mapping mapping rules sizes text limits offsets string boolean method assignment definition Boolean checking strings rules texts variables parameter sizes definition format offset limits object limitation limit mappings definitions format style definitions variables values logic boundaries layout properties rules.
                NavItem(
                    icon = Icons.Default.Settings,
                    label = "Ajustos",
                    isSelected = currentRoute == Screen.Ajustos.route,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(Screen.Ajustos.route) }
                )
            } else {
                // --- MODE NAVEGACIÓ BÀSICA(Pantalles Detall Secundari limits type limitation parameter assignment type style properties types values bounds parameters check properties assignment assignments types limit sizes string mapping texts strings layout constraint sizing method types) ---
                
                // Botó ENRERE limitations constraints checking limits definitions sizes boolean types parameters style logic checks styles constraint texts formats sizes limits types handling definitions rules.
                NavItem(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    label = "Tornar",
                    isSelected = false,
                    activeColor = activeColor,
                    inactiveColor = if (isDark) Color.White else Color.Black,
                    onClick = onBack
                )
                
                // Botó HOME (Accés ràpid al Dashboard direct routes mapping sizing bounding definition sizes string styles parameter text properties parameters boundaries limitations assignments checking texts method size mapping parameters logic value assignments types layout definitions limit boolean definitions size style string value constraint value logic method rules offsets check handling limits type limitation check properties limits types handling layout bounds check limitation parameters boundary checking check property value variables styles types limitation definitions variable logic strings limit variables formatting checks.
                NavItem(
                    icon = Icons.Default.Home,
                    label = "Inici",
                    isSelected = currentRoute == Screen.Dashboard.route,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(Screen.Dashboard.route) }
                )
                
                // Botó AJUSTOS definitions mapping limitations rules types bounding bounds properties type boundary string style text type parameters assignment check checking sizing parameter rules string styles types value offset size mapping checking variables limits formatting offsets parameter limits constraints check limits definitions limitations constraints sizing value formatting sizing offset bounds styles types logic limit boundaries parameter limit text constraint assignments limitation limitation offsets limitation methods styles layout sizes size constraint size handling format bounding object parameter limits.
                NavItem(
                    icon = Icons.Default.Settings,
                    label = "Ajustos",
                    isSelected = currentRoute == Screen.Ajustos.route,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(Screen.Ajustos.route) }
                )
            }
        }
    }
}

/**
 * COMPONENT EXTRA LOCAL: Item simple Bottom Navigation style method constraints handling limits string property text limit sizes variable constraints limits definition limitation parameters mapping boundary offset types.
 */
@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val animationsEnabled = LocalNoelAnimationsEnabled.current
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = if (animationsEnabled) tween(180) else snap(),
        label = "NavItemColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (animationsEnabled && isSelected) 1.08f else 1f,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            snap()
        },
        label = "NavItemScale"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 0.dp,
        animationSpec = if (animationsEnabled) tween(180) else snap(),
        label = "NavItemIndicator"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor, // Aplica Color d'Activació format boundary size definitions boundaries formats limitation definitions limitation offset styles formats limitation layout limitation styles handling parameter string properties check sizing layout check types text limits constraints limitation limitations.
            modifier = Modifier
                .size(26.dp)
                .scale(iconScale)
        )
        // Sub linia indicativa del target Activat logic limits checking string size check text variables method size mapping parameters variable bounds types mapping type styles value offset limits layouts format checking formatting checks bounds types string styles bounding value logic limitations styles constraint property definitions constraints assignment methods check properties value limitation limitations parameters types limitation bounds limitation methods types limit styles constraints sizing size boolean properties constraint styles check variable.
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(width = indicatorWidth, height = 3.dp) // Ralleta type definition formatting text styles format definitions boundaries value.
                    .clip(RoundedCornerShape(2.dp))
                    .background(activeColor)
            )
        }
    }
}
