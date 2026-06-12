package com.civiq.app.domain.usecase.quiz

import com.civiq.app.domain.model.QuestionAnswer
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.DailyChallengeRepository
import com.civiq.app.domain.repository.GamificationRepository
import com.civiq.app.domain.repository.QuizRepository
import com.civiq.app.domain.usecase.gamification.EvaluateAchievementsUseCase
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CompleteQuizUseCaseTest {

    private val quizRepository = mockk<QuizRepository>()
    private val gamificationRepository = mockk<GamificationRepository>()
    private val dailyChallengeRepository = mockk<DailyChallengeRepository>()
    private val evaluateAchievements = mockk<EvaluateAchievementsUseCase>()
    private lateinit var useCase: CompleteQuizUseCase

    private val streakUpdatedUser = User(id = "user-1", xp = 500, streakCount = 5)

    @Before
    fun setUp() {
        useCase = CompleteQuizUseCase(
            quizRepository = quizRepository,
            gamificationRepository = gamificationRepository,
            dailyChallengeRepository = dailyChallengeRepository,
            evaluateAchievements = evaluateAchievements,
        )

        coEvery { gamificationRepository.awardXpAndCoins(any(), any(), any()) } returns
            Resource.Success(streakUpdatedUser)
        coEvery { gamificationRepository.updateStreak(any()) } returns Resource.Success(streakUpdatedUser)
        coEvery { dailyChallengeRepository.markChallengeCompleted(any(), any(), any()) } returns
            Resource.Success(Unit)
        coJustRun { evaluateAchievements(any()) }
    }

    @Test
    fun `scores the attempt, applies xp and coin rewards, and updates streak`() = runTest {
        val answers = listOf(
            QuestionAnswer(questionId = "q1", selectedIndex = 0, isCorrect = true, timeTakenMs = 1_000),
            QuestionAnswer(questionId = "q2", selectedIndex = 1, isCorrect = true, timeTakenMs = 2_000),
            QuestionAnswer(questionId = "q3", selectedIndex = 2, isCorrect = false, timeTakenMs = 3_000),
        )
        val attemptSlot = slot<QuizAttempt>()
        coEvery { quizRepository.submitQuizAttempt(capture(attemptSlot)) } answers {
            Resource.Success(attemptSlot.captured.copy(id = "saved-1"))
        }

        val result = useCase(
            userId = "user-1",
            category = QuizCategory.DEMOCRACY,
            difficulty = QuestionDifficulty.INTERMEDIATE,
            answers = answers,
            startedAt = 1_000_000L,
        )

        // 2 correct * 10 base XP * 1.5 (INTERMEDIATE) = 30, + 25 completion bonus = 55.
        // 2 correct * 2 base coins = 4 (not a perfect score, not a daily challenge).
        val saved = attemptSlot.captured
        assertThat(saved.score).isEqualTo(2)
        assertThat(saved.totalQuestions).isEqualTo(3)
        assertThat(saved.xpEarned).isEqualTo(55)
        assertThat(saved.coinsEarned).isEqualTo(4)
        assertThat(saved.category).isEqualTo(QuizCategory.DEMOCRACY)
        assertThat(saved.difficulty).isEqualTo(QuestionDifficulty.INTERMEDIATE)
        assertThat(saved.startedAt).isEqualTo(1_000_000L)
        assertThat(saved.isDailyChallenge).isFalse()

        assertThat((result as Resource.Success).data.id).isEqualTo("saved-1")
        coVerify { gamificationRepository.awardXpAndCoins("user-1", 55, 4) }
        coVerify { gamificationRepository.updateStreak("user-1") }
        coVerify { evaluateAchievements(streakUpdatedUser) }
        coVerify(exactly = 0) { dailyChallengeRepository.markChallengeCompleted(any(), any(), any()) }
    }

    @Test
    fun `applies daily challenge bonuses and marks the challenge completed`() = runTest {
        val answers = listOf(
            QuestionAnswer(questionId = "q1", selectedIndex = 0, isCorrect = true),
            QuestionAnswer(questionId = "q2", selectedIndex = 1, isCorrect = true),
        )
        val attemptSlot = slot<QuizAttempt>()
        coEvery { quizRepository.submitQuizAttempt(capture(attemptSlot)) } answers {
            Resource.Success(attemptSlot.captured.copy(id = "saved-2"))
        }

        useCase(
            userId = "user-1",
            category = QuizCategory.ELECTIONS,
            difficulty = QuestionDifficulty.BEGINNER,
            answers = answers,
            startedAt = 2_000_000L,
            isDailyChallenge = true,
            challengeId = "2026-06-12",
        )

        // 2 correct * 10 base XP = 20, + 25 completion bonus + 50 daily challenge bonus = 95.
        // 2 correct * 2 base coins = 4, + 50 perfect score + 20 daily challenge bonus = 74.
        val saved = attemptSlot.captured
        assertThat(saved.xpEarned).isEqualTo(95)
        assertThat(saved.coinsEarned).isEqualTo(74)
        assertThat(saved.isPerfectScore).isTrue()
        assertThat(saved.isDailyChallenge).isTrue()
        assertThat(saved.challengeId).isEqualTo("2026-06-12")

        coVerify { gamificationRepository.awardXpAndCoins("user-1", 95, 74) }
        coVerify { dailyChallengeRepository.markChallengeCompleted("user-1", any(), "saved-2") }
    }

    @Test
    fun `does not apply rewards when the attempt fails to save`() = runTest {
        val error = Resource.Error<QuizAttempt>(UiText.DynamicString("offline"))
        coEvery { quizRepository.submitQuizAttempt(any()) } returns error

        val result = useCase(
            userId = "user-1",
            category = QuizCategory.DEMOCRACY,
            difficulty = QuestionDifficulty.BEGINNER,
            answers = listOf(QuestionAnswer(questionId = "q1", selectedIndex = 0, isCorrect = true)),
            startedAt = 1_000_000L,
        )

        assertThat(result).isEqualTo(error)
        coVerify(exactly = 0) { gamificationRepository.awardXpAndCoins(any(), any(), any()) }
        coVerify(exactly = 0) { gamificationRepository.updateStreak(any()) }
        coVerify(exactly = 0) { evaluateAchievements(any()) }
        coVerify(exactly = 0) { dailyChallengeRepository.markChallengeCompleted(any(), any(), any()) }
    }
}
