package com.tcm.app.data.remote.model

import com.google.gson.annotations.SerializedName

// OCR Request/Response Models
data class OcrRequest(
    val model: String = "MiniMax-Text-01",
    val messages: List<OcrMessage>,
    val temperature: Double = 0.1,
    val max_tokens: Int = 2000,
    val response_format: ResponseFormat? = null  // MiniMax-Text-01 仅支持 json_schema，不传则靠 prompt 约束 JSON
)

data class OcrMessage(
    val role: String = "user",
    val content: List<ContentItem>
)

data class ContentItem(
    val type: String,  // "text" or "image_url"
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String  // base64 data URL
)

data class ResponseFormat(
    val type: String = "json_object"
)

// OCR Response
data class OcrResponse(
    val id: String = "",
    val choices: List<Choice>? = null,  // Gson可能返回null，必须可空
    val usage: Usage? = null
)

data class Choice(
    val message: ResponseMessage? = null,
    val finish_reason: String = ""
)

data class ResponseMessage(
    val role: String = "",
    val content: String = ""  // JSON string
)

data class Usage(
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val total_tokens: Int = 0
)

// Parsed OCR Result
data class OcrResult(
    @SerializedName("prescription_name")
    val prescriptionName: String = "",
    @SerializedName("patient_name")
    val patientName: String? = null,  // Gson可能返回null
    val herbs: List<OcrHerb>? = null,  // Gson可能返回null
    val usage: OcrUsage? = null,       // Gson可能返回null
    val indications: String = "",
    @SerializedName("special_notes")
    val specialNotes: String = "",
    val confidence: Float = 0f
)

data class OcrHerb(
    val name: String,
    val dosage: String = "",
    val preparation: String = ""
)

data class OcrUsage(
    @SerializedName("decoction")
    val decoctionMethod: String = "",
    val frequency: String = "",
    @SerializedName("dosage_per_time")
    val dosagePerTime: String = ""
)

// Chat Request/Response
data class ChatRequest(
    val model: String = "MiniMax-M2.5",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2000,
    val stream: Boolean = false
)

data class ChatMessage(
    val role: String,  // system, user, assistant
    val content: String
)

data class ChatResponse(
    val id: String = "",
    val choices: List<ChatChoice>? = null,  // Gson可能返回null
    val usage: Usage? = null
)

data class ChatChoice(
    val message: ChatMessage? = null,
    val finish_reason: String = ""
)
