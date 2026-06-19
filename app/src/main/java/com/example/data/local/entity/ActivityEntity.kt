package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // SALE, INTEREST, QUESTION, LOST, AVAILABILITY, QUICK_NOTE, RECORD, LEARNING
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    
    // Core Sale Info
    val productId: Int? = null,
    val price: Double? = null, // original price
    val quantity: Int? = null,
    val discount: Double? = null, // discount in percentage
    val finalPrice: Double? = null,
    val creditedToId: Int? = null,
    val creditedFromId: Int? = null,
    
    // Interest Info
    val customerType: String? = null,
    
    // Question Info
    val questionCategory: String? = null,
    
    // Lost Info
    val lostReason: String? = null,
    val estimatedValue: Double? = null,
    
    // Availability Info
    val availabilityStatus: String? = null,
    
    // Note / Learning
    val topic: String? = null,
    val learningContext: String? = null,
    val learningLesson: String? = null,
    
    // Audit Trail
    val isCorrection: Boolean = false,
    val originalActivityId: Int? = null,
    val correctionReason: String? = null
)
