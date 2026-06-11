package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.civiq.app.domain.usecase.home.GetTodayChallengeUseCase
import javax.inject.Inject

/** Bundles all use cases consumed by the Quiz feature's ViewModels. */
data class QuizUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val getQuizQuestions: GetQuizQuestionsUseCase,
    val getQuestionsByIds: GetQuestionsByIdsUseCase,
    val getTodayChallenge: GetTodayChallengeUseCase,
    val completeQuiz: CompleteQuizUseCase,
    val getQuizAttempt: GetQuizAttemptUseCase,
    val observeQuizHistory: ObserveQuizHistoryUseCase,
    val getRemainingFreeAttempts: GetRemainingFreeAttemptsUseCase,
)
