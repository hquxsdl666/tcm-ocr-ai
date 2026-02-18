package com.tcm.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tcm.app.data.local.dao.*
import com.tcm.app.data.local.entity.*

@Database(
    entities = [
        Prescription::class,
        Herb::class,
        UsageInstruction::class,
        Symptom::class,
        ChatHistory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TcmDatabase : RoomDatabase() {
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun herbDao(): HerbDao
    abstract fun usageInstructionDao(): UsageInstructionDao
    abstract fun symptomDao(): SymptomDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: TcmDatabase? = null

        fun getDatabase(context: Context): TcmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TcmDatabase::class.java,
                    "tcm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
