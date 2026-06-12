package com.civiq.app.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.civiq.app.MainDispatcherRule
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizAttempt
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.domain.model.User
import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.civiq.app.domain.usecase.home.GetTodayChallengeUseCase
import com.civiq.app.domain.usecase.quiz.CompleteQuizUseCase
import com.civiq.app.domain.usecase.quiz.GetQuestionsByIdsUseCase
import com.civiq.app.domain.usecase.quiz.GetQuizAttemptUseCase
import com.civiq.app.domain.usecase.quiz.GetQuizQuestionsUseCase
import com.civiq.app.domain.usecase.quiz.GetRemainingFreeAttemptsUseCase
import com.civiq.app.domain.usecase.quiz.ObserveQuizHistoryUseCase
import com.civiq.app.domain.usecase.quiz.QuizUseCases
import com.civiq.app.navigation.Screen
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizPlayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeCurrentUser = mockk<ObserveCurrentUserUseCase>()
    private val getQuizQuestions = mockk<GetQuizQuestionsUseCase>()
    private val getQuestionsByIds = mockk<GetQuestionsByIdsUseCase>()
    private val getTodayChallenge = mockk<GetTodayChallengeUseCase>()
    private val completeQuiz = mockk<CompleteQuizUseCase>()
    private val getQuizAttempt = mockk<GetQuizAttemptUseCase>()
    private val observeQuizHistory = mockk<ObserveQuizHistoryUseCase>()
    private val getRemainingFreeAttempts = mockk<GetRemainingFreeAttemptsUseCase>()

    private val question1 = Question(
        id = "q1",
        questionText = "Who has the power to veto a bill passed by the legislature?",
        options = listOf("The Speaker", "The President", "The Chief Justice", "The Senate"),
        correctAnswerIndex = 1,
    )
    private val question2 = Question(
        id = "q2",
        questionText = "How often are general elections held?",
        options = listOf("Every 2 years", "Every 4 years", "Every 6 years", "Every 8 years"),
        correctAnswerIndex = 1,
    )

    @Before
    fun setUp() {
        every { observeCurrentUser() } returns flowOf(User(id = "user-1"))
        coEvery { getQuizQuestions(any(), any()) } returns Resource.Success(listOf(question1, question2))
    }

    private fun createViewModel(challengeId: String? = null): QuizPlayViewModel {
        val savedStateHandle = SavedStateHandle(
            buildMap {
                put(Screen.ARG_CATEGORY, QuizCategory.ELECTIONS.name)
                put(Screen.ARG_DIFFICULTY, QuestionDifficulty.ADVANCED.name)
                if (challengeId != null) put(Screen.ARG_CHALLENGE_ID, challengeId)
            },
        )
        val quizUseCases = QuizUseCases(
            observeCurrentUser = observeCurrentUser,
            getQuizQuestions = getQuizQuestions,
            getQuestionsByIds = getQuestionsByIds,
            getTodayChallenge = getTodayChallenge,
            completeQuiz = completeQuiz,
            getQuizAttempt = getQuizAttempt,
            observeQuizHistory = observeQuizHistory,
            getRemainingFreeAttempts = getRemainingFreeAttempts,
        )
        return QuizPlayViewModel(savedStateHandle, quizUseCases)
    }

    @Test
    fun `loads questions for the requested category and difficulty`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.category).isEqualTo(QuizCategory.ELECTIONS)
        assertThat(state.difficulty).isEqualTo(QuestionDifficulty.ADVANCED)
        assertThat(state.questions).containsExactly(question1, question2)
        coVerify { getQuizQuestions(QuizCategory.ELECTIONS, QuestionDifficulty.ADVANCED) }
    }

    @Test
    fun `selecting an answer reveals it and records the result`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(1))

        val state = viewModel.uiState.value
        assertThat(state.selectedAnswerIndex).isEqualTo(1)
        assertThat(state.isAnswerRevealed).isTrue()
        assertThat(state.answers).hasSize(1)
        assertThat(state.answers.single().questionId).isEqualTo("q1")
        assertThat(state.answers.single().isCorrect).isTrue()
    }

    @Test
    fun `selecting an answer after it is revealed is ignored`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(1))

        viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(0))

        val state = viewModel.uiState.value
        assertThat(state.selectedAnswerIndex).isEqualTo(1)
        assertThat(state.answers).hasSize(1)
    }

    @Test
    fun `next question advances after the answer is revealed`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(1))

        viewModel.onEvent(QuizPlayUiEvent.NextQuestion)

        val state = viewModel.uiState.value
        assertThat(state.currentIndex).isEqualTo(1)
        assertThat(state.selectedAnswerIndex).isNull()
        assertThat(state.isAnswerRevealed).isFalse()
    }

    @Test
    fun `next question is ignored before an answer is revealed`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(QuizPlayUiEvent.NextQuestion)

        assertThat(viewModel.uiState.value.currentIndex).isEqualTo(0)
    }

    @Test
    fun `finishing the last question submits the attempt and navigates to the result`() = runTest {
        val viewModel = createViewModel()
        val attempt = QuizAttempt(id = "attempt-1", userId = "user-1")
        coEvery {
            completeQuiz(
                userId = "user-1",
                category = QuizCategory.ELECTIONS,
                difficulty = QuestionDifficulty.ADVANCED,
                answers = any(),
                startedAt = any(),
                isDailyChallenge = false,
                challengeId = null,
            )
        } returns Resource.Success(attempt)

        // Answer the first question and move on without finishing the quiz yet.
        viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(1))
        viewModel.onEvent(QuizPlayUiEvent.NextQuestion)

        viewModel.navigationEvent.test {
            viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(1))
            viewModel.onEvent(QuizPlayUiEvent.NextQuestion)

            assertThat(awaitItem()).isEqualTo(QuizPlayNavigationEvent.NavigateToResult("attempt-1"))
        }
    }

    @Test
    fun `submission failure surfaces an error and stops submitting`() = runTest {
        val viewModel = createViewModel()
        val error = Resource.Error<QuizAttempt>(UiText.DynamicString("offline"))
        coEvery { completeQuiz(any(), any(), any(), any(), any(), any(), any()) } returns error

        viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(1))
        viewModel.onEvent(QuizPlayUiEvent.NextQuestion)
        viewModel.onEvent(QuizPlayUiEvent.SelectAnswer(1))
        viewModel.onEvent(QuizPlayUiEvent.NextQuestion)

        val state = viewModel.uiState.value
        assertThat(state.isSubmitting).isFalse()
        assertThat(state.errorMessage).isEqualTo(UiText.DynamicString("offline"))
    }

    @Test
    fun `error shown clears the error message`() = runTest {
        coEvery { getQuizQuestions(any(), any()) } returns Resource.Error(UiText.DynamicString("offline"))
        val viewModel = createViewModel()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        viewModel.onEvent(QuizPlayUiEvent.ErrorShown)

        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
