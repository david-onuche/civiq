package com.civiq.app.data.remote.dto.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for OpenAI's `/v1/chat/completions` endpoint. */
@Serializable
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double? = null,
    @SerialName("response_format") val responseFormat: OpenAiResponseFormat? = null,
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String,
)

@Serializable
data class OpenAiResponseFormat(
    val type: String = "json_object",
)

/** Response body from OpenAI's `/v1/chat/completions` endpoint. */
@Serializable
data class OpenAiChatResponse(
    val choices: List<OpenAiChoice> = emptyList(),
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

/** The assistant's reply content from the first choice, or null if none exists. */
fun OpenAiChatResponse.firstChoiceContent(): String? = choices.firstOrNull()?.message?.content
