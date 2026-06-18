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
import androidx.compose.material.icons.outlined.Edit
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



import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val supabaseUrl by viewModel.supabaseUrl.collectAsState()
    val supabaseKey by viewModel.supabaseKey.collectAsState()

    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkTheme = when(themeMode) {
        1 -> true
        2 -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val onBg = MaterialTheme.colorScheme.onBackground
    val sectionHeaderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    val scrollState = rememberLazyListState()
    
    val firstClockInTime by viewModel.firstClockInTime.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val jobTitle by viewModel.jobTitle.collectAsState()
    val workLocation by viewModel.workLocation.collectAsState()
    val shiftPagiTime by viewModel.shiftPagiTime.collectAsState()
    val shiftSiangTime by viewModel.shiftSiangTime.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

        val headerContent = remember(firstClockInTime, userName, jobTitle, workLocation, shiftPagiTime, shiftSiangTime) {
        object : CollapsibleHeaderContent {
            override val expandedHeight = 220.dp
            override val collapsedHeight = 136.dp

            @Composable
            override fun ExpandedContent(collapseProgress: Float) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ProfileHeaderExpanded(
                        collapseProgress = collapseProgress,
                        
                        firstClockIn = firstClockInTime ?: "Belum ada",
                        userName = userName,
                        jobTitle = jobTitle,
                        workLocation = workLocation
                    )
                    ProfileHeaderCollapsed(
                        collapseProgress = collapseProgress,
                        userName = userName,
                        jobTitle = jobTitle
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { 
                Box(modifier = Modifier.padding(bottom = 100.dp)) {
                    SnackbarHost(snackbarHostState)
                }
            }
        ) { innerPadding ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = com.example.ui.theme.AppSpacing.lg, 
                    end = com.example.ui.theme.AppSpacing.lg, 
                    top = headerContent.expandedHeight + com.example.ui.theme.AppSpacing.lg, 
                    bottom = 200.dp
                ),
                verticalArrangement = Arrangement.spacedBy(com.example.ui.theme.AppSpacing.xxl)
            ) {
                item {
                    Column {
                        Text(
                            text = "MASTER DATA",
                            color = sectionHeaderColor,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = com.example.ui.theme.AppSpacing.lg, bottom = com.example.ui.theme.AppSpacing.sm)
                        )
                    }
                }
            item {
                SettingItemCard(
                    
                    icon = Icons.Outlined.Inventory,
                    title = "Product Catalog",
                    subtitle = "Manage brands, products, prices",
                    onClick = { onNavigate("manage_products") }
                )
            }
            item {
                SettingItemCard(
                    
                    icon = Icons.Outlined.Group,
                    title = "Colleague List",
                    subtitle = "Manage staff and their roles",
                    onClick = { onNavigate("manage_colleagues") }
                )
            }
            item {
                SettingItemCard(
                    
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
                    
                    icon = Icons.Outlined.Sync,
                    title = "Offline Sync",
                    subtitle = "Sync offline sales entries back to server",
                    onClick = { 
                        val job = coroutineScope.launch { snackbarHostState.showSnackbar("Memulai sinkronisasi offline...") }
                        coroutineScope.launch { 
                            try {
                                val activities = viewModel.allActivities.value
                                com.example.logic.SupabaseSyncHelper.syncOfflineData(supabaseUrl, supabaseKey, activities)
                                job.cancel()
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(message = "Data offline berhasil disinkronisasi dengan Supabase", duration = androidx.compose.material3.SnackbarDuration.Long)
                            } catch (e: Exception) {
                                job.cancel()
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(message = "Gagal: ${e.message}", duration = androidx.compose.material3.SnackbarDuration.Long)
                            }
                        }
                    }
                )
            }
            item {
                SettingItemCard(
                    
                    icon = Icons.Outlined.CloudUpload,
                    title = "Cloud Backup",
                    subtitle = "Stores backup of all sales data safely",
                    onClick = { 
                        val job = coroutineScope.launch { snackbarHostState.showSnackbar("Membuat cadangan ke cloud...") }
                        coroutineScope.launch { 
                            try {
                                val products = viewModel.allProducts.value
                                val activities = viewModel.allActivities.value
                                val goals = viewModel.allGoals.value
                                val colleagues = viewModel.allColleagues.value
                                com.example.logic.SupabaseSyncHelper.backupDataToCloud(
                                    supabaseUrl, supabaseKey, products, activities, goals, colleagues
                                )
                                job.cancel()
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(message = "Backup selesai. Data telah diamankan di Supabase.", duration = androidx.compose.material3.SnackbarDuration.Long)
                            } catch (e: Exception) {
                                job.cancel()
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(message = "Gagal: ${e.message}", duration = androidx.compose.material3.SnackbarDuration.Long)
                            }
                        }
                    }
                )
            }
            // Security feature item removed since it was just a mock UI
            item {
                val themeName = if (isDarkTheme) "Cosmic Dark" else "Stellar Bright"
                SettingItemCard(
                    
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
                SettingItemCard(
                    
                    icon = Icons.Outlined.Edit,
                    title = "Edit Profil",
                    subtitle = "Ubah jabatan, lokasi, dan jam shift",
                    onClick = { showEditProfileDialog = true }
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
        
        scrollState = scrollState,
        content = headerContent
    )
    
    val openRouterApiKey by viewModel.openRouterApiKey.collectAsState()
    val supabaseUrl by viewModel.supabaseUrl.collectAsState()
    val supabaseKey by viewModel.supabaseKey.collectAsState()
    
    if (showEditProfileDialog) {
        EditProfileDialog(
            userName = userName,
            jobTitle = jobTitle,
            workLocation = workLocation,
            shiftPagi = shiftPagiTime,
            shiftSiang = shiftSiangTime,
            apiKey = openRouterApiKey,
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, job, loc, pagi, siang, key, supUrl, supKey ->
                viewModel.updateProfile(name, job, loc, pagi, siang)
                viewModel.updateOpenRouterApiKey(key)
                viewModel.updateSupabaseCredentials(supUrl, supKey)
                showEditProfileDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Profil berhasil diperbarui")
                }
            }
        )
    }
}
}



