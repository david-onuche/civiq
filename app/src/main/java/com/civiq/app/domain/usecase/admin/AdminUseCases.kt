package com.civiq.app.domain.usecase.admin

import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.civiq.app.domain.usecase.premium.ObserveFeatureFlagsUseCase
import javax.inject.Inject

/**
 * Bundles every use case backing the Admin Dashboard's screens - RBAC gating,
 * user role management, question bank CRUD, feature flags, daily challenges,
 * and achievement definitions - into a single injectable dependency.
 */
data class AdminUseCases @Inject constructor(
    val observeCurrentUser: ObserveCurrentUserUseCase,
    val observeAllUsers: ObserveAllUsersUseCase,
    val getUserById: GetUserByIdUseCase,
    val updateUserRole: UpdateUserRoleUseCase,
    val observeAllQuestions: ObserveAllQuestionsUseCase,
    val getQuestion: GetQuestionUseCase,
    val createQuestion: CreateQuestionUseCase,
    val updateQuestion: UpdateQuestionUseCase,
    val deleteQuestion: DeleteQuestionUseCase,
    val observeFeatureFlags: ObserveFeatureFlagsUseCase,
    val updateFeatureFlag: UpdateFeatureFlagUseCase,
    val observeAchievementDefinitions: ObserveAchievementDefinitionsUseCase,
    val createAchievement: CreateAchievementUseCase,
    val updateAchievement: UpdateAchievementUseCase,
    val deleteAchievement: DeleteAchievementUseCase,
    val createOrUpdateDailyChallenge: CreateOrUpdateDailyChallengeUseCase,
)
