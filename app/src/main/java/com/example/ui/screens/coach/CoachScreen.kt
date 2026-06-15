package com.example.ui.screens.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.logic.AiCoachUseCase
import com.example.logic.CoachRecommendations
import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch

private val NeonCyan = Color(0xFF00E5FF)
private val ElectricMagenta = Color(0xFFD500F9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var recommendations by remember { mutableStateOf<CoachRecommendations?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val activities by viewModel.allActivities.collectAsState()
    val goal by viewModel.currentGoal.collectAsState()
    val personalRevenue by viewModel.personalRevenue.collectAsState()
    
    val generateInsights: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            try {
                recommendations = AiCoachUseCase.generateCoachingInsights(
                    activities = activities,
                    goal = goal,
                    revenue = personalRevenue
                )
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    message = e.message ?: "Terjadi kesalahan yang tidak diketahui."
                )
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Coach", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = innerPadding.calculateTopPadding() + 24.dp, bottom = 200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CoachHeader()
            }
            
            item {
                if (recommendations != null) {
                    CoachMessageCard(recommendations!!.summary)
                }
            }
            
            item {
                Text(
                    text = "INSIGHTS & SUMMARIES",
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                InsightsSection(
                    isLoading = isLoading,
                    onGenerateClick = generateInsights,
                    tips = recommendations?.tips ?: emptyList()
                )
            }

            if (recommendations != null && recommendations!!.knowledgeGaps.isNotEmpty()) {
                item {
                    Text(
                        text = "KNOWLEDGE GAP RECOMMENDATIONS",
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    KnowledgeGapRecommendations(recommendations!!.knowledgeGaps)
                }
            } else if (!isLoading) {
                item {
                    Text(
                        text = "PERFORMANCE REVIEW",
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    PerformanceSection()
                }
                
                item {
                    Text(
                        text = "KNOWLEDGE GAP RECOMMENDATIONS",
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    KnowledgeGapEmpty()
                }
            }
        }
    }
}

@Composable
fun CoachHeader() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ElectricMagenta.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "AI Coach",
                    tint = ElectricMagenta,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Your AI Coach",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Coaching personal secara real-time untuk meningkatkan pencapaian targetmu.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CoachMessageCard(message: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Summary Highlight", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun InsightsSection(isLoading: Boolean, onGenerateClick: () -> Unit, tips: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CoachCard(
            title = "Generate Insights",
            description = "Buat rangkuman analisis aktivitas toko hari ini beserta evaluasi dari AI Coach Gemini.",
            icon = Icons.Outlined.Summarize,
            actionText = if (isLoading) "Menganalisa..." else "Generate",
            onClick = onGenerateClick,
            highlight = true
        )
        
        if (tips.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonCyan.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Outlined.Chat, contentDescription = "Tips", tint = NeonCyan, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tips for You", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    tips.forEach { tip ->
                        Text("• $tip", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CoachCard(
            title = "Monthly Review",
            description = "Analisis komprehensif metrik performa sales selama periode bulan ini.",
            icon = Icons.Outlined.Lightbulb,
            actionText = "Open Review",
            highlight = false,
            onClick = {}
        )
    }
}

@Composable
fun KnowledgeGapEmpty() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recommended Topics for You", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Belum ada rekomendasi. Tap 'Generate' di atas untuk membagikan data terbarumu dengan AI Coach agar kami bisa merekomendasikan topik belajar yang sesuai dengan performamu.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
fun KnowledgeGapRecommendations(gaps: List<com.example.logic.KnowledgeGap>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recommended Topics for You", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            
            gaps.forEach { gap ->
                TopicItem(gap.topic, gap.priority, gap.isHigh)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TopicItem(title: String, priority: String, isHigh: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isHigh) ElectricMagenta else NeonCyan)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        }
        Text(
            priority,
            color = if (isHigh) ElectricMagenta.copy(alpha = 0.8f) else NeonCyan.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun CoachCard(
    title: String,
    description: String,
    icon: ImageVector,
    actionText: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    val accentColor = if (highlight) ElectricMagenta else NeonCyan
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(actionText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
