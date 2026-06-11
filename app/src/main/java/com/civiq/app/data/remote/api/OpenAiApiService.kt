package com.civiq.app.data.remote.api

import com.civiq.app.data.remote.dto.ai.OpenAiChatRequest
import com.civiq.app.data.remote.dto.ai.OpenAiChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit service for OpenAI's Chat Completions endpoint.
 * The `Authorization: Bearer <OPENAI_API_KEY>` header is added automatically
 * by the OpenAI [okhttp3.Interceptor] configured in [com.civiq.app.di.NetworkModule].
 */
interface OpenAiApiService {

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(@Body request: OpenAiChatRequest): OpenAiChatResponse
}
