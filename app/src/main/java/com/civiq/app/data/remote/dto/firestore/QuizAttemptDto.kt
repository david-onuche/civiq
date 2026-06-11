package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `quiz_attempts/{attemptId}`. See docs/DATABASE.md. */
data class QuizAttemptDto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "DEMOCRACY",
    val difficulty: String = "BEGINNER",
    val questionIds: List<String> = emptyList(),
    val answers: List<QuestionAnswerDto> = emptyList(),
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Long = 0,
    val coinsEarned: Long = 0,
    val isDailyChallenge: Boolean = false,
    val challengeId: String? = null,
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
)

/** Embedded (not a separate collection) within [QuizAttemptDto.answers]. */
data class QuestionAnswerDto(
    val questionId: String = "",
    val selectedIndex: Int = -1,
    val isCorrect: Boolean = false,
    val timeTakenMs: Long = 0L,
)
