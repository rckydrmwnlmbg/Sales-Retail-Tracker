package com.example.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Book
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
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GlassCard
import com.example.ui.components.AdaptiveGlassCard
import com.example.ui.components.CollapsibleBentoHeader
import com.example.ui.components.CollapsibleHeaderContent
import com.example.LocalHazeState
import dev.chrisbanes.haze.HazeState
import com.example.ui.viewmodels.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

val NeonCyan = Color(0xFF60B4FF)
val ElectricMagenta = Color(0xFF1565C0)

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: MainViewModel) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Sales", "Interactions"

    val activities by viewModel.allActivities.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val scrollState = rememberLazyListState()
    val localHazeState = com.example.LocalHazeState.current

    val filteredEntries = remember(activities, selectedFilter, searchQuery) {
        activities.filter { entry ->
            (selectedFilter == "All" || (selectedFilter == "Sales" && entry.type == "SALE") || (selectedFilter == "Interactions" && entry.type != "SALE")) &&
            (entry.notes?.contains(searchQuery, ignoreCase = true) == true || entry.type.contains(searchQuery, ignoreCase = true))
        }
    }

    val headerContent = remember(activities.size, selectedFilter) {
        object : CollapsibleHeaderContent {
            override val expandedHeight = 220.dp
            override val collapsedHeight = 136.dp

            @Composable
            override fun ExpandedContent(collapseProgress: Float) {
                Box(modifier = Modifier.fillMaxSize()) {
                    JournalHeaderExpanded(
                        collapseProgress = collapseProgress,
                        hazeState = localHazeState,
                        totalCount = activities.size,
                        selectedFilter = selectedFilter,
                        onFilterSelect = { selectedFilter = it }
                    )
                    JournalHeaderCollapsed(
                        collapseProgress = collapseProgress,
                        selectedFilter = selectedFilter,
                        onFilterSelect = { selectedFilter = it }
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
                val onBg = MaterialTheme.colorScheme.onBackground
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search transactions, notes...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = onBg.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = onBg,
                        unfocusedTextColor = onBg,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = onBg.copy(alpha = 0.2f),
                        cursorColor = NeonCyan
                    ),
                    singleLine = true
                )
            }

            if (filteredEntries.isEmpty()) {
                item {
                    val onBg = MaterialTheme.colorScheme.onBackground
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                                .copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Belum Ada Aktivitas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap tombol + untuk mulai mencatat\ntransaksi atau interaksi pertamamu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredEntries, key = { it.id }) { entry ->
                    val productName = products.find { it.id == entry.productId }?.name
                    val title = when (entry.type) {
                        "SALE" -> productName ?: "Penjualan Umum"
                        "INTEREST" -> "Ketertarikan: ${productName ?: "Produk/Layanan"}"
                        "QUESTION" -> "Pertanyaan: ${entry.questionCategory ?: (productName ?: "Umum")}"
                        "LOST" -> "Peluang Hilang: ${entry.lostReason ?: "Lainnya"}"
                        "AVAILABILITY" -> "Ketersediaan: ${entry.availabilityStatus ?: (productName ?: "Stok")}"
                        "LEARNING" -> "Catatan Belajar: ${entry.topic ?: "Umum"}"
                        else -> "Catatan Cepat"
                    }
                    val details = entry.notes?.takeIf { it.isNotBlank() } ?: "Tidak ada catatan detail."
                    val amount = if (entry.type == "SALE" && entry.price != null && entry.price > 0.0) NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(entry.price) else null
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entry.timestamp))

                    JournalEntryCard(
                        type = entry.type,
                        title = title,
                        details = details,
                        amount = amount,
                        time = time,
                        isEdited = entry.isCorrection,
                        onDelete = {
                            viewModel.deleteActivity(entry)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        CollapsibleBentoHeader(
            hazeState = localHazeState,
            scrollState = scrollState,
            content = headerContent
        )
    }
}

@Composable
fun FilterSegmentedControl(
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("All", "Sales", "Interactions")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        filters.forEach { label ->
            val isSelected = selectedFilter == label
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onFilterSelect(label) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun JournalHeaderExpanded(
    collapseProgress: Float,
    hazeState: HazeState,
    totalCount: Int,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit
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
                        text = "ACTIVITY LOG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sales Journal 📗",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            AdaptiveGlassCard(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(1.2f)
                    .height(96.dp)
                    .graphicsLayer {
                        alpha = (1f - collapseProgress * 2f).coerceIn(0f, 1f)
                        scaleX = lerp(1f, 0.95f, collapseProgress)
                        scaleY = scaleX
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL ACTIVITIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = totalCount.toString(),
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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Active Filter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                FilterSegmentedControl(
                    selectedFilter = selectedFilter,
                    onFilterSelect = onFilterSelect,
                    modifier = Modifier.width(220.dp)
                )
            }
        }
    }
}

@Composable
fun JournalHeaderCollapsed(
    collapseProgress: Float,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit
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
                text = "Sales Journal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            FilterSegmentedControl(
                selectedFilter = selectedFilter,
                onFilterSelect = onFilterSelect,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
fun FilterChipCustom(label: String, isSelected: Boolean, accentColor: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val onSurfaceColor = MaterialTheme.colorScheme.onBackground
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.2f) else onSurfaceColor.copy(alpha = 0.05f)
    val textColor = if (isSelected) accentColor else onSurfaceColor.copy(alpha = 0.6f)
    val borderColor = if (isSelected) accentColor.copy(alpha = 0.5f) else Color.Transparent

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = textColor, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
fun JournalEntryCard(type: String, title: String, details: String, amount: String?, time: String, isEdited: Boolean, onDelete: () -> Unit) {
    val color = if (type == "SALE") NeonCyan else ElectricMagenta
    val icon = if (type == "SALE") Icons.Outlined.AttachMoney else Icons.Outlined.ChatBubbleOutline

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 16.dp)) {
            Icon(imageVector = Icons.Filled.Circle, contentDescription = null, tint = color, modifier = Modifier.size(12.dp).padding(top = 2.dp))
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().padding(vertical = 4.dp).background(color.copy(alpha = 0.3f)))
        }

        GlassCard(modifier = Modifier.weight(1f).padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val onSurfaceColor = MaterialTheme.colorScheme.onBackground

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = time, color = onSurfaceColor.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    if (isEdited) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(onSurfaceColor.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("EDITED", color = onSurfaceColor.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, color = onSurfaceColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = details, color = onSurfaceColor.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Light, lineHeight = 20.sp)
                    }
                    if (amount != null) {
                        Text(text = amount, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).background(onSurfaceColor.copy(alpha = 0.05f), CircleShape)) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
