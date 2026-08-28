package com.example.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.WarningGold

fun Modifier.glowBorder(
    color: Color = NeonGreen,
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    alpha: Float = 0.25f
): Modifier {
    return this.border(
        width = strokeWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                color.copy(alpha = alpha * 1.5f),
                color.copy(alpha = alpha * 0.4f),
                color.copy(alpha = alpha * 1.2f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
}

@Composable
fun PulsingStatusDot(
    modifier: Modifier = Modifier,
    isOnline: Boolean = true,
    isConnecting: Boolean = false,
    size: Dp = 10.dp
) {
    val activeColor = when {
        isConnecting -> WarningGold
        isOnline -> NeonGreen
        else -> AlertRed
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOnline || isConnecting) 1.8f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(size * 2),
        contentAlignment = Alignment.Center
    ) {
        if (isOnline || isConnecting) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = pulseAlpha))
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(activeColor)
        )
    }
}
