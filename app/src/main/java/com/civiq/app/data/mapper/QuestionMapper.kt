package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.QuestionDto
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuestionSource
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuestionType
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.safeEnumValueOf

fun QuestionDto.toDomain(): Question = Question(
    id = id,
    type = safeEnumValueOf(type, QuestionType.MULTIPLE_CHOICE),
    category = safeEnumValueOf(category, QuizCategory.DEMOCRACY),
    difficulty = safeEnumValueOf(difficulty, QuestionDifficulty.BEGINNER),
    questionText = questionText,
    options = options,
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    tone = safeEnumValueOf(tone, QuestionTone.EDUCATIONAL),
    countryCode = countryCode,
    tags = tags,
    source = safeEnumValueOf(source, QuestionSource.CURATED),
    createdAt = createdAt,
    createdBy = createdBy,
)

fun Question.toDto(): QuestionDto = QuestionDto(
    id = id,
    type = type.name,
    category = category.name,
    difficulty = difficulty.name,
    questionText = questionText,
    options = options,
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    tone = tone.name,
    countryCode = countryCode,
    tags = tags,
    source = source.name,
    createdAt = createdAt,
    createdBy = createdBy,
)
