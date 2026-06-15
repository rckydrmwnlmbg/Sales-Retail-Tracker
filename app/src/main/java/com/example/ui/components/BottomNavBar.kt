package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

class NavBarShape(
    private val cornerRadiusDp: Float,
    private val cutoutWidthDp: Float,
    private val cutoutDepthDp: Float,
    private val smoothingDp: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(Path().apply {
            val cornerRadius = with(density) { cornerRadiusDp.dp.toPx() }
            val cW = with(density) { cutoutWidthDp.dp.toPx() }
            val cD = with(density) { cutoutDepthDp.dp.toPx() }
            val smoothing = with(density) { smoothingDp.dp.toPx() }
            val cX = size.width / 2f
            val h = size.height
            val w = size.width

            moveTo(0f, cornerRadius)
            quadraticBezierTo(0f, 0f, cornerRadius, 0f)

            lineTo(cX - cW / 2, 0f)
            cubicTo(
                cX - cW / 2 + smoothing, 0f,
                cX - smoothing, cD,
                cX, cD
            )
            cubicTo(
                cX + smoothing, cD,
                cX + cW / 2 - smoothing, 0f,
                cX + cW / 2, 0f
            )

            lineTo(w - cornerRadius, 0f)
            quadraticBezierTo(w, 0f, w, cornerRadius)
            lineTo(w, h - cornerRadius)
            quadraticBezierTo(w, h, w - cornerRadius, h)
            lineTo(cornerRadius, h)
            quadraticBezierTo(0f, h, 0f, h - cornerRadius)
            close()
        })
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogClick: () -> Unit = {},
    hazeState: HazeState? = null
) {
    val haptic = LocalHapticFeedback.current

    val items: List<Pair<String, ImageVector>> = listOf(
        Pair("home", Icons.Filled.Home),
        Pair("analytics", Icons.Filled.BarChart),
        Pair("log", Icons.Filled.Add), // handled separately
        Pair("journal", Icons.AutoMirrored.Outlined.List),
        Pair("profile", Icons.Filled.Person)
    )

    val labels = listOf("Home", "Analytics", "Log", "Journal", "Profile")
    val routes = listOf(Screen.Home.route, Screen.Analytics.route, Screen.Log.route, Screen.Journal.route, Screen.Profile.route)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp) 
    ) {
        val navBarShape = NavBarShape(
            cornerRadiusDp = 32f, 
            cutoutWidthDp = 100f, 
            cutoutDepthDp = 44f, 
            smoothingDp = 30f
        )
        
        // 1. NavBar Area
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                .fillMaxWidth()
                .height(72.dp)
                .clip(navBarShape)
                .let {
                    if (hazeState != null) {
                        it.hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = Color.Transparent,
                                tint = HazeTint(Color.Black.copy(alpha = 0.10f)),
                                blurRadius = 16.dp
                            )
                        )
                    } else {
                        it.background(MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(0.4f),
                            MaterialTheme.colorScheme.outline.copy(0.05f)
                        )
                    ),
                    shape = navBarShape
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Items
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    items.take(2).forEachIndexed { index, item ->
                        NavItem(
                            item = item,
                            label = labels[index],
                            isActive = currentRoute == routes[index],
                            onClick = { onNavigate(routes[index]) }
                        )
                    }
                }
                
                // Center spacer for cutout
                Spacer(modifier = Modifier.width(90.dp))
                
                // Right Items
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    items.drop(3).forEachIndexed { index, item ->
                        val realIndex = index + 3
                        NavItem(
                            item = item,
                            label = labels[realIndex],
                            isActive = currentRoute == routes[realIndex],
                            onClick = { onNavigate(routes[realIndex]) }
                        )
                    }
                }
            }
        }

        // 2. Floating FAB area
        // Top of navbar is at Y = 130 - 12 - 72 = 46.
        // Cutout bottom is at Y = 46 + 44 = 90.
        // We want the inner FAB (60) bottom at ~86, so center is at 56.
        // If center is 56, the outer glow (96) top is 56 - 48 = 8. (Safely inside 130 height bounds).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 8.dp)
        ) {
            // Layer 1 (Outer glow)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .align(Alignment.Center)
            )
            // Layer 2 (Middle glow)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .align(Alignment.Center)
            )
            // Main FAB
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLogClick()
                        }
                    )
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Log",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    item: Pair<String, ImageVector>,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = Modifier
            .height(64.dp)
            .width(64.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick() 
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.second,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        } else {
            Icon(
                imageVector = item.second,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
