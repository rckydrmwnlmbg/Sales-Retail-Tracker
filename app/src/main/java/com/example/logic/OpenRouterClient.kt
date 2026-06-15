package com.example.logic

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.GoalEntity
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

import org.json.JSONArray
import org.json.JSONObject

object OpenRouterClient {
    private const val TAG = "OpenRouterClient"
    private const val BASE_URL = "https://openrouter.ai/api/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun queryOpenRouter(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_OPENROUTER_API_KEY") {
            throw Exception("OpenRouter API Key belum di-set di Settings (Secrets). Silakan masukkan OPENROUTER_API_KEY.")
        }

        val requestBody = JSONObject().apply {
            put("model", "nex-agi/nex-n2-pro:free")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "com.ricky.salestracker")
            .addHeader("X-Title", "Ricky Sales Tracker")
            .post(requestBody.toRequestBody(mediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: code=${response.code} body=$bodyStr")
                    throw Exception("Gagal memanggil OpenRouter (HTTP ${response.code}). Response lengkap: $bodyStr")
                }
                if (bodyStr == null) {
                    throw Exception("Response dari OpenRouter kosong.")
                }

                // Parse the response content
                val gson = Gson()
                val jsonObject = gson.fromJson(bodyStr, JsonObject::class.java)
                val choices = jsonObject.getAsJsonArray("choices")
                if (choices == null || choices.size() == 0) {
                    throw Exception("Format response OpenRouter tidak sesuai (tidak berisi choices). Response lengkap: $bodyStr")
                }
                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                val content = message.get("content").asString
                return@withContext content
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in queryOpenRouter", e)
            throw e
        }
    }

    suspend fun queryOpenRouterMultimodal(prompt: String, base64Image: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_OPENROUTER_API_KEY") {
            throw Exception("OpenRouter API Key belum di-set di Settings (Secrets). Silakan masukkan OPENROUTER_API_KEY.")
        }

        val requestBodyMap = mapOf(
            "model" to "meta-llama/llama-3.1-8b-instruct:free",
            "messages" to listOf(
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf(
                            "type" to "text",
                            "text" to prompt
                        ),
                        mapOf(
                            "type" to "image_url",
                            "image_url" to mapOf(
                                "url" to "data:image/jpeg;base64,$base64Image"
                            )
                        )
                    )
                )
            )
        )

        val jsonString = Gson().toJson(requestBodyMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonString.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "com.ricky.salestracker")
            .addHeader("X-Title", "Ricky Sales Tracker")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: code=${response.code} body=$bodyStr")
                    throw Exception("Gagal memanggil OpenRouter (HTTP ${response.code}). Response lengkap: $bodyStr")
                }
                if (bodyStr == null) {
                    throw Exception("Response dari OpenRouter kosong.")
                }

                val gson = Gson()
                val jsonObject = gson.fromJson(bodyStr, JsonObject::class.java)
                val choices = jsonObject.getAsJsonArray("choices")
                if (choices == null || choices.size() == 0) {
                    throw Exception("Format response OpenRouter tidak sesuai. Response lengkap: $bodyStr")
                }
                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                val content = message.get("content").asString
                return@withContext content
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in queryOpenRouterMultimodal", e)
            throw e
        }
    }
}
