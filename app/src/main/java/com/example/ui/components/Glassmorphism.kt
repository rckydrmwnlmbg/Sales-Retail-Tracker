package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import com.example.LocalHazeState

@Composable
fun Modifier.glassmorphism(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.dp
): Modifier {
    val baseAlpha = 0.08f
    val shadowColor = Color.Black.copy(alpha = 0.37f)

    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || MaterialTheme.colorScheme.background == Color(0xFF060810)
    return this
        .shadow(elevation = 12.dp, shape = shape, spotColor = shadowColor, ambientColor = shadowColor)
        .clip(shape)
        .hazeEffect(
            state = LocalHazeState.current,
            style = HazeStyle(
                backgroundColor = Color.White.copy(alpha = baseAlpha * 1.5f),
                tint = HazeTint(Color.White.copy(alpha = baseAlpha * 1.5f)),
                blurRadius = 24.dp
            )
        )
        .border(
            width = borderWidth,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
            shape = shape
        )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(16.dp))
            .let {
                it.hazeEffect(
                    state = LocalHazeState.current,
                    style = HazeStyle(
                        backgroundColor = Color.White.copy(alpha = 0.15f),
                        tint = HazeTint(Color.White.copy(alpha = 0.15f)),
                        blurRadius = 24.dp
                    )
                )
            }
            .border(
                width = 1.dp,
                color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color.White.copy(0.12f) else Color.Black.copy(0.06f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun GlassmorphicCard( // Legacy, pointing to GlassCard
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(modifier = modifier) {
        Box(content = content)
    }
}
