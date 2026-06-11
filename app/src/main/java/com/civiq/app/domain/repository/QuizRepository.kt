package com.civiq.app.domain.repository

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Quiz content and attempt history. Questions are sourced from the curated
 * `questions` Firestore collection; when insufficient curated questions
 * exist for a category/difficulty, callers fall back to
 * [com.civiq.app.domain.repository.AiQuizRepository].
 */
interface QuizRepository {

    suspend fun getQuestions(
        category: QuizCategory,
        difficulty: QuestionDifficulty,
        count: Int,
    ): Resource<List<Question>>

    suspend fun getQuestionsByIds(ids: List<String>): Resource<List<Question>>

    suspend fun submitQuizAttempt(attempt: QuizAttempt): Resource<QuizAttempt>

    fun observeQuizHistory(userId: String, limit: Int = 20): Flow<Resource<List<QuizAttempt>>>

    suspend fun getQuizAttempt(attemptId: String): Resource<QuizAttempt>

    /** Returns how many free quiz sessions the user has left today (Premium/Admin -> [Int.MAX_VALUE]). */
    suspend fun getRemainingFreeAttemptsToday(userId: String): Resource<Int>
}
