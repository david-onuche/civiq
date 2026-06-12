package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Adds a new curated question to the question bank. */
class CreateQuestionUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(question: Question): Resource<Question> = adminRepository.createQuestion(question)
}
