package com.civiq.app.domain.usecase.home

import com.civiq.app.domain.model.DailyChallenge
import com.civiq.app.domain.repository.DailyChallengeRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Fetches today's featured civic mission, generating one via AI if it doesn't exist yet. */
class GetTodayChallengeUseCase @Inject constructor(
    private val dailyChallengeRepository: DailyChallengeRepository,
) {
    suspend operator fun invoke(): Resource<DailyChallenge> = dailyChallengeRepository.getTodayChallenge()
}
