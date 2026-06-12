package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Saves edits to an existing achievement definition. */
class UpdateAchievementUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(achievement: Achievement): Resource<Unit> =
        adminRepository.updateAchievement(achievement)
}
