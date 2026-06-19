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



val NeonCyan = Color(0xFF60B4FF)

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun ExportScreen(viewModel: MainViewModel, onBack: () -> Unit = {}) {
    val scrollState = rememberLazyListState()
    
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
                            coroutineScope.launch {
                                val calendar = java.util.Calendar.getInstance()
                                val now = calendar.timeInMillis
                                
                                val startFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                val timeLabel = when (selectedTimeframe) {
                                    0 -> "Hari Ini (${startFormat.format(calendar.time)})"
                                    1 -> {
                                        val calStart = calendar.clone() as java.util.Calendar
                                        calStart.set(java.util.Calendar.DAY_OF_WEEK, calStart.firstDayOfWeek)
                                        val calEnd = calStart.clone() as java.util.Calendar
                                        calEnd.add(java.util.Calendar.DAY_OF_WEEK, 6)
                                        "Minggu Ini (${startFormat.format(calStart.time)} - ${startFormat.format(calEnd.time)})"
                                    }
                                    2 -> {
                                        val startCal = java.util.Calendar.getInstance()
                                        startCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                                        
                                        val endCal = java.util.Calendar.getInstance()
                                        endCal.add(java.util.Calendar.MONTH, 1)
                                        endCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                                        
                                        "Bulan Ini (${startFormat.format(startCal.time)} - ${startFormat.format(endCal.time)})"
                                    }
                                    else -> "Semua Riwayat"
                                }

                                snackbarHostState.showSnackbar("Menyiapkan dokumen '$reportType' ($timeLabel)...")
                                kotlinx.coroutines.delay(800)
                                
                                val filteredActivities = activities.filter {
                                    val actCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
                                    when (selectedTimeframe) {
                                        0 -> actCal.get(java.util.Calendar.DAY_OF_YEAR) == calendar.get(java.util.Calendar.DAY_OF_YEAR) && actCal.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR)
                                        1 -> actCal.get(java.util.Calendar.WEEK_OF_YEAR) == calendar.get(java.util.Calendar.WEEK_OF_YEAR) && actCal.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR)
                                        2 -> {
                                            val startCal = java.util.Calendar.getInstance()
                                            startCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                                            startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                            startCal.set(java.util.Calendar.MINUTE, 0)
                                            startCal.set(java.util.Calendar.SECOND, 0)
                                            
                                            val endCal = java.util.Calendar.getInstance()
                                            endCal.add(java.util.Calendar.MONTH, 1)
                                            endCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                                            endCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                            endCal.set(java.util.Calendar.MINUTE, 59)
                                            endCal.set(java.util.Calendar.SECOND, 59)
                                            
                                            it.timestamp in startCal.timeInMillis..endCal.timeInMillis
                                        }
                                        else -> true
                                    }
                                }
                                
                                if (filteredActivities.isEmpty()) {
                                    snackbarHostState.showSnackbar("Tidak ada data untuk direkap pada periode ini")
                                    return@launch
                                }
                                
                                val rows = mutableListOf<List<String>>()
                                val currentProducts = viewModel.allProducts.value
                                val currentColleagues = viewModel.allColleagues.value
                                
                                rows.add(listOf("Report", "Sales Performance"))
                                rows.add(listOf("Timeframe", timeLabel))
                                rows.add(listOf())
                                
                                rows.add(listOf("Timestamp", "Shift", "Type", "Product Code", "Product Name", "Category", "Price per Item", "QTY", "Discount (%)", "Final Price", "Credit To", "Credit From"))
                                
                                var totalHariMasuk = 0
                                var totalPenjualan = 0.0

                                val dailyGroup = filteredActivities.groupBy { 
                                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp)) 
                                }

                                for ((date, acts) in dailyGroup.toSortedMap()) {
                                    val clockIn = acts.find { it.type == "CLOCK_IN" }
                                    val clockOut = acts.find { it.type == "CLOCK_OUT" }
                                    val timeStrIn = clockIn?.let { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp)) } ?: "-"
                                    val timeStrOut = clockOut?.let { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp)) } ?: "-"
                                    
                                    val shiftType = clockIn?.let { 
                                        val inCal = java.util.Calendar.getInstance().apply { timeInMillis = it.timestamp }
                                        if (inCal.get(java.util.Calendar.HOUR_OF_DAY) < 13) "Pagi" else "Siang" 
                                    } ?: "Unknown"
                                    val shiftRecord = "Shift $shiftType (In: $timeStrIn - Out: $timeStrOut)"
                                    
                                    if (clockIn != null && clockOut != null) {
                                        val durationHours = (clockOut.timestamp - clockIn.timestamp) / 3600000.0
                                        if (durationHours >= 8.0) {
                                            totalHariMasuk++
                                        }
                                    }

                                    for (act in acts) {
                                        if (act.type == "SALE") {
                                            totalPenjualan += (act.finalPrice ?: act.price ?: 0.0)

                                            val timeStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(act.timestamp))
                                            val prodFound = currentProducts.find { it.id == act.productId }
                                            val prodCode = prodFound?.code?.takeIf { it.isNotBlank() } ?: "-"
                                            val prodName = prodFound?.name ?: "-"
                                            val catName = prodFound?.category?.takeIf { it.isNotBlank() } ?: "-"
                                            val toName = if (act.creditedToId != null) currentColleagues.find { it.id == act.creditedToId }?.name ?: "-" else "-"
                                            val fromName = if (act.creditedFromId != null) currentColleagues.find { it.id == act.creditedFromId }?.name ?: "-" else "-"
                                            val priceStr = act.price?.toLong()?.toString() ?: "-"
                                            val qtyStr = act.quantity?.toString() ?: "-"
                                            val discStr = act.discount?.toString() ?: "-"
                                            val finalPriceStr = act.finalPrice?.toLong()?.toString() ?: "-"
                                            
                                            rows.add(listOf(
                                                timeStr,
                                                shiftRecord,
                                                act.type,
                                                prodCode,
                                                prodName,
                                                catName,
                                                priceStr,
                                                qtyStr,
                                                discStr,
                                                finalPriceStr,
                                                toName,
                                                fromName
                                            ))
                                        }
                                    }
                                }

                                val productCounts = filteredActivities.filter { it.type == "SALE" && it.productId != null }.groupBy { it.productId!! }.mapValues { it.value.sumOf { act -> act.quantity ?: 1 } }
                                val topProductId = productCounts.maxByOrNull { it.value }?.key
                                val topProductName = currentProducts.find { it.id == topProductId }?.name ?: "-"
                                val topProductCount = productCounts[topProductId] ?: 0
                                val topProductStr = if (topProductId != null) "$topProductName ($topProductCount items)" else "-"
                                
                                val personalGoalValue = viewModel.currentGoal.value?.personalTarget ?: 0.0
                                val targetAmount = when (selectedTimeframe) {
                                    0 -> personalGoalValue / 30.0
                                    1 -> personalGoalValue / 4.0
                                    else -> personalGoalValue
                                }
                                
                                val targetPerc = if (targetAmount > 0) (totalPenjualan / targetAmount * 100).toInt() else 0
                                val targetStr = if (targetAmount > 0) "Target: Rp ${targetAmount.toLong()} | Achieved: Rp ${totalPenjualan.toLong()} ($targetPerc%)" else "Target belum diatur"
                                
                                val totalTransactions = filteredActivities.count { it.type == "SALE" }
                                val totalItemsSold = filteredActivities.filter { it.type == "SALE" }.sumOf { it.quantity ?: 1 }

                                rows.add(listOf())
                                rows.add(listOf("Total Hari Masuk", totalHariMasuk.toString()))
                                rows.add(listOf("Total Transaksi", totalTransactions.toString()))
                                rows.add(listOf("Total Items Terjual", totalItemsSold.toString()))
                                rows.add(listOf("Total Penjualan", totalPenjualan.toLong().toString()))
                                rows.add(listOf("Produk Paling Sering Dijual", topProductStr))
                                rows.add(listOf("Sales Target", targetStr))

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
            modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(com.example.ui.theme.AppSpacing.lg)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
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

    Column(verticalArrangement = Arrangement.spacedBy(com.example.ui.theme.AppSpacing.lg)) {
        ExportItemCard(
            title = "Sales Performance",
            subtitle = "CSV • Laporan Rekap Penjualan",
            icon = Icons.Outlined.Description,
            onClick = { handleClick("Sales Performance") }
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
        contentPadding = PaddingValues(horizontal = com.example.ui.theme.AppSpacing.xl)
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
                Spacer(modifier = Modifier.width(com.example.ui.theme.AppSpacing.lg))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
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
