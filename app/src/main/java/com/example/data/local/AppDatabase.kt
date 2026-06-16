package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.AppDao
import com.example.data.local.entity.ActivityEntity
import com.example.data.local.entity.ColleagueEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        ColleagueEntity::class,
        ActivityEntity::class,
        GoalEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
