package com.example.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.AdaptiveGlassCard
import com.example.ui.components.CollapsibleBentoHeader
import com.example.ui.components.CollapsibleHeaderContent


import com.example.ui.viewmodels.MainViewModel
import java.text.NumberFormat
import java.util.*
import java.text.SimpleDateFormat

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val scrollState = rememberLazyListState()
    
    var selectedPeriod by remember { mutableStateOf(2) } // 0: Daily, 1: Weekly, 2: Monthly

    val allActivities by viewModel.allActivities.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    
    val filteredActivities = remember(allActivities, selectedPeriod) {
        val now = java.util.Calendar.getInstance()
        allActivities.filter { activity ->
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = activity.timestamp
            when (selectedPeriod) {
                0 -> {
                    cal.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR) &&
                    cal.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
                }
                1 -> {
                    val diff = now.timeInMillis - activity.timestamp
                    diff in 0..(7L * 24 * 60 * 60 * 1000)
                }
                else -> {
                    val today = now.get(java.util.Calendar.DAY_OF_MONTH)
                    val startCal = java.util.Calendar.getInstance()
                    val endCal = java.util.Calendar.getInstance()
                    if (today >= 10) {
                        startCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                        startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        startCal.set(java.util.Calendar.MINUTE, 0)
                        startCal.set(java.util.Calendar.SECOND, 0)
                        endCal.add(java.util.Calendar.MONTH, 1)
                        endCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                        endCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                        endCal.set(java.util.Calendar.MINUTE, 59)
                        endCal.set(java.util.Calendar.SECOND, 59)
                    } else {
                        startCal.add(java.util.Calendar.MONTH, -1)
                        startCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                        startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        startCal.set(java.util.Calendar.MINUTE, 0)
                        startCal.set(java.util.Calendar.SECOND, 0)
                        endCal.set(java.util.Calendar.DAY_OF_MONTH, 10)
                        endCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                        endCal.set(java.util.Calendar.MINUTE, 59)
                        endCal.set(java.util.Calendar.SECOND, 59)
                    }
                    activity.timestamp in startCal.timeInMillis..endCal.timeInMillis
                }
            }
        }
    }

    val revenue = remember(filteredActivities) {
        filteredActivities.filter { it.type == "SALE" && it.creditedToId == null }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    }
    val transactions = remember(filteredActivities) {
        filteredActivities.count { it.type == "SALE" && it.creditedToId == null }
    }

    val headerContent = remember(revenue, transactions, selectedPeriod) {
        object : CollapsibleHeaderContent {
            override val expandedHeight = 220.dp
            override val collapsedHeight = 136.dp

            @Composable
            override fun ExpandedContent(collapseProgress: Float) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AnalyticsHeaderExpanded(
                        collapseProgress = collapseProgress,
                        
                        revenueValue = revenue,
                        transactionCount = transactions,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelect = { selectedPeriod = it }
                    )
                    AnalyticsHeaderCollapsed(
                        collapseProgress = collapseProgress,
                        selectedPeriod = selectedPeriod,
                        onPeriodSelect = { selectedPeriod = it }
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = com.example.ui.theme.AppSpacing.lg,
                end = com.example.ui.theme.AppSpacing.lg,
                top = headerContent.expandedHeight + com.example.ui.theme.AppSpacing.lg,
                bottom = 200.dp
            ),
            verticalArrangement = Arrangement.spacedBy(com.example.ui.theme.AppSpacing.xxl)
        ) {
            item {
                SectionTitle("REVENUE TREND")
                RevenueChart(filteredActivities, selectedPeriod)
            }

            item {
                SectionTitle("KEY METRICS")
                MetricsGrid(filteredActivities)
            }
            
            item {
                SectionTitle("Aktivitas per Jam")
                PeakHourHeatmap(filteredActivities)
            }

            item {
                SectionTitle("CONVERSION FUNNEL")
                ConversionFunnel(filteredActivities)
            }

            item {
                SectionTitle("SHIFT & ATTENDANCE")
                ShiftAnalytics(filteredActivities)
            }

            item {
                SectionTitle("PRODUCT ANALYTICS")
                ProductAnalytics(filteredActivities, allProducts)
            }

            item {
                SectionTitle("ATTRIBUTION ANALYTICS")
                AttributionAnalytics(filteredActivities)
            }

            item {
                SectionTitle("WIN/LOSS ANALYSIS")
                WinLossAnalysis(filteredActivities)
            }

            item {
                SectionTitle("KNOWLEDGE GAP")
                KnowledgeGapAnalysis(filteredActivities)
            }
        }

        CollapsibleBentoHeader(
            
            scrollState = scrollState,
            content = headerContent
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
    )
    Spacer(Modifier.height(com.example.ui.theme.AppSpacing.sm))
}

