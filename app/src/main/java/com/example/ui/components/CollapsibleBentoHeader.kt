package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeTint
import androidx.compose.foundation.isSystemInDarkTheme

interface CollapsibleHeaderContent {
    val expandedHeight: Dp
    val collapsedHeight: Dp
    
    @Composable
    fun ExpandedContent(collapseProgress: Float)
}

@Composable
fun CollapsibleBentoHeader(
    hazeState: HazeState,
    scrollState: LazyListState,
    content: CollapsibleHeaderContent
) {
    // Hitung collapse progress dari scroll offset
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
    // 0f = fully expanded, 1f = fully collapsed

    val headerHeight = lerp(
        start = content.expandedHeight,
        stop = content.collapsedHeight,
        fraction = collapseProgress
    )

    // Warna solid electrical blue yang agak gelap berpadu dengan alpha
    val darkElectricBlue = Color(0xFF0A2240)
    val backgroundAlpha = (collapseProgress * 0.95f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(darkElectricBlue.copy(alpha = backgroundAlpha))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(0.20f),
                        Color.White.copy(0.05f)
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)
            )
    ) {
        content.ExpandedContent(collapseProgress)
    }
}
