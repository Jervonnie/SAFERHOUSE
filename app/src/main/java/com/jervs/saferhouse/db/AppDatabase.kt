package com.jervs.saferhouse.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jervs.saferhouse.model.EmergencyContact
import com.jervs.saferhouse.model.SafetyLog

@Database(entities = [EmergencyContact::class, SafetyLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saferhouse_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
