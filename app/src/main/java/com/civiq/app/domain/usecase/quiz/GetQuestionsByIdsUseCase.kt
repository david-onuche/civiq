package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Loads a specific, ordered set of questions by ID, e.g. for a daily challenge. */
class GetQuestionsByIdsUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(ids: List<String>): Resource<List<Question>> =
        quizRepository.getQuestionsByIds(ids).map { questions ->
            val byId = questions.associateBy { it.id }
            ids.mapNotNull { byId[it] }
        }
}
