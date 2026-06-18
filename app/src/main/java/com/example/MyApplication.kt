package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository

class MyApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler(this)
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "casio_sales_db"
        ).fallbackToDestructiveMigration().build()
        repository = AppRepository(database.appDao())
    }
}