@Composable
fun SettingItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val onBg = MaterialTheme.colorScheme.onBackground
    val bodyColor = onBg
    
    AdaptiveGlassCard(
        
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
    firstClockIn: String,
    userName: String,
    jobTitle: String,
    workLocation: String
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
                        text = "👤 $userName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = jobTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AdaptiveGlassCard(
                
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
                        text = workLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AdaptiveGlassCard(
            
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
fun ProfileHeaderCollapsed(
    collapseProgress: Float,
    userName: String,
    jobTitle: String
) {
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
                text = "👤 $userName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            Text(
                text = jobTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    userName: String,
    jobTitle: String,
    workLocation: String,
    shiftPagi: String,
    shiftSiang: String,
    apiKey: String,
    supabaseUrl: String,
    supabaseKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var job by remember { mutableStateOf(jobTitle) }
    var location by remember { mutableStateOf(workLocation) }
    var pagi by remember { mutableStateOf(shiftPagi) }
    var siang by remember { mutableStateOf(shiftSiang) }
    var apiKeyValue by remember { mutableStateOf(apiKey) }
    var sUrl by remember { mutableStateOf(supabaseUrl) }
    var sKey by remember { mutableStateOf(supabaseKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Profil", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = { Text("Jabatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi Kerja") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pagi,
                    onValueChange = { pagi = it },
                    label = { Text("Jam Shift Pagi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = siang,
                    onValueChange = { siang = it },
                    label = { Text("Jam Shift Siang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiKeyValue,
                    onValueChange = { apiKeyValue = it },
                    label = { Text("OpenRouter API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sUrl,
                    onValueChange = { sUrl = it },
                    label = { Text("Supabase URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sKey,
                    onValueChange = { sKey = it },
                    label = { Text("Supabase Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, job, location, pagi, siang, apiKeyValue, sUrl, sKey) }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

