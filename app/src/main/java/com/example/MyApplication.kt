package com.example

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository

class MyApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler(this)
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE activities ADD COLUMN quantity INTEGER")
                database.execSQL("ALTER TABLE activities ADD COLUMN discount REAL")
                database.execSQL("ALTER TABLE activities ADD COLUMN finalPrice REAL")
            }
        }
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "casio_sales_db"
        )
        .addMigrations(MIGRATION_4_5)
        .fallbackToDestructiveMigration()
        .build()
        repository = AppRepository(database.appDao())
    }
}
