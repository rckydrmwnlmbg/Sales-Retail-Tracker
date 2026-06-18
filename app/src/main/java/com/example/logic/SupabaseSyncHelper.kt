package com.example.logic

import android.util.Log
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.ColleagueEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.FieldNamingPolicy
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
    private val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private fun buildEndpoint(baseUrl: String, path: String): String {
        var cleanUrl = baseUrl.trim()
        if (cleanUrl.endsWith("/")) cleanUrl = cleanUrl.dropLast(1)
        if (cleanUrl.endsWith("/rest/v1")) cleanUrl = cleanUrl.dropLast(8)
        return "$cleanUrl/rest/v1$path"
    }

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
        
        val errors = mutableListOf<String>()
        try {
            // Upsert Products
            if (products.isNotEmpty()) {
                val pJson = gson.toJson(products)
                try {
                    executePost(buildEndpoint(supabaseUrl, "/products?on_conflict=id"), supabaseKey, pJson)
                } catch (e: Exception) {
                    errors.add("Products: ${e.message}")
                }
            }

            // Upsert Activities
            if (activities.isNotEmpty()) {
                val aJson = gson.toJson(activities)
                try {
                    executePost(buildEndpoint(supabaseUrl, "/activities?on_conflict=id"), supabaseKey, aJson)
                } catch (e: Exception) {
                    errors.add("Activities: ${e.message}")
                }
            }
            
            // Upsert Goals
            if (goals.isNotEmpty()) {
                val gJson = gson.toJson(goals)
                try {
                    executePost(buildEndpoint(supabaseUrl, "/goals?on_conflict=month_year"), supabaseKey, gJson)
                } catch (e: Exception) {
                    errors.add("Goals: ${e.message}")
                }
            }
            
            // Upsert Colleagues
            if (colleagues.isNotEmpty()) {
                val cJson = gson.toJson(colleagues)
                try {
                    executePost(buildEndpoint(supabaseUrl, "/colleagues?on_conflict=id"), supabaseKey, cJson)
                } catch (e: Exception) {
                    errors.add("Colleagues: ${e.message}")
                }
            }
            
            Log.d(TAG, "Backup to Supabase finished. Errors: $errors")
            if (errors.isNotEmpty()) {
                throw Exception(errors.joinToString(" | "))
            }
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
                executePost(buildEndpoint(supabaseUrl, "/activities?on_conflict=id"), supabaseKey, aJson)
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
