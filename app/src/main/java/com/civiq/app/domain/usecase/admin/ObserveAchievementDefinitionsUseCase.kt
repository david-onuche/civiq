package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams every [Achievement] definition for the Admin Dashboard's achievement management screen. */
class ObserveAchievementDefinitionsUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    operator fun invoke(): Flow<Resource<List<Achievement>>> = adminRepository.observeAchievements()
}
