package com.tcm.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "prescriptions")
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val patientName: String = "",  // 患者名称
    val description: String = "",
    val source: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isAiGenerated: Boolean = false,
    val confidenceScore: Float = 0f
)
