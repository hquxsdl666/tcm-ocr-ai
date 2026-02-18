package com.tcm.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "chat_history",
    foreignKeys = [
        ForeignKey(
            entity = Prescription::class,
            parentColumns = ["id"],
            childColumns = ["prescriptionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("prescriptionId")]
)
data class ChatHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // user, assistant, system
    val content: String,
    val prescriptionId: Long? = null,
    val timestamp: Date = Date()
)
