package com.tcm.app.data.remote

import com.tcm.app.data.remote.model.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApi {
    @POST("v1/chat/completions")
    suspend fun performOcr(
        @Header("Authorization") authorization: String,
        @Body request: OcrRequest
    ): OcrResponse

    @POST("v1/chat/completions")
    suspend fun sendChatMessage(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse
}
