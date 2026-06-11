package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.mapper.toDto
import com.civiq.app.data.remote.dto.firestore.DailyChallengeDto
import com.civiq.app.data.remote.dto.firestore.DailyChallengeProgressDto
import com.civiq.app.domain.model.AiQuestionRequest
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.DailyChallengeProgress
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.repository.AiQuizRepository
import com.civiq.app.domain.repository.DailyChallengeRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.GamificationConfig
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.civiq.app.utils.toDateId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Today's featured civic mission, stored at `daily_challenges/{yyyy-MM-dd}`.
 * If today's challenge doesn't exist yet, one is generated via
 * [AiQuizRepository], its questions are persisted to the `questions`
 * collection, and the resulting [DailyChallenge] is cached for the day.
 */
@Singleton
class DailyChallengeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val aiQuizRepository: AiQuizRepository,
) : DailyChallengeRepository {

    override suspend fun getTodayChallenge(): Resource<DailyChallenge> {
        val today = System.currentTimeMillis().toDateId()
        val docRef = firestore.collection(FirestoreCollections.DAILY_CHALLENGES).document(today)
        return try {
            val existing = docRef.get().await().toObject(DailyChallengeDto::class.java)
            if (existing != null) {
                Resource.Success(existing.toDomain())
            } else {
                generateAndPersistChallenge(today)
            }
        } catch (e: Exception) {
            Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load daily challenge"))
        }
    }

    private suspend fun generateAndPersistChallenge(date: String): Resource<DailyChallenge> {
        val category = QuizCategory.entries.random()
        val difficulty = QuestionDifficulty.INTERMEDIATE
        val request = AiQuestionRequest(
            category = category,
            difficulty = difficulty,
            tone = QuestionTone.EDUCATIONAL,
            count = GamificationConfig.DAILY_CHALLENGE_QUESTION_COUNT,
        )
        return when (val result = aiQuizRepository.generateQuestions(request)) {
            is Resource.Success -> {
                val questionIds = persistQuestions(result.data)
                val challenge = DailyChallenge(
                    id = date,
                    date = date,
                    title = "Daily Civic Challenge",
                    description = "Test your knowledge of ${category.displayName} today!",
                    category = category,
                    difficulty = difficulty,
                    questionIds = questionIds,
                    createdAt = System.currentTimeMillis(),
                )
                firestore.collection(FirestoreCollections.DAILY_CHALLENGES).document(date)
                    .set(challenge.toDto())
                    .await()
                Resource.Success(challenge)
            }
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Loading -> Resource.Error(UiText.DynamicString("Failed to generate daily challenge"))
        }
    }

    private suspend fun persistQuestions(questions: List<com.civiq.app.domain.model.Question>): List<String> {
        val collection = firestore.collection(FirestoreCollections.QUESTIONS)
        return questions.map { question ->
            val docRef = collection.document()
            docRef.set(question.copy(id = docRef.id).toDto()).await()
            docRef.id
        }
    }

    override fun observeChallengeProgress(userId: String, date: String): Flow<Resource<DailyChallengeProgress?>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = firestore.collection(FirestoreCollections.USERS)
            .document(userId)
            .collection(FirestoreCollections.DAILY_CHALLENGE_PROGRESS)
            .document(date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load challenge progress")))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toObject(DailyChallengeProgressDto::class.java)?.toDomain()))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun markChallengeCompleted(userId: String, date: String, attemptId: String): Resource<Unit> = try {
        val progress = DailyChallengeProgress(
            challengeId = date,
            userId = userId,
            isCompleted = true,
            attemptId = attemptId,
            completedAt = System.currentTimeMillis(),
        )
        firestore.collection(FirestoreCollections.USERS)
            .document(userId)
            .collection(FirestoreCollections.DAILY_CHALLENGE_PROGRESS)
            .document(date)
            .set(progress.toDto())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to mark challenge complete"))
    }
}
