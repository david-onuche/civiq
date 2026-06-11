package com.civiq.app.data.remote.api

import com.civiq.app.data.remote.dto.ai.GeminiGenerateContentRequest
import com.civiq.app.data.remote.dto.ai.GeminiGenerateContentResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service for the Gemini `generateContent` REST endpoint.
 * The `?key=<GEMINI_API_KEY>` query parameter is added automatically by the
 * Gemini [okhttp3.Interceptor] configured in [com.civiq.app.di.NetworkModule].
 */
interface GeminiApiService {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Body request: GeminiGenerateContentRequest,
    ): GeminiGenerateContentResponse
}
