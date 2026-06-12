package com.civiq.app.domain.usecase.premium

import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.domain.repository.FeatureFlagRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams all remotely-configured feature flags, used to render the Premium feature list. */
class ObserveFeatureFlagsUseCase @Inject constructor(
    private val featureFlagRepository: FeatureFlagRepository,
) {
    operator fun invoke(): Flow<Resource<List<FeatureFlag>>> = featureFlagRepository.observeFeatureFlags()
}
