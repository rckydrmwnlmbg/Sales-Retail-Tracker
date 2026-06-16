package com.example.ui.screens.export

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.AdaptiveGlassCard
import com.example.ui.components.CollapsibleBentoHeader
import com.example.ui.components.CollapsibleHeaderContent
import com.example.LocalHazeState
import dev.chrisbanes.haze.HazeState

val NeonCyan = Color(0xFF60B4FF)

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun ExportScreen(viewModel: MainViewModel, onBack: () -> Unit = {}) {
    val scrollState = rememberLazyListState()
    val localHazeState = com.example.LocalHazeState.current
    var selectedFormat by remember { mutableStateOf(0) } // we can remove this, we'll configure per button
    var selectedTimeframe by remember { mutableStateOf(0) } // 0: Today, 1: Week, 2: Month, 3: Custom
    
    var showPreviewModal by remember { mutableStateOf(false) }
    var previewData by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var currentReportType by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val activities by viewModel.allActivities.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val headerContent = remember(selectedFormat) {
        object : CollapsibleHeaderContent {
            override val expandedHeight = 220.dp
            override val collapsedHeight = 136.dp

            @Composable
            override fun ExpandedContent(collapseProgress: Float) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ExportHeaderExpanded(
                        collapseProgress = collapseProgress,
                        hazeState = localHazeState,
                        onBack = onBack,
                        totalActivities = activities.size
                    )
                    ExportHeaderCollapsed(
                        collapseProgress = collapseProgress
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "SELECT TIMEFRAME",
                    color = NeonCyan.copy(alpha = 0.8f),
                    letterSpacing = 1.5.sp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TimeframeSelector(
                    selectedTimeframe = selectedTimeframe,
                    onSelect = { selectedTimeframe = it }
                )
            }

            item {
                Text(
                    text = "EXPORT OPTIONS",
                    color = NeonCyan.copy(alpha = 0.8f),
                    letterSpacing = 1.5.sp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
                ExportOptionsGrid(
                    onExportClick = { reportType ->
                        if (activities.isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Data aktivitas belum ada")
                            }
                        } else {
                            val time = when(selectedTimeframe) {
                                0 -> "Hari Ini"
                                1 -> "Minggu Ini"
                                2 -> "Bulan Ini"
                                else -> "Semua Riwayat"
                            }
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Menyiapkan dokumen '$reportType' ($time)...")
                                kotlinx.coroutines.delay(800)
                                
                                val calendar = java.util.Calendar.getInstance()
                                val now = calendar.timeInMillis
                                val filteredActivities = activities.filter {
                                    val actCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
                                    when (selectedTimeframe) {
                                        0 -> actCal.get(java.util.Calendar.DAY_OF_YEAR) == calendar.get(java.util.Calendar.DAY_OF_YEAR) && actCal.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR)
                                        1 -> actCal.get(java.util.Calendar.WEEK_OF_YEAR) == calendar.get(java.util.Calendar.WEEK_OF_YEAR) && actCal.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR)
                                        2 -> actCal.get(java.util.Calendar.MONTH) == calendar.get(java.util.Calendar.MONTH) && actCal.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR)
                                        else -> true
                                    }
                                }
                                
                                if (filteredActivities.isEmpty()) {
                                    snackbarHostState.showSnackbar("Tidak ada data untuk direkap pada periode ini")
                                    return@launch
                                }
                                
                                val rows = mutableListOf<List<String>>()
                                rows.add(listOf("Timestamp", "Type", "Product Name", "Notes", "Price", "Customer Type", "Lost Reason", "Credited To", "Credited From"))
                                val currentProducts = viewModel.allProducts.value
                                val currentColleagues = viewModel.allColleagues.value
                                for (activity in filteredActivities) {
                                    val prodName = currentProducts.find { it.id == activity.productId }?.name ?: ""
                                    val toName = currentColleagues.find { it.id == activity.creditedToId }?.name ?: ""
                                    val fromName = currentColleagues.find { it.id == activity.creditedFromId }?.name ?: ""
                                    
                                    val row = listOf(
                                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(activity.timestamp)),
                                        activity.type,
                                        prodName,
                                        activity.notes ?: "",
                                        activity.price?.toLong()?.toString() ?: "",
                                        activity.customerType ?: "",
                                        activity.lostReason ?: "",
                                        toName,
                                        fromName
                                    )
                                    rows.add(row)
                                }
                                
                                previewData = rows
                                currentReportType = reportType
                                showPreviewModal = true
                            }
                        }
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        )

        CollapsibleBentoHeader(
            hazeState = localHazeState,
            scrollState = scrollState,
            content = headerContent
        )
        
        if (showPreviewModal) {
            CsvPreviewDialog(
                reportType = currentReportType,
                data = previewData,
                onDismiss = { showPreviewModal = false },
                onExport = {
                    showPreviewModal = false
                    val csvBuilder = StringBuilder()
                    for (row in previewData) {
                        csvBuilder.append(row.joinToString(",") { it.escapeCsv() }).append("\n")
                    }
                    try {
                        val exportDir = java.io.File(context.cacheDir, "exports")
                        if (!exportDir.exists()) exportDir.mkdirs()
                        val file = java.io.File(exportDir, "Laporan_Penjualan_${System.currentTimeMillis()}.csv")
                        file.writeText(csvBuilder.toString(), Charsets.UTF_8)
    
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
    
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Laporan Penjualan - $currentReportType")
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan Laporan: $currentReportType"))
                    } catch (e: Exception) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Gagal membagikan laporan: ${e.message}") }
                    }
                }
            )
        }
    }
}

