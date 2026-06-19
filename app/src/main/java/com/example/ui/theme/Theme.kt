package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// DARK COLOR SCHEME
private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF060810),
    surface = Color(0x0FFFFFFF),
    surfaceVariant = Color(0x18FFFFFF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xCCFFFFFF),
    onSurfaceVariant = Color(0x80FFFFFF),

    // Electric Blue — vibrant untuk dark background
    primary = Color(0xFF60B4FF),
    primaryContainer = Color(0x2660B4FF),
    onPrimary = Color(0xFF060810),

    secondary = Color(0xFF4ADE80),   // success mint
    tertiary = Color(0xFFFFD93D),    // warning amber
    error = Color(0xFFFF6B6B),       // danger red

    outline = Color(0x26FFFFFF),
    outlineVariant = Color(0x14FFFFFF)
)

// LIGHT COLOR SCHEME
private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF0F4FF),
    surface = Color(0xB3FFFFFF),
    surfaceVariant = Color(0x80FFFFFF),
    onBackground = Color(0xFF080C1A),
    onSurface = Color(0xCC080C1A),
    onSurfaceVariant = Color(0x80080C1A),

    // Royal Blue — lebih dalam untuk light background
    primary = Color(0xFF1565C0),
    primaryContainer = Color(0x201565C0),
    onPrimary = Color(0xFFFFFFFF),

    secondary = Color(0xFF166534),   // dark green
    tertiary = Color(0xFF854D0E),    // dark amber
    error = Color(0xFF991B1B),       // dark red

    outline = Color(0x26080C1A),
    outlineVariant = Color(0x14080C1A)
)

@Composable
fun MyApplicationTheme(
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        dynamicDarkColorScheme(context)
      }
      else -> DarkColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

