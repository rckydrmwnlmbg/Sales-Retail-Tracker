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
        
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN creditedFromId INTEGER DEFAULT NULL")
            }
        }

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "casio_sales_db"
        ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build()
        repository = AppRepository(database.appDao())
    }
}
