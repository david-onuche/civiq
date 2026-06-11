package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.GamificationConfig
import com.civiq.app.utils.Resource
import javax.inject.Inject

/** Loads a curated set of questions for a standard quiz session. */
class GetQuizQuestionsUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(
        category: QuizCategory,
        difficulty: QuestionDifficulty,
        count: Int = GamificationConfig.DEFAULT_QUIZ_LENGTH,
    ): Resource<List<Question>> = quizRepository.getQuestions(category, difficulty, count)
}
