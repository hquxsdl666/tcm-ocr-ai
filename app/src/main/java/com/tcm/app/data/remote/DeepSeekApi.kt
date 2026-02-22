package com.tcm.app.data.remote

import com.tcm.app.data.remote.model.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApi {
    /** MiniMax 多模态（图片识别）使用 chatcompletion_v2 + MiniMax-Text-01 */
    @POST("v1/text/chatcompletion_v2")
    suspend fun performOcr(
        @Header("Authorization") authorization: String,
        @Body request: OcrRequest
    ): OcrResponse

    /** MiniMax 文本对话使用 MiniMax-M2.5 */
    @POST("v1/text/chatcompletion_v2")
    suspend fun sendChatMessage(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}
