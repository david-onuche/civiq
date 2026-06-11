package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.remote.dto.firestore.LeaderboardEntryDto
import com.civiq.app.domain.model.LeaderboardEntry
import com.civiq.app.domain.model.LeaderboardPeriod
import com.civiq.app.domain.model.LeaderboardScope
import com.civiq.app.domain.repository.LeaderboardRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ranked XP leaderboards, stored at
 * `leaderboards/{period}_{scope}[_{countryCode}]/entries/{userId}` and
 * recomputed periodically by a Cloud Function.
 */
@Singleton
class LeaderboardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : LeaderboardRepository {

    private fun leaderboardId(period: LeaderboardPeriod, scope: LeaderboardScope, countryCode: String?): String =
        if (scope == LeaderboardScope.COUNTRY && !countryCode.isNullOrBlank()) {
            "${period.name}_${scope.name}_$countryCode"
        } else {
            "${period.name}_${scope.name}"
        }

    override fun observeLeaderboard(
        period: LeaderboardPeriod,
        scope: LeaderboardScope,
        countryCode: String?,
        limit: Int,
    ): Flow<Resource<List<LeaderboardEntry>>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = firestore.collection(FirestoreCollections.LEADERBOARDS)
            .document(leaderboardId(period, scope, countryCode))
            .collection(FirestoreCollections.LEADERBOARD_ENTRIES)
            .orderBy("rank")
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load leaderboard")))
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents
                    ?.mapNotNull { it.toObject(LeaderboardEntryDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(entries))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getUserRank(
        userId: String,
        period: LeaderboardPeriod,
        scope: LeaderboardScope,
        countryCode: String?,
    ): Resource<LeaderboardEntry?> = try {
        val snapshot = firestore.collection(FirestoreCollections.LEADERBOARDS)
            .document(leaderboardId(period, scope, countryCode))
            .collection(FirestoreCollections.LEADERBOARD_ENTRIES)
            .document(userId)
            .get()
            .await()
        Resource.Success(snapshot.toObject(LeaderboardEntryDto::class.java)?.toDomain())
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load rank"))
    }
}
