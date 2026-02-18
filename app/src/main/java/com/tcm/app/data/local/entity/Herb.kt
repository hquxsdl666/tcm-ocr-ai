package com.tcm.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "herbs",
    foreignKeys = [
        ForeignKey(
            entity = Prescription::class,
            parentColumns = ["id"],
            childColumns = ["prescriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("prescriptionId")]
)
data class Herb(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prescriptionId: Long,
    val name: String,
    val dosage: String = "",
    val preparation: String = "",
    val sequence: Int = 0
)
