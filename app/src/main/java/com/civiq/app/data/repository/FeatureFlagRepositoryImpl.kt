package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.remote.dto.firestore.FeatureFlagDto
import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.domain.repository.FeatureFlagRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote feature toggles backed by Firestore (`feature_flags/{key}`), with
 * [FirebaseRemoteConfig] as a low-latency fallback when a flag document is
 * missing or the read fails.
 */
@Singleton
class FeatureFlagRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val remoteConfig: FirebaseRemoteConfig,
) : FeatureFlagRepository {

    override fun observeFeatureFlags(): Flow<Resource<List<FeatureFlag>>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = firestore.collection(FirestoreCollections.FEATURE_FLAGS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load feature flags")))
                    return@addSnapshotListener
                }
                val flags = snapshot?.documents
                    ?.mapNotNull { it.toObject(FeatureFlagDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(flags))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun isFeatureEnabled(key: String): Boolean = try {
        val snapshot = firestore.collection(FirestoreCollections.FEATURE_FLAGS).document(key).get().await()
        snapshot.toObject(FeatureFlagDto::class.java)?.isEnabled ?: remoteConfig.getBoolean(key)
    } catch (e: Exception) {
        remoteConfig.getBoolean(key)
    }
}
