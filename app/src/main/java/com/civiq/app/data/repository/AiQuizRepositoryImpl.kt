package com.civiq.app.data.repository

import com.civiq.app.BuildConfig
import com.civiq.app.R
import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.remote.ai.AiPromptTemplates
import com.civiq.app.data.remote.api.GeminiApiService
import com.civiq.app.data.remote.api.OpenAiApiService
import com.civiq.app.data.remote.dto.ai.AiGeneratedQuestionSetDto
import com.civiq.app.data.remote.dto.ai.GeminiContent
import com.civiq.app.data.remote.dto.ai.GeminiGenerateContentRequest
import com.civiq.app.data.remote.dto.ai.GeminiGenerationConfig
import com.civiq.app.data.remote.dto.ai.GeminiPart
import com.civiq.app.data.remote.dto.ai.OpenAiChatRequest
import com.civiq.app.data.remote.dto.ai.OpenAiMessage
import com.civiq.app.data.remote.dto.ai.OpenAiResponseFormat
import com.civiq.app.data.remote.dto.ai.firstCandidateText
import com.civiq.app.data.remote.dto.ai.firstChoiceContent
import com.civiq.app.domain.model.AiQuestionRequest
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.repository.AiQuizRepository
import com.civiq.app.utils.AiConfig
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates quiz questions for [AiQuestionRequest]s by attempting Gemini
 * first, falling back to OpenAI if Gemini is unavailable or fails, and
 * finally falling back to [FallbackQuestionProvider]'s bundled curated
 * content if neither AI provider produces a usable result.
 */
@Singleton
class AiQuizRepositoryImpl @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val openAiApiService: OpenAiApiService,
    private val json: Json,
) : AiQuizRepository {

    override suspend fun generateQuestions(request: AiQuestionRequest): Resource<List<Question>> {
        generateWithGemini(request)?.let { return Resource.Success(it) }
        generateWithOpenAi(request)?.let { return Resource.Success(it) }

        val fallback = FallbackQuestionProvider.getQuestions(request)
        return if (fallback.isNotEmpty()) {
            Resource.Success(fallback)
        } else {
            Resource.Error(UiText.StringResource(R.string.common_error_generic))
        }
    }

    private suspend fun generateWithGemini(request: AiQuestionRequest): List<Question>? {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) return null
        return try {
            val prompt = AiPromptTemplates.buildQuestionGenerationPrompt(request)
            val response = geminiApiService.generateContent(
                model = AiConfig.GEMINI_MODEL,
                request = GeminiGenerateContentRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(
                        temperature = AiConfig.DEFAULT_TEMPERATURE,
                        responseMimeType = "application/json",
                    ),
                ),
            )
            parseQuestions(response.firstCandidateText(), request)
        } catch (e: Exception) {
            Timber.w(e, "Gemini question generation failed")
            null
        }
    }

    private suspend fun generateWithOpenAi(request: AiQuestionRequest): List<Question>? {
        if (BuildConfig.OPENAI_API_KEY.isBlank()) return null
        return try {
            val prompt = AiPromptTemplates.buildQuestionGenerationPrompt(request)
            val response = openAiApiService.createChatCompletion(
                OpenAiChatRequest(
                    model = AiConfig.OPENAI_MODEL,
                    messages = listOf(OpenAiMessage(role = "user", content = prompt)),
                    temperature = AiConfig.DEFAULT_TEMPERATURE,
                    responseFormat = OpenAiResponseFormat(type = "json_object"),
                ),
            )
            parseQuestions(response.firstChoiceContent(), request)
        } catch (e: Exception) {
            Timber.w(e, "OpenAI question generation failed")
            null
        }
    }

    /** Parses [rawJson] as an [AiGeneratedQuestionSetDto] and maps it to domain [Question]s, or null if unusable. */
    private fun parseQuestions(rawJson: String?, request: AiQuestionRequest): List<Question>? {
        if (rawJson.isNullOrBlank()) return null
        return try {
            json.decodeFromString<AiGeneratedQuestionSetDto>(rawJson)
                .questions
                .filter { it.questionText.isNotBlank() && it.options.size >= 2 }
                .map { it.toDomain(request) }
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse AI-generated question response")
            null
        }
    }
}
