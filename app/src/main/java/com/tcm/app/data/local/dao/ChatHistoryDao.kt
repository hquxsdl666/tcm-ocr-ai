package com.tcm.app.data.local.dao

import androidx.room.*
import com.tcm.app.data.local.entity.ChatHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllChatHistory(): Flow<List<ChatHistory>>

    @Query("SELECT * FROM chat_history WHERE prescriptionId = :prescriptionId OR prescriptionId IS NULL ORDER BY timestamp ASC")
    fun getChatHistoryByPrescriptionId(prescriptionId: Long?): Flow<List<ChatHistory>>

    @Query("SELECT * FROM chat_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentChatHistory(limit: Int): List<ChatHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(chatHistory: ChatHistory): Long

    @Delete
    suspend fun deleteChatMessage(chatHistory: ChatHistory)

    @Query("DELETE FROM chat_history")
    suspend fun deleteAllChatHistory()

    @Query("DELETE FROM chat_history WHERE timestamp < datetime('now', '-' || :days || ' days')")
    suspend fun deleteOldChatHistory(days: Int)
}