@Composable
fun SegmentedPeriodControl(
    selectedPeriod: Int,
    onPeriodSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = listOf("Daily", "Weekly", "Monthly")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("Daily", "Weekly", "Monthly").forEachIndexed { index, label ->
            val isSelected = selectedPeriod == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onPeriodSelect(index) },
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
fun AnalyticsHeaderExpanded(
    collapseProgress: Float,
    revenueValue: Double,
    transactionCount: Int,
    selectedPeriod: Int,
    onPeriodSelect: (Int) -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
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
                        text = "PERFORMANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SegmentedPeriodControl(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelect = onPeriodSelect,
                        modifier = Modifier.fillMaxWidth()
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
                val periodName = when(selectedPeriod) {
                    0 -> "Today"
                    1 -> "This Week"
                    else -> "This Month"
                }
                Text(
                    text = "$periodName  •  ${formatter.format(revenueValue)}  •  $transactionCount Transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AnalyticsHeaderCollapsed(
    collapseProgress: Float,
    selectedPeriod: Int,
    onPeriodSelect: (Int) -> Unit
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
                text = "Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
            SegmentedPeriodControl(
                selectedPeriod = selectedPeriod,
                onPeriodSelect = onPeriodSelect,
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

@Composable
fun RevenueChart(activities: List<com.example.data.local.entity.ActivityEntity>, selectedPeriod: Int) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    val revenue = activities.filter { it.type == "SALE" && it.creditedToId == null }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    val sales = activities.filter { it.type == "SALE" }.sortedBy { it.timestamp }
    val sf = SimpleDateFormat("dd/MM", Locale.getDefault())
    val dailyRevenue = sales.groupBy { sf.format(Date(it.timestamp)) }
        .mapValues { it.value.sumOf { s -> s.price ?: 0.0 } }
        .entries.toList()
        
    val periodName = when(selectedPeriod) {
        0 -> "Today"
        1 -> "This Week"
        else -> "This Month"
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth().height(250.dp)) {
        Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = periodName.uppercase(Locale.getDefault()), 
                    style = MaterialTheme.typography.labelMedium,
                    color = bodyColor.copy(alpha = 0.6f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(com.example.ui.theme.AppSpacing.xs))
                    Text(
                        text = if(dailyRevenue.size > 1) "+Active" else "Tracking", 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = formatter.format(revenue), 
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = bodyColor,
                modifier = Modifier.padding(top = com.example.ui.theme.AppSpacing.xs, bottom = com.example.ui.theme.AppSpacing.xl)
            )

            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                if (dailyRevenue.isEmpty()) {
                    // Empty state line
                    drawLine(color = bodyColor.copy(alpha=0.1f), start = Offset(0f, height/2), end = Offset(width, height/2), strokeWidth = 2.dp.toPx())
                    return@Canvas
                }

                val maxRev = dailyRevenue.maxOf { it.value }.toFloat().takeIf { it > 0f } ?: 1000f
                val minRev = 0f
                val range = maxRev - minRev
                
                val points = if (dailyRevenue.size == 1) {
                    listOf(
                        Offset(0f, height),
                        Offset(width, height - ((dailyRevenue[0].value.toFloat() / maxRev) * height))
                    )
                } else {
                    dailyRevenue.mapIndexed { index, entry ->
                        val x = (index.toFloat() / (dailyRevenue.size - 1)) * width
                        val y = if (range == 0f) height / 2 else height - ((entry.value.toFloat() - minRev) / range * height)
                        Offset(x, y)
                    }
                }

                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                }
                drawPath(path = path, color = primaryColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                points.forEach { point -> drawCircle(color = bodyColor, radius = 4.dp.toPx(), center = point) }
            }
        }
    }
}

@Composable
fun MetricsGrid(activities: List<com.example.data.local.entity.ActivityEntity>) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    val interests = activities.count { it.type == "INTEREST" }.toFloat()
    val salesCount = activities.count { it.type == "SALE" }.toFloat()
    val conv = if (interests > 0) (salesCount / interests * 100) else 0f
    
    val rev = activities.filter { it.type == "SALE" && it.creditedToId == null }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    val avg = if (salesCount > 0) rev / salesCount else 0.0
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(com.example.ui.theme.AppSpacing.lg)) {
        GlassmorphicCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
                Text(
                    text = "Closing Rate", 
                    style = MaterialTheme.typography.labelMedium,
                    color = bodyColor.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(com.example.ui.theme.AppSpacing.sm))
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", conv), 
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = bodyColor
                )
            }
        }
        GlassmorphicCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
                Text(
                    text = "Avg Value", 
                    style = MaterialTheme.typography.labelMedium,
                    color = bodyColor.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(com.example.ui.theme.AppSpacing.sm))
                Text(
                    text = formatter.format(avg), 
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = bodyColor
                )
            }
        }
    }
}

