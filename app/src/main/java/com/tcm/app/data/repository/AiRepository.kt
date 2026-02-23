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
import kotlinx.coroutines.flow.first

class AiRepository(
    private val api: DeepSeekApi,
    private val chatHistoryDao: ChatHistoryDao,
    private val context: Context,
    private val prescriptionRepository: PrescriptionRepository? = null  // 添加方剂库仓库引用
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
        prescriptionContext: List<PrescriptionWithDetails> = emptyList()
    ): Result<String> {
        return try {
            // Build comprehensive context from prescription library
            val contextMessage = buildPrescriptionContext(prescriptionContext)

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

    /**
     * 构建详细的方剂库上下文，包含完整的药材信息
     */
    private fun buildPrescriptionContext(prescriptions: List<PrescriptionWithDetails>): String {
        if (prescriptions.isEmpty()) return ""
        
        return buildString {
            append("\n\n【我的方剂库信息】\n")
            append("您共有 ${prescriptions.size} 个保存的方剂，以下是详细信息：\n\n")
            
            prescriptions.take(15).forEachIndexed { index, prescriptionWithDetails ->
                val p = prescriptionWithDetails.prescription
                append("${index + 1}. 方剂：${p.name}\n")
                
                // 患者信息
                if (p.patientName.isNotBlank()) {
                    append("   患者：${p.patientName}\n")
                }
                
                // 药材组成
                if (prescriptionWithDetails.herbs.isNotEmpty()) {
                    append("   组成：")
                    val herbStr = prescriptionWithDetails.herbs
                        .sortedBy { it.sequence }
                        .map { herb ->
                            buildString {
                                append(herb.name)
                                if (herb.dosage.isNotBlank()) append(" ${herb.dosage}")
                                if (herb.preparation.isNotBlank()) append("(${herb.preparation})")
                            }
                        }
                        .joinToString("、")
                    append(herbStr)
                    append("\n")
                }
                
                // 用法用量
                prescriptionWithDetails.usageInstruction?.let { usage ->
                    if (usage.decoctionMethod.isNotBlank() || usage.frequency.isNotBlank()) {
                        append("   用法：")
                        val usageParts = mutableListOf<String>()
                        if (usage.decoctionMethod.isNotBlank()) usageParts.add(usage.decoctionMethod)
                        if (usage.frequency.isNotBlank()) usageParts.add(usage.frequency)
                        if (usage.dosagePerTime.isNotBlank()) usageParts.add("每次${usage.dosagePerTime}")
                        append(usageParts.joinToString("，"))
                        append("\n")
                    }
                }
                
                // 主治功效
                if (p.description.isNotBlank()) {
                    append("   功效：${p.description}\n")
                }
                
                // 适用症状
                if (prescriptionWithDetails.symptoms.isNotEmpty()) {
                    append("   适用症状：${prescriptionWithDetails.symptoms.joinToString("、") { it.symptom }}\n")
                }
                
                // 来源标记
                if (p.isAiGenerated) {
                    append("   [AI生成]")
                }
                
                append("\n")
            }
            
            if (prescriptions.size > 15) {
                append("... 还有 ${prescriptions.size - 15} 个方剂\n")
            }
            
            append("\n请基于以上我的方剂库信息进行分析和回答。可以参考这些方剂的组成、功效和适用症状来提供更个性化的建议。")
        }
    }

    suspend fun recommendPrescription(
        symptoms: String,
        constitution: String,
        ageGender: String,
        prescriptionLibrary: List<PrescriptionWithDetails>
    ): Result<String> {
        val prompt = buildString {
            append("你是一位经验丰富的中医师。基于以下患者信息和我的方剂库，请推荐合适的方剂。\n\n")
            append("【患者信息】\n")
            append("症状：$symptoms\n")
            append("体质：$constitution\n")
            append("性别年龄：$ageGender\n\n")
            
            // 添加详细的方剂库信息
            if (prescriptionLibrary.isNotEmpty()) {
                append("【我的方剂库】\n")
                prescriptionLibrary.take(10).forEach { p ->
                    append("- ${p.prescription.name}")
                    if (p.prescription.patientName.isNotBlank()) {
                        append(" (患者: ${p.prescription.patientName})")
                    }
                    append("\n")
                    
                    if (p.herbs.isNotEmpty()) {
                        append("  组成: ${p.herbs.sortedBy { it.sequence }.joinToString(", ") { "${it.name}${if (it.dosage.isNotBlank()) " ${it.dosage}" else ""}" }}\n")
                    }
                    if (p.prescription.description.isNotBlank()) {
                        append("  功效: ${p.prescription.description}\n")
                    }
                    append("\n")
                }
                append("\n")
            }
            
            append("请给出：\n")
            append("1. 推荐的方剂组成（药材+剂量）\n")
            append("2. 方剂功效\n")
            append("3. 方解（为何选择这些药材）\n")
            append("4. 注意事项\n")
            append("5. 与我历史方剂的关联性分析（如有相似方剂请说明）\n")
            append("6. 是否推荐参考我历史方剂库中的某个方剂并加减使用")
        }

        return getAiResponse(listOf(ChatMessage(role = "user", content = prompt)), prescriptionLibrary)
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

    // 类型别名，用于表示包含详细信息的方剂
    data class PrescriptionWithDetails(
        val prescription: Prescription,
        val herbs: List<com.tcm.app.data.local.entity.Herb> = emptyList(),
        val usageInstruction: com.tcm.app.data.local.entity.UsageInstruction? = null,
        val symptoms: List<com.tcm.app.data.local.entity.Symptom> = emptyList()
    )
}
