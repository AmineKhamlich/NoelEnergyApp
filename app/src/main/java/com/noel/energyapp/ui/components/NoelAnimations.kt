package com.noel.energyapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val LocalNoelAnimationsEnabled = staticCompositionLocalOf { true }

@Composable
fun Modifier.noelReveal(
    delayMillis: Int = 0,
    offsetY: Dp = 18.dp,
    durationMillis: Int = 320
): Modifier {
    val animationsEnabled = LocalNoelAnimationsEnabled.current
    var visible by remember { mutableStateOf(!animationsEnabled) }

    LaunchedEffect(animationsEnabled, delayMillis) {
        if (animationsEnabled) {
            visible = false
            delay(delayMillis.toLong())
            visible = true
        } else {
            visible = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (animationsEnabled) tween(durationMillis) else snap(),
        label = "NoelRevealAlpha"
    )
    val yOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else offsetY,
        animationSpec = if (animationsEnabled) tween(durationMillis) else snap(),
        label = "NoelRevealOffset"
    )
    val density = LocalDensity.current
    val yOffsetPx = with(density) { yOffset.toPx() }

    return this.graphicsLayer {
        this.alpha = alpha
        translationY = yOffsetPx
    }
}
