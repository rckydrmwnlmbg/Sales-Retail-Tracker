package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlin.random.Random

import androidx.compose.ui.layout.onSizeChanged

class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var life: Float = 1f,
    val size: Float
)

@Composable
fun ParticleEffect(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    onComplete: () -> Unit
) {
    if (!trigger) return

    val particles = remember { mutableStateListOf<Particle>() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var timeMSs by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(trigger, size) {
        if (trigger && size != IntSize.Zero && particles.isEmpty()) {
            val colors = listOf(Color(0xFFE53935), Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFFFDD835), Color(0xFF8E24AA))
            // Generate particles from center bottom initially, then let them burst
            for (i in 0..100) {
                particles.add(
                    Particle(
                        x = size.width / 2f,
                        y = size.height / 2f,
                        vx = Random.nextFloat() * 1000f - 500f,
                        vy = Random.nextFloat() * 1000f - 800f,
                        color = colors.random(),
                        size = Random.nextFloat() * 8f + 8f
                    )
                )
            }
        }
    }

    LaunchedEffect(particles.size) {
        if (particles.isNotEmpty()) {
            var timeMs = 0f
            while (timeMs < 2000f) {
                withFrameNanos { 
                    timeMs += 16f
                    for (p in particles) {
                        p.x += p.vx * 0.016f
                        p.y += p.vy * 0.016f
                        p.vy += 1500f * 0.016f // Gravity
                        p.life -= 0.016f / 2f // Fade out
                    }
                    timeMSs = timeMs
                }
            }
            particles.clear()
            onComplete()
        }
    }

    Canvas(modifier = modifier
        .fillMaxSize()
        .onSizeChanged { size = it }
    ) {
        val currentT = timeMSs // read to trigger recomposition
        for (p in particles) {
            if (p.life > 0) {
                drawCircle(
                    color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}
