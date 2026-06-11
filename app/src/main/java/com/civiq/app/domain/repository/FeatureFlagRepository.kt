package com.civiq.app.domain.repository

import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Remote feature toggles backed by Firestore (`feature_flags/{key}`) with
 * Firebase Remote Config as a fast local cache. Used to gate Premium-only
 * functionality (AI Coach, exclusive challenges, ad-free, unlimited quizzes)
 * without requiring an app release.
 */
interface FeatureFlagRepository {

    fun observeFeatureFlags(): Flow<Resource<List<FeatureFlag>>>

    suspend fun isFeatureEnabled(key: String): Boolean
}