fun String.escapeCsv(): String {
    val needsEscaping = this.contains(",") || this.contains("\"") || this.contains("\n") || this.contains("\r")
    if (needsEscaping) {
        val escaped = this.replace("\"", "\"\"")
        return "\"$escaped\""
    }
    return this
}

@Composable
fun CsvPreviewDialog(
    reportType: String,
    data: List<List<String>>,
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Pratinjau: $reportType",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    val scrollStateV = rememberLazyListState()
                    val scrollStateH = rememberScrollState()
                    
                    LazyColumn(
                        state = scrollStateV,
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollStateH)
                            .padding(8.dp)
                    ) {
                        items(data.size) { rowIndex ->
                            val row = data[rowIndex]
                            val isHeader = rowIndex == 0
                            Row(modifier = Modifier.fillMaxWidth()) {
                                row.forEach { cell ->
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = 100.dp)
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = cell.replace("\n", " "),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            style = if (isHeader) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold) 
                                                    else MaterialTheme.typography.bodySmall,
                                            color = if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onExport) {
                        Text("Kirim / Unduh")
                    }
                }
            }
        }
    }
}

@Composable
fun FormatSelector(
    selectedFormat: Int,
    onFormatSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val formats = listOf("CSV (Excel)") // Removed PDF and XLSX mocks
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        formats.forEachIndexed { index, label ->
            // Always selected since there is only 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onFormatSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun ExportHeaderExpanded(
    collapseProgress: Float,
    hazeState: HazeState,
    onBack: () -> Unit,
    totalActivities: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = (1f - collapseProgress * 4f).coerceIn(0f, 1f)
                    }
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
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
                        text = "REPORTING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Export Data 📁",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            AdaptiveGlassCard(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(1.1f)
                    .height(96.dp)
                    .graphicsLayer {
                        alpha = (1f - collapseProgress * 2f).coerceIn(0f, 1f)
                        scaleX = lerp(1f, 0.95f, collapseProgress)
                        scaleY = scaleX
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RECORDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = totalActivities.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
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
                    text = if (totalActivities > 0) "Status: $totalActivities Records Ready to Export" else "Status: No Data Available",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (totalActivities > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ExportHeaderCollapsed(
    collapseProgress: Float
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
                text = "Export Data 📁",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

@Composable
fun TimeframeSelector(selectedTimeframe: Int, onSelect: (Int) -> Unit) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TimeOption("Today's Report", "Includes all shifts", selectedTimeframe == 0, { onSelect(0) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            TimeOption("This Week", "Current active week", selectedTimeframe == 1, { onSelect(1) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            TimeOption("This Month", "Current billing month", selectedTimeframe == 2, { onSelect(2) })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            TimeOption("Custom Range", "Select specific dates", selectedTimeframe == 3, { onSelect(3) })
        }
    }
}

@Composable
fun TimeOption(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun ExportOptionsGrid(onExportClick: (String) -> Unit = {}) {
    val haptic = LocalHapticFeedback.current

    val handleClick = { type: String ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onExportClick(type)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ExportItemCard(
            title = "Evidence Report",
            subtitle = "CSV • Timestamped format",
            icon = Icons.Outlined.Description,
            onClick = { handleClick("Evidence Report") }
        )
        ExportItemCard(
            title = "Personal Sales Ledger",
            subtitle = "CSV • Excel / Spreadsheet",
            icon = Icons.Outlined.List,
            onClick = { handleClick("Personal Sales Ledger") }
        )
        ExportItemCard(
            title = "Monthly Performance",
            subtitle = "CSV • Detailed Analytics",
            icon = Icons.Outlined.DateRange,
            onClick = { handleClick("Monthly Performance") }
        )
    }
}

@Composable
fun ExportItemCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonCyan.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
