package com.example.ui.components

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeChild

object BlurCapabilityManager {
    enum class BlurLevel { FULL, REDUCED, MINIMAL, NONE }

    fun getBlurLevel(context: Context): BlurLevel {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            
            val totalRamGb = memInfo.totalMem / (1024 * 1024 * 1024.0)
            
            when {
                memInfo.lowMemory -> BlurLevel.NONE
                totalRamGb >= 6.0 -> BlurLevel.FULL
                totalRamGb >= 4.0 -> BlurLevel.REDUCED
                totalRamGb >= 2.0 -> BlurLevel.MINIMAL
                else -> BlurLevel.NONE
            }
        } catch (e: Exception) {
            BlurLevel.NONE
        }
    }
}

val LocalBlurLevel = compositionLocalOf { BlurCapabilityManager.BlurLevel.FULL }

@Composable
fun AdaptiveBlurCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    AdaptiveGlassCard(modifier, content)
}

@Composable
fun AdaptiveGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val borderColors = if (isDark) {
        listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.0f))
    } else {
        listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.2f))
    }

    val cardModifier = modifier
        .clip(RoundedCornerShape(16.dp))
        .hazeChild(
            state = com.example.LocalHazeState.current,
            style = dev.chrisbanes.haze.HazeStyle(
                backgroundColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
                tint = dev.chrisbanes.haze.HazeTint(color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)),
                blurRadius = 48.dp
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
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = borderColors,
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            ),
            shape = RoundedCornerShape(16.dp)
        )

    Box(
        modifier = cardModifier,
        content = content
    )
}