@Composable
fun PeakHourHeatmap(activities: List<com.example.data.local.entity.ActivityEntity>) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    val formatter = SimpleDateFormat("HH", Locale.getDefault())
    val hourCounts = activities.groupBy { formatter.format(Date(it.timestamp)).toInt() }.mapValues { it.value.size }
    
    val displayHours = listOf(10, 12, 14, 16, 18, 20)
    val maxCount = hourCounts.values.maxOrNull()?.toFloat() ?: 1f
    
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
            Spacer(modifier = Modifier.height(com.example.ui.theme.AppSpacing.lg))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                displayHours.forEach { hour ->
                    val count = hourCounts[hour] ?: hourCounts[hour-1] ?: hourCounts[hour+1] ?: 0
                    val density = if (maxCount > 0) (count.toFloat() / maxCount).coerceAtLeast(0.1f) else 0.1f
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(32.dp).height(80.dp).clip(RoundedCornerShape(8.dp)).background(bodyColor.copy(alpha = 0.1f)), contentAlignment = Alignment.BottomCenter) {
                            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(density).background(MaterialTheme.colorScheme.primary.copy(alpha = density)))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$hour:00", color = bodyColor.copy(alpha = 0.5f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ConversionFunnel(activities: List<com.example.data.local.entity.ActivityEntity>) {
    val interests = activities.count { it.type == "INTEREST" }
    val questions = activities.count { it.type == "QUESTION" }
    val sales = activities.count { it.type == "SALE" }
    
    val maxVal = maxOf(interests, questions, sales, 1).toFloat()

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
            FunnelStep("Customer Interest", interests.toString(), if(maxVal>0) interests/maxVal else 0f)
            FunnelStep("Customer Question", questions.toString(), if(maxVal>0) questions/maxVal else 0f)
            FunnelStep("Sale", sales.toString(), if(maxVal>0) sales/maxVal else 0f)
        }
    }
}

@Composable
fun FunnelStep(label: String, value: String, percentage: Float) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.padding(vertical = com.example.ui.theme.AppSpacing.sm)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = bodyColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(com.example.ui.theme.AppSpacing.xs))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(bodyColor.copy(alpha = 0.1f))) {
            Box(modifier = Modifier.fillMaxWidth(percentage).fillMaxHeight().background(Color(0xFF6C63FF)))
        }
    }
}

@Composable
fun ProductAnalytics(activities: List<com.example.data.local.entity.ActivityEntity>, products: List<com.example.data.local.entity.ProductEntity>) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    val salesByProduct = activities.filter { it.type == "SALE" }
        .groupBy { it.productId }
        .map { (prodId, acts) ->
            val pName = products.find { it.id == prodId }?.name ?: "Unknown Product"
            val revenue = acts.sumOf { it.finalPrice ?: it.price ?: 0.0 }
            val count = acts.size
            Triple(pName, revenue, count)
        }
        .sortedByDescending { it.second }
        .take(5)

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        val onBg = MaterialTheme.colorScheme.onBackground
        Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
            if (salesByProduct.isEmpty()) {
                Text("No data available yet.", color = onBg.copy(alpha=0.5f), fontSize=14.sp)
            } else {
                salesByProduct.forEach { (name, rev, count) ->
                    ProductItem(name, formatter.format(rev), "$count units")
                }
            }
        }
    }
}

@Composable
fun ProductItem(name: String, revenue: String, units: String) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = com.example.ui.theme.AppSpacing.sm), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, color = bodyColor, fontWeight = FontWeight.Medium)
            Text(units, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
        }
        Text(revenue, style = MaterialTheme.typography.titleMedium, color = bodyColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AttributionAnalytics(activities: List<com.example.data.local.entity.ActivityEntity>) {
    val sales = activities.filter { it.type == "SALE" }
    
    val actualRevenue = sales.filter { it.creditedToId == null }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    val givenRevenue = sales.filter { it.creditedToId != null }.sumOf { it.finalPrice ?: it.price ?: 0.0 }
    val officialRevenue = actualRevenue + givenRevenue
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    val maxVal = maxOf(actualRevenue, officialRevenue, givenRevenue, 1.0).toFloat()

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(com.example.ui.theme.AppSpacing.sm)) {
            AttributionItem("Actual Revenue", formatter.format(actualRevenue), (actualRevenue/maxVal).toFloat(), Color(0xFF4ADE80))
            AttributionItem("Official Revenue", formatter.format(officialRevenue), (officialRevenue/maxVal).toFloat(), MaterialTheme.colorScheme.primary)
            AttributionItem("Revenue Given", formatter.format(givenRevenue), (givenRevenue/maxVal).toFloat(), Color(0xFFFBBF24))
            AttributionItem("Revenue Received", formatter.format(0.0), 0f, Color.Gray)
        }
    }
}

