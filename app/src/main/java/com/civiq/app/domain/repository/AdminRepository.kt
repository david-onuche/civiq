package com.civiq.app.domain.repository

import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Administrative operations gated behind [UserRole.ADMIN] by Firestore
 * security rules (see docs/DATABASE.md). Powers the Admin Dashboard's
 * user, question, challenge, and achievement management screens.
 */
interface AdminRepository {

    fun observeAllUsers(limit: Int = 50): Flow<Resource<List<User>>>

    suspend fun updateUserRole(userId: String, role: UserRole): Resource<Unit>

    fun observeAllQuestions(category: QuizCategory? = null, limit: Int = 100): Flow<Resource<List<Question>>>

    suspend fun getQuestion(questionId: String): Resource<Question>

    suspend fun createQuestion(question: Question): Resource<Question>

    suspend fun updateQuestion(question: Question): Resource<Unit>

    suspend fun deleteQuestion(questionId: String): Resource<Unit>

    suspend fun createOrUpdateDailyChallenge(challenge: DailyChallenge): Resource<DailyChallenge>

    fun observeAchievements(): Flow<Resource<List<Achievement>>>

    suspend fun createAchievement(achievement: Achievement): Resource<Achievement>

    suspend fun updateAchievement(achievement: Achievement): Resource<Unit>

    suspend fun deleteAchievement(achievementId: String): Resource<Unit>

    suspend fun updateFeatureFlag(flag: FeatureFlag): Resource<Unit>
}
