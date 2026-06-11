package com.civiq.app.domain.usecase.home

import com.civiq.app.domain.model.DailyChallengeProgress
import com.civiq.app.domain.repository.DailyChallengeRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the current user's completion record for a given day's daily challenge. */
class ObserveDailyChallengeProgressUseCase @Inject constructor(
    private val dailyChallengeRepository: DailyChallengeRepository,
) {
    operator fun invoke(userId: String, date: String): Flow<Resource<DailyChallengeProgress?>> =
        dailyChallengeRepository.observeChallengeProgress(userId, date)
}
