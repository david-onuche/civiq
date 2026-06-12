package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.mapper.toDto
import com.civiq.app.data.remote.dto.firestore.AchievementDto
import com.civiq.app.data.remote.dto.firestore.QuestionDto
import com.civiq.app.data.remote.dto.firestore.UserDto
import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.FirestoreFields
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Administrative read/write access to users, questions, daily challenges,
 * and achievements. Access is enforced server-side by Firestore security
 * rules requiring [UserRole.ADMIN] (see docs/DATABASE.md).
 */
@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : AdminRepository {

    override fun observeAllUsers(limit: Int): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = firestore.collection(FirestoreCollections.USERS)
            .orderBy(FirestoreFields.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load users")))
                    return@addSnapshotListener
                }
                val users = snapshot?.documents
                    ?.mapNotNull { it.toObject(UserDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(users))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun updateUserRole(userId: String, role: UserRole): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.USERS).document(userId)
            .update(FirestoreFields.FIELD_ROLE, role.name)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update user role"))
    }

    override fun observeAllQuestions(category: QuizCategory?, limit: Int): Flow<Resource<List<Question>>> = callbackFlow {
        trySend(Resource.Loading())
        var query: Query = firestore.collection(FirestoreCollections.QUESTIONS)
        if (category != null) {
            query = query.whereEqualTo(FirestoreFields.FIELD_CATEGORY, category.name)
        }
        val registration = query.limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load questions")))
                    return@addSnapshotListener
                }
                val questions = snapshot?.documents
                    ?.mapNotNull { it.toObject(QuestionDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(questions))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getQuestion(questionId: String): Resource<Question> = try {
        val snapshot = firestore.collection(FirestoreCollections.QUESTIONS).document(questionId).get().await()
        val dto = snapshot.toObject(QuestionDto::class.java)
        if (dto != null) {
            Resource.Success(dto.toDomain())
        } else {
            Resource.Error(UiText.DynamicString("Question not found"))
        }
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load question"))
    }

    override suspend fun createQuestion(question: Question): Resource<Question> = try {
        val collection = firestore.collection(FirestoreCollections.QUESTIONS)
        val docRef = if (question.id.isBlank()) collection.document() else collection.document(question.id)
        val toSave = question.copy(id = docRef.id, createdAt = System.currentTimeMillis())
        docRef.set(toSave.toDto()).await()
        Resource.Success(toSave)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to create question"))
    }

    override suspend fun updateQuestion(question: Question): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.QUESTIONS).document(question.id)
            .set(question.toDto())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update question"))
    }

    override suspend fun deleteQuestion(questionId: String): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.QUESTIONS).document(questionId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to delete question"))
    }

    override suspend fun createOrUpdateDailyChallenge(challenge: DailyChallenge): Resource<DailyChallenge> = try {
        val id = challenge.id.ifBlank { challenge.date }
        val toSave = challenge.copy(id = id)
        firestore.collection(FirestoreCollections.DAILY_CHALLENGES).document(id)
            .set(toSave.toDto())
            .await()
        Resource.Success(toSave)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to save daily challenge"))
    }

    override fun observeAchievements(): Flow<Resource<List<Achievement>>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = firestore.collection(FirestoreCollections.ACHIEVEMENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load achievements")))
                    return@addSnapshotListener
                }
                val achievements = snapshot?.documents
                    ?.mapNotNull { it.toObject(AchievementDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(achievements))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createAchievement(achievement: Achievement): Resource<Achievement> = try {
        val collection = firestore.collection(FirestoreCollections.ACHIEVEMENTS)
        val docRef = if (achievement.id.isBlank()) collection.document() else collection.document(achievement.id)
        val toSave = achievement.copy(id = docRef.id)
        docRef.set(toSave.toDto()).await()
        Resource.Success(toSave)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to create achievement"))
    }

    override suspend fun updateAchievement(achievement: Achievement): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.ACHIEVEMENTS).document(achievement.id)
            .set(achievement.toDto())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update achievement"))
    }

    override suspend fun deleteAchievement(achievementId: String): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.ACHIEVEMENTS).document(achievementId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to delete achievement"))
    }

    override suspend fun updateFeatureFlag(flag: FeatureFlag): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.FEATURE_FLAGS).document(flag.key)
            .set(flag.toDto())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update feature flag"))
    }
}
