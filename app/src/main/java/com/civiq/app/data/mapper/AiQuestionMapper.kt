package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.ai.AiGeneratedQuestionDto
import com.civiq.app.domain.model.AiQuestionRequest
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionSource
import com.civiq.app.domain.model.QuestionType
import com.civiq.app.utils.safeEnumValueOf

/**
 * Maps a single question from an AI provider's JSON response to the domain
 * [Question] model, filling in fields ([category], [QuestionDifficulty][com.civiq.app.domain.model.QuestionDifficulty],
 * [countryCode][Question.countryCode]) from the originating [request] since
 * the AI response only describes the question content itself.
 */
fun AiGeneratedQuestionDto.toDomain(request: AiQuestionRequest): Question = Question(
    id = "",
    type = safeEnumValueOf(type, request.type ?: QuestionType.MULTIPLE_CHOICE),
    category = request.category,
    difficulty = request.difficulty,
    questionText = questionText,
    options = options,
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    tone = safeEnumValueOf(tone, request.tone),
    countryCode = request.countryCode,
    tags = emptyList(),
    source = QuestionSource.AI_GENERATED,
    createdAt = System.currentTimeMillis(),
    createdBy = null,
)