@Composable
fun AttributionItem(source: String, value: String, percentage: Float, color: Color) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = com.example.ui.theme.AppSpacing.sm)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(source, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(com.example.ui.theme.AppSpacing.xs))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(bodyColor.copy(alpha = 0.1f))) {
            Box(modifier = Modifier.fillMaxWidth(percentage).fillMaxHeight().background(color))
        }
    }
}

@Composable
fun WinLossAnalysis(activities: List<com.example.data.local.entity.ActivityEntity>) {
    val bodyColor = MaterialTheme.colorScheme.onBackground
    
    val lost = activities.filter { it.type == "LOST" }
    val totalLost = lost.size
    
    val reasons = lost.groupBy { it.lostReason ?: "Lainnya" }.mapValues { it.value.size }
        .entries.sortedByDescending { it.value }.take(5)

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
            if (reasons.isEmpty()) {
                Text("No data available yet.", style = MaterialTheme.typography.bodyMedium, color = bodyColor.copy(alpha=0.5f))
            } else {
                reasons.forEach { entry ->
                    val pct = entry.value.toFloat() / totalLost
                    MissReason(entry.key, pct, entry.value)
                }
            }
        }
    }
}

@Composable
fun MissReason(reason: String, percentage: Float, value: Int) {
    val bodyColor = MaterialTheme.colorScheme.onBackground
    val color = MaterialTheme.colorScheme.error

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.MoneyOff, contentDescription=null, tint=color, modifier=Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(reason, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            }
            Text("${(percentage * 100).toInt()}% ($value)", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(bodyColor.copy(alpha = 0.1f))) {
            androidx.compose.animation.AnimatedVisibility(visible = true) {
                 Box(modifier = Modifier.fillMaxWidth(percentage).fillMaxHeight().background(color))
            }
        }
    }
}

@Composable
fun KnowledgeGapAnalysis(activities: List<com.example.data.local.entity.ActivityEntity>) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    val questions = activities.filter { it.type == "QUESTION" }
    
    val topQuestions = questions.groupBy { it.questionCategory ?: "Lainnya" }
        .mapValues { it.value.size }
        .entries.sortedByDescending { it.value }.take(4)

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(com.example.ui.theme.AppSpacing.lg)) {
            if (topQuestions.isEmpty()) {
                Text("No data available yet.", style = MaterialTheme.typography.bodyMedium, color = bodyColor.copy(alpha=0.5f))
            } else {
                val maxQuestions = topQuestions.maxOf { it.value }.toFloat().coerceAtLeast(1f)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topQuestions.forEach { entry ->
                        KnowledgeGapItem(entry.key, entry.value, entry.value.toFloat() / maxQuestions)
                    }
                }
            }
        }
    }
}

@Composable
fun KnowledgeGapItem(topic: String, count: Int, relativePercentage: Float) {
    val color = Color(0xFF00D4FF)
    val bodyColor = MaterialTheme.colorScheme.onBackground
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LightbulbCircle, contentDescription=null, tint=color, modifier=Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(topic, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            }
            Text("$count", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(bodyColor.copy(alpha = 0.1f))) {
            Box(modifier = Modifier.fillMaxWidth(relativePercentage).fillMaxHeight().background(color))
        }
    }
}

@Composable
fun ShiftAnalytics(activities: List<com.example.data.local.entity.ActivityEntity>) {
    val clockIns = activities.count { it.type == "CLOCK_IN" }
    val clockOuts = activities.count { it.type == "CLOCK_OUT" }
    val bodyColor = MaterialTheme.colorScheme.onBackground
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        com.example.ui.components.GlassCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Shifts Started", 
                    style = MaterialTheme.typography.labelMedium,
                    color = bodyColor.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$clockIns", 
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = bodyColor
                )
            }
        }
        com.example.ui.components.GlassCard(modifier = Modifier.weight(1f)) {
             Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Shifts Ended", 
                    style = MaterialTheme.typography.labelMedium,
                    color = bodyColor.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$clockOuts", 
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = bodyColor
                )
            }
        }
    }
}
