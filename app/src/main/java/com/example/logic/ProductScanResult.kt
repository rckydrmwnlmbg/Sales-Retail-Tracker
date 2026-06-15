package com.example.logic

import androidx.annotation.Keep
import com.example.data.local.entity.ProductEntity

@Keep
data class ProductScanResult(
    val products: List<ScannedProduct> = emptyList(),
    val confidence: String = "LOW",
    val notes: String = ""
)

@Keep
data class ScannedProduct(
    val productCode: String,
    val productName: String,
    val category: String,
    val price: Double,
    val features: List<String> = emptyList()
) {
    fun toProductEntity() = ProductEntity(
        code = productCode,
        name = productName,
        category = category,
        normalPrice = price,
        isActive = true
    )
}
