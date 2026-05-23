package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlantingEvent::class,
        ScoutingLog::class,
        ExpenseItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WatermelonDatabase : RoomDatabase() {
    abstract fun watermelonDao(): WatermelonDao

    companion object {
        @Volatile
        private var INSTANCE: WatermelonDatabase? = null

        fun getDatabase(context: Context): WatermelonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WatermelonDatabase::class.java,
                    "watermelon_companion_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
