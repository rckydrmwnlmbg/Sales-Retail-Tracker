package com.example.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeTint
import androidx.compose.foundation.isSystemInDarkTheme

object BlurCapabilityManager {

    enum class BlurLevel {
        FULL,    // Flagship — full Haze blur 20dp
        REDUCED, // Mid-range — blur 10dp
        MINIMAL, // Low-end — blur 4dp
        NONE     // Very low-end — simulasi tanpa blur
    }

    fun getBlurLevel(context: Context): BlurLevel {
        return try {
            val activityManager = context.getSystemService(
                Context.ACTIVITY_SERVICE
            ) as? ActivityManager ?: return BlurLevel.MINIMAL

            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val isLowRam = activityManager.isLowRamDevice
            val api = Build.VERSION.SDK_INT

            when {
                api >= Build.VERSION_CODES.S -> BlurLevel.FULL
                totalRamGb >= 4.0 && !isLowRam -> BlurLevel.REDUCED
                else -> BlurLevel.MINIMAL
            }
        } catch (e: Exception) {
            BlurLevel.MINIMAL
        }
    }

    fun getBlurRadius(level: BlurLevel): Dp = when (level) {
        BlurLevel.FULL    -> 20.dp
        BlurLevel.REDUCED -> 10.dp
        BlurLevel.MINIMAL -> 4.dp
        BlurLevel.NONE    -> 0.dp
    }

    fun getCardAlpha(level: BlurLevel): Float = when (level) {
        BlurLevel.FULL    -> 0.08f
        BlurLevel.REDUCED -> 0.15f
        BlurLevel.MINIMAL -> 0.25f
        BlurLevel.NONE    -> 0.40f
    }
}

val LocalBlurLevel = compositionLocalOf {
    BlurCapabilityManager.BlurLevel.FULL
}

@Composable
fun AdaptiveGlassCard(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val blurLevel = remember { BlurCapabilityManager.getBlurLevel(context) }
    val blurRadius = BlurCapabilityManager.getBlurRadius(blurLevel)
    val cardAlpha = BlurCapabilityManager.getCardAlpha(blurLevel)

    val cardModifier = modifier
        .clip(RoundedCornerShape(16.dp))
        .let {
            it.hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = if (isSystemInDarkTheme()) Color(0xFF04060C).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.6f),
                    tint = HazeTint(if (isSystemInDarkTheme()) Color(0xFF04060C).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.6f)),
                    blurRadius = blurRadius
                )
            )
        }
        .border(
            width = 1.dp,
            color = if (isSystemInDarkTheme()) Color.White.copy(0.12f) else Color.Black.copy(0.06f),
            shape = RoundedCornerShape(16.dp)
        )

    Box(modifier = cardModifier) {
        // Shimmer highlight — garis putih di tepi atas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            androidx.compose.material3.MaterialTheme.colorScheme.outline,
                            androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant,
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}
