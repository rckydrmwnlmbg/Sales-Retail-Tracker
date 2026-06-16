package com.example.logic

import android.util.Log
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.ColleagueEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object SupabaseSyncHelper {
    private const val TAG = "SupabaseSync"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
        
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val gson = Gson()

    suspend fun backupDataToCloud(
        supabaseUrl: String, 
        supabaseKey: String,
        products: List<ProductEntity>,
        activities: List<ActivityEntity>,
        goals: List<GoalEntity>,
        colleagues: List<ColleagueEntity>
    ) = withContext(Dispatchers.IO) {
        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty()) {
            throw Exception("Supabase URL atau Key kosong. Silakan isi di menu Profil.")
        }
        
        try {
            // Upsert Products
            if (products.isNotEmpty()) {
                val pJson = gson.toJson(products)
                executePost("$supabaseUrl/rest/v1/products?on_conflict=id", supabaseKey, pJson)
            }

            // Upsert Activities
            if (activities.isNotEmpty()) {
                val aJson = gson.toJson(activities)
                executePost("$supabaseUrl/rest/v1/activities?on_conflict=id", supabaseKey, aJson)
            }
            
            // Upsert Goals
            if (goals.isNotEmpty()) {
                val gJson = gson.toJson(goals)
                executePost("$supabaseUrl/rest/v1/goals?on_conflict=monthYear", supabaseKey, gJson)
            }
            
            // Upsert Colleagues
            if (colleagues.isNotEmpty()) {
                val cJson = gson.toJson(colleagues)
                executePost("$supabaseUrl/rest/v1/colleagues?on_conflict=id", supabaseKey, cJson)
            }
            
            Log.d(TAG, "Backup to Supabase successful.")
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up to Supabase: ${e.message}")
            throw e
        }
    }
    
    suspend fun syncOfflineData(
        supabaseUrl: String, 
        supabaseKey: String,
        activities: List<ActivityEntity>
    ) = withContext(Dispatchers.IO) {
        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty()) {
            throw Exception("Supabase URL atau Key kosong. Silakan isi di menu Profil.")
        }
        
        // Sync offline activities (in a real app we'd track "synced" status)
        try {
            if (activities.isNotEmpty()) {
                val aJson = gson.toJson(activities)
                executePost("$supabaseUrl/rest/v1/activities?on_conflict=id", supabaseKey, aJson)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Supabase: ${e.message}")
            throw e
        }
    }

    private fun executePost(url: String, apiKey: String, jsonBody: String) {
        val requestBody = jsonBody.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "resolution=merge-duplicates")
            .post(requestBody)
            .build()
            
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val responseBody = response.body?.string() ?: ""
            throw Exception("Failed with code ${response.code}: $responseBody")
        }
    }
}
