package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Saves edits to an existing curated question. */
class UpdateQuestionUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(question: Question): Resource<Unit> = adminRepository.updateQuestion(question)
}
