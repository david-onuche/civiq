package com.civiq.app.data.repository

import com.civiq.app.BuildConfig
import com.civiq.app.R
import com.civiq.app.data.remote.ai.AiPromptTemplates
import com.civiq.app.data.remote.api.GeminiApiService
import com.civiq.app.data.remote.api.OpenAiApiService
import com.civiq.app.data.remote.dto.ai.GeminiContent
import com.civiq.app.data.remote.dto.ai.GeminiGenerateContentRequest
import com.civiq.app.data.remote.dto.ai.GeminiGenerationConfig
import com.civiq.app.data.remote.dto.ai.GeminiPart
import com.civiq.app.data.remote.dto.ai.OpenAiChatRequest
import com.civiq.app.data.remote.dto.ai.OpenAiMessage
import com.civiq.app.data.remote.dto.ai.firstCandidateText
import com.civiq.app.data.remote.dto.ai.firstChoiceContent
import com.civiq.app.domain.model.CoachMessage
import com.civiq.app.domain.model.CoachMessageRole
import com.civiq.app.domain.repository.AiCoachRepository
import com.civiq.app.utils.AiConfig
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Powers CiviQ's Premium "AI Learning Coach" chat by attempting Gemini first,
 * then falling back to OpenAI if Gemini is unavailable or fails. Unlike
 * [AiQuizRepositoryImpl], there is no bundled static fallback - a reply that
 * can't be generated is surfaced to the caller as an error.
 */
@Singleton
class AiCoachRepositoryImpl @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val openAiApiService: OpenAiApiService,
) : AiCoachRepository {

    override suspend fun sendMessage(history: List<CoachMessage>): Resource<String> {
        sendWithGemini(history)?.let { return Resource.Success(it) }
        sendWithOpenAi(history)?.let { return Resource.Success(it) }
        return Resource.Error(UiText.StringResource(R.string.common_error_generic))
    }

    private suspend fun sendWithGemini(history: List<CoachMessage>): String? {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) return null
        return try {
            val response = geminiApiService.generateContent(
                model = AiConfig.GEMINI_MODEL,
                request = GeminiGenerateContentRequest(
                    contents = history.map { it.toGeminiContent() },
                    generationConfig = GeminiGenerationConfig(temperature = AiConfig.DEFAULT_TEMPERATURE),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = AiPromptTemplates.buildCoachSystemPrompt()))),
                ),
            )
            response.firstCandidateText()?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.w(e, "Gemini coach reply failed")
            null
        }
    }

    private suspend fun sendWithOpenAi(history: List<CoachMessage>): String? {
        if (BuildConfig.OPENAI_API_KEY.isBlank()) return null
        return try {
            val messages = listOf(OpenAiMessage(role = "system", content = AiPromptTemplates.buildCoachSystemPrompt())) +
                history.map { it.toOpenAiMessage() }
            val response = openAiApiService.createChatCompletion(
                OpenAiChatRequest(
                    model = AiConfig.OPENAI_MODEL,
                    messages = messages,
                    temperature = AiConfig.DEFAULT_TEMPERATURE,
                ),
            )
            response.firstChoiceContent()?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.w(e, "OpenAI coach reply failed")
            null
        }
    }

    private fun CoachMessage.toGeminiContent(): GeminiContent = GeminiContent(
        role = if (role == CoachMessageRole.COACH) "model" else "user",
        parts = listOf(GeminiPart(text = content)),
    )

    private fun CoachMessage.toOpenAiMessage(): OpenAiMessage = OpenAiMessage(
        role = if (role == CoachMessageRole.COACH) "assistant" else "user",
        content = content,
    )
}
