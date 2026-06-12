package com.civiq.app.domain.usecase.aicoach

import com.civiq.app.domain.model.CoachMessage
import com.civiq.app.domain.repository.AiCoachRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Sends the conversation [history] to the configured AI provider and returns the coach's reply. */
class SendCoachMessageUseCase @Inject constructor(
    private val aiCoachRepository: AiCoachRepository,
) {
    suspend operator fun invoke(history: List<CoachMessage>): Resource<String> = aiCoachRepository.sendMessage(history)
}
