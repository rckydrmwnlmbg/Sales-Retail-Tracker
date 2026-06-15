package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val monthYear: String, // format "YYYY-MM"
    val shopTarget: Double,
    val groupTarget: Double,
    val personalTarget: Double
)
