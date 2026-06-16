package com.example.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.AdaptiveGlassCard
import com.example.ui.components.CollapsibleBentoHeader
import com.example.ui.components.CollapsibleHeaderContent
import com.example.LocalHazeState
import dev.chrisbanes.haze.HazeState
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.*
import com.example.ui.viewmodels.MainViewModel
import java.text.NumberFormat

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

val PrimaryGradient = Brush.linearGradient(listOf(Color(0xFF60B4FF), Color(0xFF1565C0)))

data class ShiftInfo(
    val name: String,
    val timeRange: String,
    val isClockedIn: Boolean
)

@Composable
fun SectionHeader(title: String) {
    val bodyColor = MaterialTheme.colorScheme.onBackground
    Text(
        text = title,
        color = bodyColor.copy(alpha = 0.5f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit = {}) {
    val scrollState = rememberLazyListState()
    val isClockedIn by viewModel.isClockedIn.collectAsState()
    val clockInHour by viewModel.clockInHour.collectAsState()
    val localHazeState = com.example.LocalHazeState.current

    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val activeHour = if (isClockedIn && clockInHour != null) clockInHour!! else currentHour
    val shiftName = if (activeHour < 13) "Shift Pagi" else "Shift Siang"
    
    val shiftPagiTimeStr by viewModel.shiftPagiTime.collectAsState()
    val shiftSiangTimeStr by viewModel.shiftSiangTime.collectAsState()
    val shiftTime = if (activeHour < 13) shiftPagiTimeStr else shiftSiangTimeStr
    
    val userName by viewModel.userName.collectAsState()
    
    var triggerParticles by remember { mutableStateOf(false) }

    val headerContent = remember(isClockedIn, shiftName, shiftTime, userName) {
        object : CollapsibleHeaderContent {
            override val expandedHeight = 220.dp
            override val collapsedHeight = 136.dp

            @Composable
            override fun ExpandedContent(collapseProgress: Float) {
                Box(modifier = Modifier.fillMaxSize()) {
                    HomeHeaderExpanded(
                        collapseProgress = collapseProgress,
                        hazeState = localHazeState,
                        userName = userName,
                        shiftInfo = ShiftInfo(shiftName, shiftTime, isClockedIn),
                        onClockIn = { viewModel.clockIn() },
                        onClockOut = { viewModel.clockOut() }
                    )
                    HomeHeaderCollapsed(
                        collapseProgress = collapseProgress,
                        shiftInfo = ShiftInfo(shiftName, shiftTime, isClockedIn),
                        onClockIn = { viewModel.clockIn() },
                        onClockOut = { viewModel.clockOut() }
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
                SectionHeader("Today's Snapshot")
                SnapshotGrid(viewModel)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Daily Goals")
                DailyGoalSection(viewModel, onGoalAchieved = { triggerParticles = true })
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Target Progress")
                ProgressSection(viewModel)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("AI Coach")
                AiCoachPromoCard(onNavigate)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Attribution Summary")
                AttributionSection(viewModel)
            }
        }

        CollapsibleBentoHeader(
            hazeState = localHazeState,
            scrollState = scrollState,
            content = headerContent
        )
        
        com.example.ui.components.ParticleEffect(
            trigger = triggerParticles,
            onComplete = { triggerParticles = false },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun HomeHeaderExpanded(
    collapseProgress: Float,
    hazeState: HazeState,
    userName: String,
    shiftInfo: ShiftInfo,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 52.dp) // status bar space
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdaptiveGlassCard(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(1.5f)
                    .height(96.dp)
                    .graphicsLayer {
                        alpha = (1f - collapseProgress * 2f).coerceIn(0f, 1f)
                        scaleX = lerp(1f, 0.95f, collapseProgress)
                        scaleY = scaleX
                    }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val greeting = when (hour) {
                        in 6..11  -> "Selamat pagi,"
                        in 12..14 -> "Selamat siang,"
                        in 15..17 -> "Selamat sore,"
                        in 18..22 -> "Selamat malam,"
                        else      -> "Selamat beristirahat,"
                    }
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$userName 👋",
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = shiftInfo.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = shiftInfo.timeRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (shiftInfo.isClockedIn)
                                    MaterialTheme.colorScheme.error.copy(0.2f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(0.2f)
                            )
                            .border(
                                1.dp,
                                if (shiftInfo.isClockedIn)
                                    MaterialTheme.colorScheme.error.copy(0.5f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (shiftInfo.isClockedIn) onClockOut()
                                else onClockIn()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (shiftInfo.isClockedIn)
                                "Clock Out" else "Clock In",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (shiftInfo.isClockedIn)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
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
                val formattedDate = try {
                    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                    sdf.format(Date())
                } catch (e: Throwable) {
                    "Sunday, 14 June 2026"
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HomeHeaderCollapsed(
    collapseProgress: Float,
    shiftInfo: ShiftInfo,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ricky 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${shiftInfo.name} · ${shiftInfo.timeRange}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (shiftInfo.isClockedIn)
                            MaterialTheme.colorScheme.error.copy(0.15f)
                        else
                            MaterialTheme.colorScheme.primary.copy(0.15f)
                    )
                    .clickable {
                        if (shiftInfo.isClockedIn) onClockOut()
                        else onClockIn()
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (shiftInfo.isClockedIn) "Clock Out" else "Clock In",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (shiftInfo.isClockedIn)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SnapshotGrid(viewModel: MainViewModel) {
    val bodyColor = MaterialTheme.colorScheme.onBackground
    
    val revenue by viewModel.personalRevenue.collectAsState()
    val transactions by viewModel.personalTransactions.collectAsState()
    val allActivities by viewModel.allActivities.collectAsState()
    val interactions = allActivities.count { it.type != "SALE" }
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    val goal by viewModel.currentGoal.collectAsState()
    val personalTarget = goal?.personalTarget ?: 0.0
    val targetText = formatter.format(personalTarget)
    val progress = if (personalTarget > 0) (revenue / personalTarget).coerceAtMost(1.0) else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = bodyColor.copy(alpha = 0.1f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = PrimaryGradient,
                            startAngle = -90f,
                            sweepAngle = 360f * progress.toFloat(),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${(progress * 100).toInt()}%", color = bodyColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(text = "Daily Sales Goal", color = bodyColor.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Light)
                    Text(text = formatter.format(revenue), color = bodyColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
                    Text(text = "of $targetText target", color = bodyColor.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(imageVector = Icons.Outlined.ReceiptLong, contentDescription = "Transactions", tint = Color(0xFF6C63FF), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "$transactions", color = bodyColor, fontSize = 28.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
                    Text(text = "Transactions", color = bodyColor.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Light)
                }
            }

            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(imageVector = Icons.Outlined.LocalMall, contentDescription = "Non-Sale", tint = Color(0xFFFBBF24), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "$interactions", color = bodyColor, fontSize = 28.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
                    Text(text = "Interactions", color = bodyColor.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Light)
                }
            }
        }
    }
}

@Composable
fun ProgressSection(viewModel: MainViewModel) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    val revenue by viewModel.personalRevenue.collectAsState()
    val goal by viewModel.currentGoal.collectAsState()
    val personalTarget = goal?.personalTarget ?: 0.0
    val progress = if (personalTarget > 0) (revenue / personalTarget).coerceAtMost(1.0).toFloat() else 0f
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    val progressColor = when {
        progress < 0.5f  -> Color(0xFFFF6B6B)  // danger red
        progress < 0.8f  -> Color(0xFFFFD93D)  // amber
        progress < 1.0f  -> MaterialTheme.colorScheme.primary  // biru
        else             -> Color(0xFF4ADE80)  // success mint
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Monthly Personal Target", color = bodyColor.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Light)
                    Text(text = formatter.format(personalTarget), color = bodyColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
                }
                Text(text = "${(progress * 100).toInt()}%", color = bodyColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(bodyColor.copy(alpha = 0.1f), CircleShape)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val progressWidth = size.width * progress
                    if (progressWidth > 0) {
                        drawLine(
                            color = progressColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(progressWidth, size.height / 2),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttributionSection(viewModel: MainViewModel) {
    val activities by viewModel.allActivities.collectAsState()
    val sales = activities.filter { it.type == "SALE" }
    
    val actualRevenue = sales.filter { it.creditedToId == null }.sumOf { it.price ?: 0.0 }
    val givenRevenue = sales.filter { it.creditedToId != null }.sumOf { it.price ?: 0.0 }
    val officialRevenue = actualRevenue + givenRevenue
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AttributionStat("Actual Revenue", formatter.format(actualRevenue), Color(0xFF4ADE80))
            AttributionStat("Given to Team", formatter.format(givenRevenue), Color(0xFFFBBF24))
            AttributionStat("Official Target Achieved", formatter.format(officialRevenue), Color(0xFF6C63FF))
        }
    }
}

@Composable
fun AttributionStat(label: String, value: String, valueColor: Color) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = bodyColor.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
    }
}

@Composable
fun AiCoachPromoCard(onNavigate: (String) -> Unit) {
    val bodyColor = MaterialTheme.colorScheme.onBackground

    GlassmorphicCard(modifier = Modifier.fillMaxWidth().clickable { onNavigate("coach") }) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF6C63FF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = "AI Coach", tint = Color(0xFF6C63FF), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Your AI Coach", color = bodyColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Get daily coaching and shift summaries.", color = bodyColor.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go", tint = bodyColor.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun DailyGoalSection(viewModel: MainViewModel, onGoalAchieved: () -> Unit) {
    val bodyColor = MaterialTheme.colorScheme.onBackground
    val todayRevenue by viewModel.todayPersonalRevenue.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    
    val currentGoal by viewModel.currentGoal.collectAsState()
    val dailyTarget = currentGoal?.personalTarget?.div(26) ?: 0.0
    
    val todayStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var isAchievedMarked by remember { mutableStateOf(prefs.getBoolean("DAILY_ACHIEVED_$todayStr", false)) }
    
    val progress = if (dailyTarget > 0) (todayRevenue / dailyTarget).coerceAtMost(1.0).toFloat() else 0f
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    
    val progressColor = when {
        progress < 0.5f  -> Color(0xFFFF6B6B) 
        progress < 1.0f  -> Color(0xFFFFD93D) 
        else             -> Color(0xFF4ADE80) 
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Daily Sales Target", color = bodyColor.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Light)
                    Text(text = if (dailyTarget > 0) formatter.format(dailyTarget) else "Belum Ditetapkan", color = bodyColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
                }
                Text(text = "${(progress * 100).toInt()}%", color = bodyColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(bodyColor.copy(alpha = 0.1f), CircleShape)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val progressWidth = size.width * progress
                    if (progressWidth > 0) {
                        drawLine(
                            color = progressColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(progressWidth, size.height / 2),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Today: ${formatter.format(todayRevenue)}", color = bodyColor.copy(alpha = 0.8f), fontSize = 14.sp)
                
                if (progress >= 1.0f && !isAchievedMarked) {
                    androidx.compose.material3.Button(
                        onClick = {
                            isAchievedMarked = true
                            prefs.edit().putBoolean("DAILY_ACHIEVED_$todayStr", true).apply()
                            onGoalAchieved()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF4ADE80)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Mark Achieved!", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isAchievedMarked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Achieved", tint = Color(0xFF4ADE80), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(text = "Target Achieved!", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
