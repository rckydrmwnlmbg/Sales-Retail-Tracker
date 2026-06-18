package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import com.example.LocalHazeState

@Composable
fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp
): Modifier {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val borderColors = if (isDark) {
        listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.0f))
    } else {
        listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.2f))
    }

    return this
        .clip(shape)
        .hazeChild(
            state = LocalHazeState.current,
            style = dev.chrisbanes.haze.HazeStyle(
                backgroundColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f),
                tint = dev.chrisbanes.haze.HazeTint(color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f)),
                blurRadius = 4.dp
            )
        )
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.05f else 0.4f),
                    Color.White.copy(alpha = if (isDark) 0.02f else 0.1f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = borderColors,
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = shape
        )
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.glassmorphism()
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassmorphicCard(modifier, content)
}
