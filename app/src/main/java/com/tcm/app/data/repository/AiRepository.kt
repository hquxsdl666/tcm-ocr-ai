package com.tcm.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.tcm.app.data.local.dao.ChatHistoryDao
import com.tcm.app.data.local.entity.ChatHistory
import com.tcm.app.data.local.entity.Prescription
import com.tcm.app.data.remote.DeepSeekApi
import com.tcm.app.data.remote.model.*
import com.tcm.app.utils.Constants
import com.tcm.app.utils.ImageUtils
import com.tcm.app.utils.JsonParser
import kotlinx.coroutines.flow.Flow

class AiRepository(
    private val api: DeepSeekApi,
    private val chatHistoryDao: ChatHistoryDao,
    private val context: Context
) {
    private fun getApiKey(): String {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        return (prefs.getString(Constants.PREFS_API_KEY, "") ?: "").trim()
    }

    suspend fun performOcr(bitmap: Bitmap): Result<OcrResult> {
        return try {
            val imageDataUrl = ImageUtils.preprocessImage(bitmap)
            val request = OcrRequest(
                messages = listOf(
                    OcrMessage(
                        content = listOf(
                            ContentItem(type = "text", text = Constants.OCR_PROMPT),
                            ContentItem(type = "image_url", image_url = ImageUrl(url = imageDataUrl))
                        )
                    )
                )
            )

            val response = api.performOcr("Bearer ${getApiKey()}", request)
            val content = response.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response"))

            // Extract JSON from markdown if present
            val jsonContent = JsonParser.extractJsonFromMarkdown(content)
            val ocrResult = JsonParser.parseOcrResult(jsonContent)
                ?: return Result.failure(Exception("Failed to parse OCR result"))

            Result.success(ocrResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAiResponse(
        messages: List<ChatMessage>,
        prescriptionContext: List<Prescription> = emptyList()
    ): Result<String> {
        return try {
            // Build context from prescription library
            val contextMessage = if (prescriptionContext.isNotEmpty()) {
                buildString {
                    append("以下是我的方剂库参考：\n")
                    prescriptionContext.forEach { prescription ->
                        append("- ${prescription.name}: ${prescription.description}\n")
                    }
                    append("\n请基于以上信息回答。")
                }
            } else ""

            val fullMessages = mutableListOf(
                ChatMessage(role = "system", content = Constants.AI_SYSTEM_PROMPT + contextMessage)
            )
            fullMessages.addAll(messages)

            val request = ChatRequest(messages = fullMessages)
            val response = api.sendChatMessage("Bearer ${getApiKey()}", request)
            
            val content = response.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response"))

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recommendPrescription(
        symptoms: String,
        constitution: String,
        ageGender: String,
        prescriptionLibrary: List<Prescription>
    ): Result<String> {
        val prompt = buildString {
            append("你是一位经验丰富的中医师。基于以下患者信息和我的方剂库，请推荐合适的方剂。\n\n")
            append("【患者信息】\n")
            append("症状：$symptoms\n")
            append("体质：$constitution\n")
            append("性别年龄：$ageGender\n\n")
            
            if (prescriptionLibrary.isNotEmpty()) {
                append("【我的方剂库】（仅作为参考，可创新组合）\n")
                prescriptionLibrary.take(10).forEach { prescription ->
                    append("- ${prescription.name}: ${prescription.description}\n")
                }
                append("\n")
            }
            
            append("请给出：\n")
            append("1. 推荐的方剂组成（药材+剂量）\n")
            append("2. 方剂功效\n")
            append("3. 方解（为何选择这些药材）\n")
            append("4. 注意事项\n")
            append("5. 推荐方剂与我历史方剂的关联性分析（如有）")
        }

        return getAiResponse(listOf(ChatMessage(role = "user", content = prompt)))
    }

    fun getChatHistory(): Flow<List<ChatHistory>> = chatHistoryDao.getAllChatHistory()

    suspend fun saveChatMessage(role: String, content: String, prescriptionId: Long? = null) {
        chatHistoryDao.insertChatMessage(
            ChatHistory(role = role, content = content, prescriptionId = prescriptionId)
        )
    }

    suspend fun clearChatHistory() {
        chatHistoryDao.deleteAllChatHistory()
    }

    suspend fun saveApiKey(apiKey: String) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(Constants.PREFS_API_KEY, apiKey.trim()).apply()
    }

    fun getSavedApiKey(): String {
        return getApiKey()
    }
}
