package com.tcm.app.data.remote

import com.tcm.app.data.remote.model.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApi {
    /** Kimi 视觉识别（图片 OCR）使用 moonshot-v1-8k-vision-preview */
    @POST("v1/chat/completions")
    suspend fun performOcr(
        @Header("Authorization") authorization: String,
        @Body request: OcrRequest
    ): OcrResponse

    /** Kimi 文本对话使用 moonshot-v1-8k */
    @POST("v1/chat/completions")
    suspend fun sendChatMessage(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}
