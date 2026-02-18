package com.tcm.app.data.local.dao

import androidx.room.*
import com.tcm.app.data.local.entity.Herb
import kotlinx.coroutines.flow.Flow

@Dao
interface HerbDao {
    @Query("SELECT * FROM herbs WHERE prescriptionId = :prescriptionId ORDER BY sequence")
    fun getHerbsByPrescriptionId(prescriptionId: Long): Flow<List<Herb>>

    @Query("SELECT * FROM herbs WHERE prescriptionId = :prescriptionId ORDER BY sequence")
    suspend fun getHerbsByPrescriptionIdSync(prescriptionId: Long): List<Herb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHerb(herb: Herb): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHerbs(herbs: List<Herb>)

    @Update
    suspend fun updateHerb(herb: Herb)

    @Delete
    suspend fun deleteHerb(herb: Herb)

    @Query("DELETE FROM herbs WHERE prescriptionId = :prescriptionId")
    suspend fun deleteHerbsByPrescriptionId(prescriptionId: Long)

    @Query("SELECT DISTINCT name FROM herbs ORDER BY name")
    suspend fun getAllHerbNames(): List<String>
}
