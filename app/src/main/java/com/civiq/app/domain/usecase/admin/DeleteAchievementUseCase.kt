package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Removes an achievement definition. Already-unlocked [com.civiq.app.domain.model.UserAchievement] records are unaffected. */
class DeleteAchievementUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(achievementId: String): Resource<Unit> = adminRepository.deleteAchievement(achievementId)
}
