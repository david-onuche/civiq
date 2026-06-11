package com.civiq.app.domain.model

import com.civiq.app.utils.AiConfig

/**
 * Parameters for an AI-generated question batch. Passed from
 * [com.civiq.app.domain.repository.AiQuizRepository] through to the prompt
 * template builder (`AiPromptTemplates`) used by the Gemini/OpenAI services.
 */
data class AiQuestionRequest(
    val category: QuizCategory,
    val difficulty: QuestionDifficulty,
    val tone: QuestionTone = QuestionTone.EDUCATIONAL,
    val countryCode: String? = null,
    val count: Int = AiConfig.DEFAULT_QUESTION_COUNT,
    val type: QuestionType? = null,
    val topicHint: String? = null,
)
