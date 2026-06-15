package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Colors
val CasioRed = Color(0xFFD62828)
val CasioRedGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF5252), // Lighter red
        Color(0xFFD62828), // Casio Red
        Color(0xFF991B1B)  // Darker red for depth
    )
)
val CasioBlack = Color(0xFF1A1A1A)
val SurfaceWhite = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF5F5F7)
val BorderLight = Color(0xFFE8E8EC)

// Secondary/Status Colors
val SuccessGreen = Color(0xFF22C55E)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)

// Neutral Colors
val Neutral400 = Color(0xFF9CA3AF)
val Neutral600 = Color(0xFF4B5563)
val Neutral800 = Color(0xFF1F2937)

// Attribution Colors
val ActualBlue = Color(0xFF2563EB)
val CreditedPurple = Color(0xFF7C3AED)
val GivenCoral = Color(0xFFF97316)
val ReceivedTeal = Color(0xFF0D9488)

// Dark Theme Colors
val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val SurfaceElevatedDark = Color(0xFF334155)
val BorderDark = Color(0xFF334155)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val AccentDark = Color(0xFFEF4444)
