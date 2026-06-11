package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.QuestionAnswerDto
import com.civiq.app.data.remote.dto.firestore.QuizAttemptDto
import com.civiq.app.domain.model.QuestionAnswer
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.safeEnumValueOf

fun QuizAttemptDto.toDomain(): QuizAttempt = QuizAttempt(
    id = id,
    userId = userId,
    category = safeEnumValueOf(category, QuizCategory.DEMOCRACY),
    difficulty = safeEnumValueOf(difficulty, QuestionDifficulty.BEGINNER),
    questionIds = questionIds,
    answers = answers.map { it.toDomain() },
    score = score,
    totalQuestions = totalQuestions,
    xpEarned = xpEarned,
    coinsEarned = coinsEarned,
    isDailyChallenge = isDailyChallenge,
    challengeId = challengeId,
    startedAt = startedAt,
    completedAt = completedAt,
)

fun QuizAttempt.toDto(): QuizAttemptDto = QuizAttemptDto(
    id = id,
    userId = userId,
    category = category.name,
    difficulty = difficulty.name,
    questionIds = questionIds,
    answers = answers.map { it.toDto() },
    score = score,
    totalQuestions = totalQuestions,
    xpEarned = xpEarned,
    coinsEarned = coinsEarned,
    isDailyChallenge = isDailyChallenge,
    challengeId = challengeId,
    startedAt = startedAt,
    completedAt = completedAt,
)

fun QuestionAnswerDto.toDomain(): QuestionAnswer = QuestionAnswer(
    questionId = questionId,
    selectedIndex = selectedIndex,
    isCorrect = isCorrect,
    timeTakenMs = timeTakenMs,
)

fun QuestionAnswer.toDto(): QuestionAnswerDto = QuestionAnswerDto(
    questionId = questionId,
    selectedIndex = selectedIndex,
    isCorrect = isCorrect,
    timeTakenMs = timeTakenMs,
)
