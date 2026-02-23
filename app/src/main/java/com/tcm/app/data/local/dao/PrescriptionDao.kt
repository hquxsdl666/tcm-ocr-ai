package com.tcm.app.data.local.dao

import androidx.room.*
import com.tcm.app.data.local.entity.Prescription
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY createdAt DESC")
    fun getAllPrescriptions(): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionById(id: Long): Prescription?

    @Query("""
        SELECT DISTINCT p.* FROM prescriptions p
        INNER JOIN herbs h ON p.id = h.prescriptionId
        WHERE h.name LIKE '%' || :herbName || '%'
        ORDER BY p.createdAt DESC
    """)
    fun searchByHerbName(herbName: String): Flow<List<Prescription>>

    @Query("""
        SELECT * FROM prescriptions
        WHERE name LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        OR patientName LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchByNameOrDescription(query: String): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription): Long

    @Update
    suspend fun updatePrescription(prescription: Prescription)

    @Delete
    suspend fun deletePrescription(prescription: Prescription)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescriptionById(id: Long)

    @Query("SELECT COUNT(*) FROM prescriptions")
    suspend fun getPrescriptionCount(): Int

    // 统计相关查询
    @Query("SELECT COUNT(*) FROM prescriptions")
    fun getPrescriptionCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM prescriptions WHERE isAiGenerated = 1")
    fun getAiGeneratedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM prescriptions WHERE createdAt >= :startDate")
    suspend fun getPrescriptionCountSince(startDate: Long): Int

    @Query("SELECT * FROM prescriptions ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentPrescriptions(limit: Int): List<Prescription>
}
