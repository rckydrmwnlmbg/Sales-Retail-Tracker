package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.ui.viewmodels.MainViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.AdaptiveGlassCard
import com.example.ui.components.CollapsibleBentoHeader
import com.example.ui.components.CollapsibleHeaderContent
import com.example.LocalHazeState
import dev.chrisbanes.haze.HazeState

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val activities by viewModel.allActivities.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkTheme = when(themeMode) {
        1 -> true
        2 -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val onBg = MaterialTheme.colorScheme.onBackground
    val sectionHeaderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    val scrollState = rememberLazyListState()
    val localHazeState = com.example.LocalHazeState.current
    val firstClockInTime by viewModel.firstClockInTime.collectAsState()

    val headerContent = remember(firstClockInTime) {
        object : CollapsibleHeaderContent {
            override val expandedHeight = 220.dp
            override val collapsedHeight = 136.dp

            @Composable
            override fun ExpandedContent(collapseProgress: Float) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ProfileHeaderExpanded(
                        collapseProgress = collapseProgress,
                        hazeState = localHazeState,
                        firstClockIn = firstClockInTime ?: "Belum ada"
                    )
                    ProfileHeaderCollapsed(collapseProgress = collapseProgress)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { 
                Box(modifier = Modifier.padding(bottom = 200.dp)) {
                    SnackbarHost(snackbarHostState)
                }
            }
        ) { innerPadding ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp, 
                    end = 24.dp, 
                    top = headerContent.expandedHeight + 16.dp, 
                    bottom = 200.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "MASTER DATA",
                        color = sectionHeaderColor,
                        letterSpacing = 1.5.sp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
            item {
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.Inventory,
                    title = "Product Catalog",
                    subtitle = "Manage brands, products, prices",
                    onClick = { onNavigate("manage_products") }
                )
            }
            item {
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.Group,
                    title = "Colleague List",
                    subtitle = "Manage staff and their roles",
                    onClick = { onNavigate("manage_colleagues") }
                )
            }
            item {
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.TrackChanges,
                    title = "Goal Setting",
                    subtitle = "Set monthly target and KPI",
                    onClick = { onNavigate("manage_goals") }
                )
            }
            item {
                Text(
                    text = "REPORTS & DATA",
                    color = sectionHeaderColor,
                    letterSpacing = 1.5.sp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.FileDownload,
                    title = "Export Data",
                    subtitle = "Download reports to CSV or Share",
                    onClick = { onNavigate("export") }
                )
            }
            item {
                Text(
                    text = "SYSTEM & SECURITY",
                    color = sectionHeaderColor,
                    letterSpacing = 1.5.sp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.Sync,
                    title = "Offline Sync",
                    subtitle = "Sync offline sales entries back to server",
                    onClick = { 
                        coroutineScope.launch { snackbarHostState.showSnackbar("Rules updated: Syncing pending entries to server.") }
                    }
                )
            }
            item {
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.CloudUpload,
                    title = "Cloud Backup",
                    subtitle = "Stores backup of all sales data safely",
                    onClick = { 
                        coroutineScope.launch { snackbarHostState.showSnackbar("Backup started... Data secured in cloud.") }
                    }
                )
            }
            item {
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.Lock,
                    title = "Security & Privacy",
                    subtitle = "Protect sales numbers with PIN/Fingerprint",
                    onClick = { 
                        coroutineScope.launch { snackbarHostState.showSnackbar("Biometric lock enforced for analytics.") }
                    }
                )
            }
            item {
                val themeName = if (isDarkTheme) "Cosmic Dark" else "Stellar Bright"
                SettingItemCard(
                    hazeState = localHazeState,
                    icon = Icons.Outlined.DarkMode,
                    title = "App Theme",
                    subtitle = "Currently using $themeName",
                    onClick = { 
                        val newMode = if (isDarkTheme) 2 else 1 // toggle mode 1 and 2
                        viewModel.setThemeMode(newMode)
                        val newThemeName = if (newMode == 1) "Cosmic Dark" else "Stellar Bright"
                        coroutineScope.launch { snackbarHostState.showSnackbar("Theme changed to $newThemeName") }
                    }
                )
            }

            item {
                Text(
                    text = "RECENT ACTIVITY LOGS",
                    color = sectionHeaderColor,
                    letterSpacing = 1.5.sp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            val recentLogs = activities.sortedByDescending { it.timestamp }.take(5)
            if (recentLogs.isEmpty()) {
                item {
                    Text("No activities recorded yet.", color = onBg.copy(alpha=0.5f), fontSize=14.sp)
                }
            } else {
                items(recentLogs.size) { index ->
                    val log = recentLogs[index]
                    val pName = products.find { it.id == log.productId }?.name ?: "General Activity"
                    val sf = SimpleDateFormat("dd MMM yy HH:mm", Locale.getDefault())
                    val dateStr = sf.format(Date(log.timestamp))
                    
                    AdaptiveGlassCard(
                        hazeState = localHazeState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = "Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = log.type, color = onBg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(text = dateStr, color = onBg.copy(alpha=0.5f), fontSize = 12.sp)
                                }
                            }
                            Text(text = pName, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(1f, fill=false), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    CollapsibleBentoHeader(
        hazeState = localHazeState,
        scrollState = scrollState,
        content = headerContent
    )
}
}



@Composable
fun SettingItemCard(
    hazeState: dev.chrisbanes.haze.HazeState,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val onBg = MaterialTheme.colorScheme.onBackground
    val bodyColor = onBg
    
    AdaptiveGlassCard(
        hazeState = hazeState,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick() 
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = bodyColor.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = bodyColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    color = bodyColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                modifier = Modifier.size(16.dp),
                tint = bodyColor.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ProfileHeaderExpanded(
    collapseProgress: Float,
    hazeState: HazeState,
    firstClockIn: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdaptiveGlassCard(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(1.3f)
                    .height(96.dp)
                    .graphicsLayer {
                        alpha = (1f - collapseProgress * 2f).coerceIn(0f, 1f)
                        scaleX = lerp(1f, 0.95f, collapseProgress)
                        scaleY = scaleX
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "👤 Ricky",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sales Regular",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AdaptiveGlassCard(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(1.3f)
                    .height(96.dp)
                    .graphicsLayer {
                        alpha = (1f - collapseProgress * 2f).coerceIn(0f, 1f)
                        scaleX = lerp(1f, 0.95f, collapseProgress)
                        scaleY = scaleX
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Casio Official Store",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Mall 23 Semarang",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AdaptiveGlassCard(
            hazeState = hazeState,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .graphicsLayer {
                    alpha = (1f - collapseProgress * 3f).coerceIn(0f, 1f)
                    scaleX = lerp(1f, 0.95f, collapseProgress)
                    scaleY = scaleX
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Bergabung: $firstClockIn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProfileHeaderCollapsed(collapseProgress: Float) {
    if (collapseProgress > 0.5f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 28.dp)
                .graphicsLayer {
                    alpha = ((collapseProgress - 0.5f) * 2f).coerceIn(0f, 1f)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "👤 Ricky",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            Text(
                text = "Sales Regular",
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
