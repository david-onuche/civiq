package com.civiq.app.data.remote.dto.ai

import kotlinx.serialization.Serializable

/** Request body for Gemini's `models/{model}:generateContent` REST endpoint. */
@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null,
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiPart(
    val text: String,
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val responseMimeType: String? = null,
)

/** Response body from Gemini's `generateContent` endpoint. */
@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null,
)

/** Concatenates the text of every part in the first candidate, or null if none exists. */
fun GeminiGenerateContentResponse.firstCandidateText(): String? =
    candidates.firstOrNull()?.content?.parts?.joinToString(separator = "") { it.text }
