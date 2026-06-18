package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.example.LocalHazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

interface CollapsibleHeaderContent {
    val expandedHeight: Dp
    val collapsedHeight: Dp

    @Composable
    fun ExpandedContent(collapseProgress: Float)
}

@Composable
fun CollapsibleBentoHeader(
    hazeState: dev.chrisbanes.haze.HazeState? = null, // kept for backward compat, not used
    scrollState: LazyListState,
    content: CollapsibleHeaderContent
) {
    val collapseThreshold = 200f
    val scrollOffset by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex > 0) {
                collapseThreshold
            } else {
                scrollState.firstVisibleItemScrollOffset.toFloat()
                    .coerceAtMost(collapseThreshold)
            }
        }
    }
    val collapseProgress = scrollOffset / collapseThreshold

    val rawHeaderHeight = lerp(
        start = content.expandedHeight,
        stop = content.collapsedHeight,
        fraction = collapseProgress
    )

    val headerHeight by animateDpAsState(
        targetValue = rawHeaderHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "headerHeight"
    )

    val isDark = isSystemInDarkTheme()

    val shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .clip(shape)
            .hazeChild(
                state = LocalHazeState.current,
                style = HazeStyle(
                    backgroundColor = if (isDark)
                        Color(0xFF060810).copy(alpha = 0.55f)
                    else
                        Color.White.copy(alpha = 0.72f),
                    tint = HazeTint(
                        if (isDark)
                            Color.Black.copy(alpha = 0.15f)
                        else
                            Color.White.copy(alpha = 0.30f)
                    ),
                    blurRadius = 4.dp
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(if (isDark) 0.20f else 0.60f),
                        Color.White.copy(if (isDark) 0.05f else 0.20f)
                    )
                ),
                shape = shape
            )
    ) {
        content.ExpandedContent(collapseProgress)
    }
}
