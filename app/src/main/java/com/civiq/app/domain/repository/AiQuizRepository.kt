package com.civiq.app.domain.repository

import com.civiq.app.domain.model.AiQuestionRequest
import com.civiq.app.domain.model.Question
import com.civiq.app.utils.Resource

/**
 * Abstraction over AI question generation providers (Gemini, OpenAI).
 * Implementations should attempt the configured primary provider, fall back
 * to a secondary provider on failure, and ultimately fall back to bundled
 * static content (see `FallbackQuestionProvider`) so the quiz experience
 * never fully breaks.
 */
interface AiQuizRepository {
    suspend fun generateQuestions(request: AiQuestionRequest): Resource<List<Question>>
}
