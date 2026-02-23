package com.tcm.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,  // 升级数据库版本
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

        // 从版本1升级到版本2的迁移：添加patientName字段
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prescriptions ADD COLUMN patientName TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): TcmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TcmDatabase::class.java,
                    "tcm_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
