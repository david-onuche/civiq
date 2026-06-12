package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Removes a question from the question bank. */
class DeleteQuestionUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(questionId: String): Resource<Unit> = adminRepository.deleteQuestion(questionId)
}
