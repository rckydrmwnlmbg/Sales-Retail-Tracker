package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using Default sans-serif for now, approximating Inter.
val InterLike = FontFamily.SansSerif

val Typography =
  Typography(
    displayLarge = TextStyle( // display-xl
        fontFamily = InterLike,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp, // ~1.2
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle( // data-lg
        fontFamily = InterLike,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 28.sp, // 1.0
        fontFeatureSettings = "tnum"
    ),
    displaySmall = TextStyle( // data-md
        fontFamily = InterLike,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 20.sp, // 1.0
        fontFeatureSettings = "tnum"
    ),
    headlineLarge = TextStyle( // display-lg
        fontFamily = InterLike,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp, // ~1.25
    ),
    titleLarge = TextStyle( // heading-md
        fontFamily = InterLike,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp, // ~1.3
    ),
    titleMedium = TextStyle( // heading-sm
        fontFamily = InterLike,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp, // ~1.4
    ),
    bodyLarge = TextStyle( // body-md
        fontFamily = InterLike,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp, // 1.5
    ),
    bodyMedium = TextStyle( // body-sm
        fontFamily = InterLike,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp, // 1.5
    ),
    labelSmall = TextStyle( // label
        fontFamily = InterLike,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 13.sp, // ~1.2
    )
  )
