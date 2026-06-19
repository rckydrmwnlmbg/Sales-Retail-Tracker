package com.example.logic

import com.example.BuildConfig
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.GoalEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CoachRecommendations(
    val summary: String,
    val tips: List<String>,
    val knowledgeGaps: List<KnowledgeGap>
)

data class KnowledgeGap(
    val topic: String,
    val priority: String, // "High", "Medium", "Low"
    val isHigh: Boolean
)

object AiCoachUseCase {
    suspend fun generateCoachingInsights(
        activities: List<ActivityEntity>,
        goal: GoalEntity?,
        revenue: Double,
        apiKey: String
    ): CoachRecommendations = withContext(Dispatchers.IO) {
        val target = goal?.personalTarget ?: 0.0
        val targetHit = if (target > 0) String.format("%.1f", (revenue / target) * 100) else "0.0"

        val format = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
        val activitiesSummary = activities.takeLast(10).joinToString("\n") {
            "- ${format.format(java.util.Date(it.timestamp))}: ${it.type} ${it.notes ?: ""} | Rp ${it.finalPrice ?: it.price ?: 0.0}"
        }
        val shiftsCount = activities.count { it.type == "CLOCK_IN" }

        val prompt = """
            Kamu adalah AI Coach berbahasa Indonesia untuk seorang Sales Executive jam tangan Casio.
            Berikut adalah data performa saat ini (menyesuaikan rentang waktu perhitungan perusahaan 10 sampai 10):
            - Target Personal: Rp $target
            - Pencapaian Saat Ini: Rp $revenue ($targetHit%)
            
            Informasi Shift / Kehadiran:
            - Jumlah Shift Masuk (CLOCK_IN): $shiftsCount
            
            Berikut adalah 10 aktivitas (shift & operasional/sales) terakhir:
            $activitiesSummary
            
            Berdasarkan data di atas (khususnya perhatikan keteraturan/konsistensi shift dan pencapaian sales), tolong berikan coaching advice singkat, rangkuman performa, dan rekomendasikan topik edukasi (knowledge gaps).
            
            Kembalikan response HANYA dalam format JSON berikut (tanpa blok ```json):
            {
              "summary": "Teks paragraf singkat rangkuman performa shift terakhir dan motivasi pencapaian target.",
              "tips": ["Tip 1", "Tip 2", "Tip 3"],
              "knowledgeGaps": [
                {
                  "topic": "Topik spesifik seputar G-Shock, Edifice, Baby-G, atau teknik reselling",
                  "priority": "High / Medium / Low",
                  "isHigh": true/false
                }
              ]
            }
        """.trimIndent()

        try {
            val responseText = OpenRouterClient.queryOpenRouter(prompt, apiKey)
            val jsonText = responseText.replace("```json", "").replace("```", "").trim()
            Gson().fromJson(jsonText, CoachRecommendations::class.java)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw Exception(e.message ?: "Gagal mendapatkan rekomendasi AI. Mohon cek koneksi atau API Key Anda.")
        }
    }
}
