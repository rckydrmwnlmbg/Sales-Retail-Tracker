package com.example.logic

import android.graphics.Bitmap
import com.example.BuildConfig
import com.google.gson.Gson

object ScanBrochureUseCase {

    suspend fun scanBrochure(imageBitmap: Bitmap): ProductScanResult {
        val outputStream = java.io.ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)

        val prompt = """
            Kamu adalah sistem ekstraksi data produk jam tangan Casio.
            Analisa gambar brosur/katalog Casio ini dan ekstrack
            informasi produk yang terlihat.

            Kembalikan response HANYA dalam format JSON berikut,
            tanpa teks tambahan apapun:
            {
              "products": [
                {
                  "productCode": "kode produk (contoh: GA-2100-1A)",
                  "productName": "nama lengkap produk",
                  "category": "salah satu dari: G-Shock / Baby-G / Edifice / ProTrek / Vintage / Databank / Standard / Kalkulator / Lainnya",
                  "price": 0,
                  "features": ["fitur1", "fitur2"]
                }
              ],
              "confidence": "HIGH / MEDIUM / LOW",
              "notes": "catatan jika ada data yang tidak jelas"
            }

            Jika gambar bukan brosur Casio atau tidak ada data produk yang bisa dibaca, kembalikan:
            {
              "products": [],
              "confidence": "LOW",
              "notes": "alasan mengapa tidak bisa diekstrak"
            }
        """.trimIndent()

        try {
            val responseText = OpenRouterClient.queryOpenRouterMultimodal(prompt, base64String)
            val jsonText = responseText.trim()
            return parseProductScanResult(jsonText)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw Exception(e.message ?: "Gagal memproses gambar. Mohon ulangi beberapa saat lagi.")
        }
    }

    private fun parseProductScanResult(jsonText: String): ProductScanResult {
        try {
            val cleanJson = jsonText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            return Gson().fromJson(cleanJson, ProductScanResult::class.java)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw Exception("Gagal memproses format data (JSON). Mohon ulangi.")
        }
    }
}
