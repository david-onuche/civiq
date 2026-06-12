package com.civiq.app.domain.usecase.gamification

import com.civiq.app.domain.model.Achievement
import com.civiq.app.domain.model.AchievementCriteriaType
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserAchievement
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EvaluateAchievementsUseCaseTest {

    private val gamificationRepository = mockk<GamificationRepository>()
    private val quizRepository = mockk<QuizRepository>()
    private lateinit var useCase: EvaluateAchievementsUseCase

    private val user = User(id = "user-1", xp = 100, streakCount = 1)

    @Before
    fun setUp() {
        useCase = EvaluateAchievementsUseCase(gamificationRepository, quizRepository)

        coEvery { gamificationRepository.unlockAchievement(any(), any()) } returns
            Resource.Success(UserAchievement())
        coEvery { gamificationRepository.awardXpAndCoins(any(), any(), any()) } returns Resource.Success(user)
    }

    private fun stubAchievements(achievements: List<Achievement>) {
        every { gamificationRepository.observeAchievements() } returns flowOf(Resource.Success(achievements))
    }

    private fun stubUnlocked(unlocked: List<UserAchievement>) {
        every { gamificationRepository.observeUserAchievements(any()) } returns flowOf(Resource.Success(unlocked))
    }

    private fun stubHistory(attempts: List<QuizAttempt>) {
        every { quizRepository.observeQuizHistory(any(), any()) } returns flowOf(Resource.Success(attempts))
    }

    @Test
    fun `unlocks an achievement and awards its rewards once criteria is met`() = runTest {
        val achievement = Achievement(
            id = "first-quiz",
            criteriaType = AchievementCriteriaType.QUIZZES_COMPLETED,
            criteriaValue = 1,
            xpReward = 50,
            coinReward = 10,
        )
        stubAchievements(listOf(achievement))
        stubUnlocked(emptyList())
        stubHistory(listOf(QuizAttempt(userId = "user-1", score = 5, totalQuestions = 5)))

        useCase(user)

        coVerify { gamificationRepository.unlockAchievement("user-1", "first-quiz") }
        coVerify { gamificationRepository.awardXpAndCoins("user-1", 50, 10) }
    }

    @Test
    fun `does not unlock an achievement whose criteria is not yet met`() = runTest {
        val achievement = Achievement(
            id = "streak-100",
            criteriaType = AchievementCriteriaType.STREAK_DAYS,
            criteriaValue = 100,
            xpReward = 500,
            coinReward = 100,
        )
        stubAchievements(listOf(achievement))
        stubUnlocked(emptyList())
        stubHistory(emptyList())

        useCase(user)

        coVerify(exactly = 0) { gamificationRepository.unlockAchievement(any(), any()) }
        coVerify(exactly = 0) { gamificationRepository.awardXpAndCoins(any(), any(), any()) }
    }

    @Test
    fun `skips achievements already unlocked by the user`() = runTest {
        val achievement = Achievement(
            id = "already-unlocked",
            criteriaType = AchievementCriteriaType.QUIZZES_COMPLETED,
            criteriaValue = 0,
            xpReward = 50,
            coinReward = 10,
        )
        stubAchievements(listOf(achievement))
        stubUnlocked(listOf(UserAchievement(achievementId = "already-unlocked")))
        stubHistory(emptyList())

        useCase(user)

        coVerify(exactly = 0) { gamificationRepository.unlockAchievement(any(), any()) }
    }

    @Test
    fun `unlocks a zero-reward achievement without awarding xp or coins`() = runTest {
        val achievement = Achievement(
            id = "no-reward",
            criteriaType = AchievementCriteriaType.QUIZZES_COMPLETED,
            criteriaValue = 0,
            xpReward = 0,
            coinReward = 0,
        )
        stubAchievements(listOf(achievement))
        stubUnlocked(emptyList())
        stubHistory(emptyList())

        useCase(user)

        coVerify { gamificationRepository.unlockAchievement("user-1", "no-reward") }
        coVerify(exactly = 0) { gamificationRepository.awardXpAndCoins(any(), any(), any()) }
    }

    @Test
    fun `returns early without querying further when there are no achievement definitions`() = runTest {
        stubAchievements(emptyList())

        useCase(user)

        coVerify(exactly = 0) { gamificationRepository.observeUserAchievements(any()) }
        coVerify(exactly = 0) { gamificationRepository.unlockAchievement(any(), any()) }
    }

    @Test
    fun `returns early when the achievements query fails`() = runTest {
        every { gamificationRepository.observeAchievements() } returns
            flowOf(Resource.Error(UiText.DynamicString("offline")))

        useCase(user)

        coVerify(exactly = 0) { gamificationRepository.observeUserAchievements(any()) }
        coVerify(exactly = 0) { gamificationRepository.unlockAchievement(any(), any()) }
    }
}
