package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.repository.AdminRepository
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams curated questions for the Admin Dashboard's question bank screen, optionally filtered by category. */
class ObserveAllQuestionsUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    operator fun invoke(category: QuizCategory? = null, limit: Int = 100): Flow<Resource<List<Question>>> =
        adminRepository.observeAllQuestions(category, limit)
}
