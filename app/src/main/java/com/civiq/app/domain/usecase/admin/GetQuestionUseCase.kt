package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Loads a single question by ID for the Admin Question Editor screen. */
class GetQuestionUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(questionId: String): Resource<Question> = adminRepository.getQuestion(questionId)
}
