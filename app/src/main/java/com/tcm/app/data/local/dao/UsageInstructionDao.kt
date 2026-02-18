package com.tcm.app.data.local.dao

import androidx.room.*
import com.tcm.app.data.local.entity.UsageInstruction
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageInstructionDao {
    @Query("SELECT * FROM usage_instructions WHERE prescriptionId = :prescriptionId LIMIT 1")
    fun getUsageInstructionByPrescriptionId(prescriptionId: Long): Flow<UsageInstruction?>

    @Query("SELECT * FROM usage_instructions WHERE prescriptionId = :prescriptionId LIMIT 1")
    suspend fun getUsageInstructionByPrescriptionIdSync(prescriptionId: Long): UsageInstruction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageInstruction(usageInstruction: UsageInstruction): Long

    @Update
    suspend fun updateUsageInstruction(usageInstruction: UsageInstruction)

    @Delete
    suspend fun deleteUsageInstruction(usageInstruction: UsageInstruction)

    @Query("DELETE FROM usage_instructions WHERE prescriptionId = :prescriptionId")
    suspend fun deleteUsageInstructionByPrescriptionId(prescriptionId: Long)
}
