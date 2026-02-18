package com.tcm.app.data.local.dao

import androidx.room.*
import com.tcm.app.data.local.entity.Symptom
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms WHERE prescriptionId = :prescriptionId")
    fun getSymptomsByPrescriptionId(prescriptionId: Long): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE prescriptionId = :prescriptionId")
    suspend fun getSymptomsByPrescriptionIdSync(prescriptionId: Long): List<Symptom>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: Symptom): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptoms: List<Symptom>)

    @Query("""
        SELECT DISTINCT p.* FROM prescriptions p
        INNER JOIN symptoms s ON p.id = s.prescriptionId
        WHERE s.symptom LIKE '%' || :symptom || '%'
        ORDER BY p.createdAt DESC
    """)
    fun searchBySymptom(symptom: String): Flow<List<com.tcm.app.data.local.entity.Prescription>>

    @Delete
    suspend fun deleteSymptom(symptom: Symptom)

    @Query("DELETE FROM symptoms WHERE prescriptionId = :prescriptionId")
    suspend fun deleteSymptomsByPrescriptionId(prescriptionId: Long)

    @Query("SELECT DISTINCT symptom FROM symptoms ORDER BY symptom")
    suspend fun getAllSymptoms(): List<String>
}
