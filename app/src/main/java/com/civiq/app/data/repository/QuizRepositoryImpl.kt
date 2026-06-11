package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.mapper.toDto
import com.civiq.app.data.remote.dto.firestore.QuestionDto
import com.civiq.app.data.remote.dto.firestore.QuizAttemptDto
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.FirestoreFields
import com.civiq.app.utils.GamificationConfig
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.civiq.app.utils.startOfDay
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Curated quiz content (`questions`) and per-user attempt history (`quiz_attempts`). */
@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : QuizRepository {

    override suspend fun getQuestions(
        category: QuizCategory,
        difficulty: QuestionDifficulty,
        count: Int,
    ): Resource<List<Question>> = try {
        val snapshot = firestore.collection(FirestoreCollections.QUESTIONS)
            .whereEqualTo(FirestoreFields.FIELD_CATEGORY, category.name)
            .whereEqualTo(FirestoreFields.FIELD_DIFFICULTY, difficulty.name)
            .limit(count.toLong())
            .get()
            .await()
        val questions = snapshot.documents.mapNotNull { it.toObject(QuestionDto::class.java)?.toDomain() }
        Resource.Success(questions)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load questions"))
    }

    override suspend fun getQuestionsByIds(ids: List<String>): Resource<List<Question>> {
        if (ids.isEmpty()) return Resource.Success(emptyList())
        return try {
            val questions = ids.chunked(30).flatMap { chunk ->
                firestore.collection(FirestoreCollections.QUESTIONS)
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(QuestionDto::class.java)?.toDomain() }
            }
            Resource.Success(questions)
        } catch (e: Exception) {
            Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load questions"))
        }
    }

    override suspend fun submitQuizAttempt(attempt: QuizAttempt): Resource<QuizAttempt> = try {
        val collection = firestore.collection(FirestoreCollections.QUIZ_ATTEMPTS)
        val docRef = if (attempt.id.isBlank()) collection.document() else collection.document(attempt.id)
        val toSave = attempt.copy(id = docRef.id)
        docRef.set(toSave.toDto()).await()
        Resource.Success(toSave)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to save quiz attempt"))
    }

    override fun observeQuizHistory(userId: String, limit: Int): Flow<Resource<List<QuizAttempt>>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = firestore.collection(FirestoreCollections.QUIZ_ATTEMPTS)
            .whereEqualTo("userId", userId)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load quiz history")))
                    return@addSnapshotListener
                }
                val attempts = snapshot?.documents
                    ?.mapNotNull { it.toObject(QuizAttemptDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(attempts))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getQuizAttempt(attemptId: String): Resource<QuizAttempt> = try {
        val snapshot = firestore.collection(FirestoreCollections.QUIZ_ATTEMPTS).document(attemptId).get().await()
        val dto = snapshot.toObject(QuizAttemptDto::class.java)
        if (dto != null) {
            Resource.Success(dto.toDomain())
        } else {
            Resource.Error(UiText.DynamicString("Quiz attempt not found"))
        }
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load quiz attempt"))
    }

    override suspend fun getRemainingFreeAttemptsToday(userId: String): Resource<Int> = try {
        val startOfDay = System.currentTimeMillis().startOfDay()
        val snapshot = firestore.collection(FirestoreCollections.QUIZ_ATTEMPTS)
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("startedAt", startOfDay)
            .get()
            .await()
        val remaining = (GamificationConfig.FREE_TIER_DAILY_QUIZ_LIMIT - snapshot.size()).coerceAtLeast(0)
        Resource.Success(remaining)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to check quiz limit"))
    }
}
