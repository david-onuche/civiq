package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.mapper.toDto
import com.civiq.app.data.remote.dto.firestore.AchievementDto
import com.civiq.app.data.remote.dto.firestore.UserAchievementDto
import com.civiq.app.data.remote.dto.firestore.UserDto
import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserAchievement
import com.civiq.app.domain.model.UserLevels
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.civiq.app.utils.isDayBefore
import com.civiq.app.utils.isSameDayAs
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** XP/coin awards, level recalculation, streaks, and achievement unlocks. */
@Singleton
class GamificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : GamificationRepository {

    private fun userDoc(userId: String) = firestore.collection(FirestoreCollections.USERS).document(userId)

    override suspend fun awardXpAndCoins(userId: String, xp: Long, coins: Long): Resource<User> = try {
        val docRef = userDoc(userId)
        val updated = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val dto = snapshot.toObject(UserDto::class.java) ?: throw IllegalStateException("User not found")
            val newXp = dto.xp + xp
            val newCoins = dto.coins + coins
            val newLevel = UserLevels.fromTotalXp(newXp).level
            transaction.update(
                docRef,
                mapOf(
                    "xp" to newXp,
                    "coins" to newCoins,
                    "level" to newLevel,
                ),
            )
            dto.copy(xp = newXp, coins = newCoins, level = newLevel)
        }.await()
        Resource.Success(updated.toDomain())
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to award XP"))
    }

    override suspend fun updateStreak(userId: String): Resource<User> = try {
        val docRef = userDoc(userId)
        val updated = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val dto = snapshot.toObject(UserDto::class.java) ?: throw IllegalStateException("User not found")
            val now = System.currentTimeMillis()
            val newStreak = when {
                dto.lastActiveDate == 0L -> 1
                now.isSameDayAs(dto.lastActiveDate) -> dto.streakCount
                dto.lastActiveDate.isDayBefore(now) -> dto.streakCount + 1
                else -> 1
            }
            val newLongest = maxOf(dto.longestStreak, newStreak)
            transaction.update(
                docRef,
                mapOf(
                    "streakCount" to newStreak,
                    "longestStreak" to newLongest,
                    "lastActiveDate" to now,
                ),
            )
            dto.copy(streakCount = newStreak, longestStreak = newLongest, lastActiveDate = now)
        }.await()
        Resource.Success(updated.toDomain())
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update streak"))
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

    override fun observeUserAchievements(userId: String): Flow<Resource<List<UserAchievement>>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = userDoc(userId)
            .collection(FirestoreCollections.USER_ACHIEVEMENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load achievements")))
                    return@addSnapshotListener
                }
                val achievements = snapshot?.documents
                    ?.mapNotNull { it.toObject(UserAchievementDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(achievements))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun unlockAchievement(userId: String, achievementId: String): Resource<UserAchievement> = try {
        val userAchievement = UserAchievement(achievementId = achievementId, unlockedAt = System.currentTimeMillis())
        userDoc(userId)
            .collection(FirestoreCollections.USER_ACHIEVEMENTS)
            .document(achievementId)
            .set(userAchievement.toDto())
            .await()
        Resource.Success(userAchievement)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to unlock achievement"))
    }
}
