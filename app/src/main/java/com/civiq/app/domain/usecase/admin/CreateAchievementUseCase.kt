package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Defines a new unlockable achievement/badge. */
class CreateAchievementUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(achievement: Achievement): Resource<Achievement> =
        adminRepository.createAchievement(achievement)
}
