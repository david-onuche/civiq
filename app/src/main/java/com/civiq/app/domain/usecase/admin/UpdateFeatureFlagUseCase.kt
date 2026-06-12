package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.FeatureFlag
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Toggles or edits a remotely-configured [FeatureFlag], e.g. enabling the AI Learning Coach for all Premium users. */
class UpdateFeatureFlagUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(flag: FeatureFlag): Resource<Unit> = adminRepository.updateFeatureFlag(flag)
}
