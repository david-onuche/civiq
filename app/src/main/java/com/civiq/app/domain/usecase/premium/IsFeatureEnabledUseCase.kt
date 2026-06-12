package com.civiq.app.domain.usecase.premium

import com.civiq.app.domain.repository.FeatureFlagRepository
import javax.inject.Inject

/** Checks whether a remotely-configured feature flag (see [com.civiq.app.domain.model.FeatureFlagKeys]) is enabled. */
class IsFeatureEnabledUseCase @Inject constructor(
    private val featureFlagRepository: FeatureFlagRepository,
) {
    suspend operator fun invoke(key: String): Boolean = featureFlagRepository.isFeatureEnabled(key)
}
