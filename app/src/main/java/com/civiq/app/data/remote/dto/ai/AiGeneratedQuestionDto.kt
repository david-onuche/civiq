package com.civiq.app.data.remote.dto.ai

import kotlinx.serialization.Serializable

/**
 * Shape of a single question as returned by an AI provider's JSON response.
 * Both Gemini (`responseMimeType = "application/json"`) and OpenAI
 * (`response_format = json_object`) are prompted (see `AiPromptTemplates`)
 * to return an object matching [AiGeneratedQuestionSetDto].
 */
@Serializable
data class AiGeneratedQuestionDto(
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String = "",
    val type: String = "MULTIPLE_CHOICE",
    val tone: String = "EDUCATIONAL",
)

/** Root JSON object requested from AI providers: a batch of generated questions. */
@Serializable
data class AiGeneratedQuestionSetDto(
    val questions: List<AiGeneratedQuestionDto> = emptyList(),
)
