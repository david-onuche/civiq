package com.civiq.app.presentation.auth

import com.civiq.app.MainDispatcherRule
import com.civiq.app.domain.model.User
import com.civiq.app.domain.usecase.auth.AuthUseCases
import com.civiq.app.domain.usecase.auth.ContinueAsGuestUseCase
import com.civiq.app.domain.usecase.auth.ObserveCurrentUserUseCase
import com.civiq.app.domain.usecase.auth.RegisterWithEmailUseCase
import com.civiq.app.domain.usecase.auth.SendPasswordResetUseCase
import com.civiq.app.domain.usecase.auth.SignInWithEmailUseCase
import com.civiq.app.domain.usecase.auth.SignInWithGoogleUseCase
import com.civiq.app.domain.usecase.auth.SignOutUseCase
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val signInWithEmail = mockk<SignInWithEmailUseCase>()
    private val registerWithEmail = mockk<RegisterWithEmailUseCase>()
    private val signInWithGoogle = mockk<SignInWithGoogleUseCase>()
    private val sendPasswordReset = mockk<SendPasswordResetUseCase>()
    private val signOut = mockk<SignOutUseCase>()
    private val continueAsGuest = mockk<ContinueAsGuestUseCase>()
    private val observeCurrentUser = mockk<ObserveCurrentUserUseCase>()

    private lateinit var viewModel: AuthViewModel

    private val user = User(id = "user-1", email = "user@example.com", displayName = "Ada")

    @Before
    fun setUp() {
        val authUseCases = AuthUseCases(
            signInWithEmail = signInWithEmail,
            registerWithEmail = registerWithEmail,
            signInWithGoogle = signInWithGoogle,
            sendPasswordReset = sendPasswordReset,
            signOut = signOut,
            continueAsGuest = continueAsGuest,
            observeCurrentUser = observeCurrentUser,
        )
        viewModel = AuthViewModel(authUseCases)
    }

    @Test
    fun `field changes update state and clear any existing error`() {
        viewModel.onEvent(AuthUiEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("password1"))
        viewModel.onEvent(AuthUiEvent.ConfirmPasswordChanged("password1"))
        viewModel.onEvent(AuthUiEvent.DisplayNameChanged("Ada"))

        val state = viewModel.uiState.value
        assertThat(state.email).isEqualTo("user@example.com")
        assertThat(state.password).isEqualTo("password1")
        assertThat(state.confirmPassword).isEqualTo("password1")
        assertThat(state.displayName).isEqualTo("Ada")
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun `sign in success marks the session authenticated`() = runTest {
        coEvery { signInWithEmail(any(), any()) } returns Resource.Success(user)

        viewModel.onEvent(AuthUiEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("password1"))
        viewModel.onEvent(AuthUiEvent.SignIn)

        val state = viewModel.uiState.value
        assertThat(state.isAuthenticated).isTrue()
        assertThat(state.isLoading).isFalse()
        coVerify { signInWithEmail("user@example.com", "password1") }
    }

    @Test
    fun `sign in failure surfaces the error message without authenticating`() = runTest {
        val error = UiText.DynamicString("Invalid credentials")
        coEvery { signInWithEmail(any(), any()) } returns Resource.Error(error)

        viewModel.onEvent(AuthUiEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("wrong-password"))
        viewModel.onEvent(AuthUiEvent.SignIn)

        val state = viewModel.uiState.value
        assertThat(state.isAuthenticated).isFalse()
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isEqualTo(error)
    }

    @Test
    fun `register success forwards all fields and authenticates`() = runTest {
        coEvery { registerWithEmail(any(), any(), any(), any()) } returns Resource.Success(user)

        viewModel.onEvent(AuthUiEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthUiEvent.PasswordChanged("password1"))
        viewModel.onEvent(AuthUiEvent.ConfirmPasswordChanged("password1"))
        viewModel.onEvent(AuthUiEvent.DisplayNameChanged("Ada"))
        viewModel.onEvent(AuthUiEvent.Register)

        assertThat(viewModel.uiState.value.isAuthenticated).isTrue()
        coVerify {
            registerWithEmail(
                email = "user@example.com",
                password = "password1",
                confirmPassword = "password1",
                displayName = "Ada",
            )
        }
    }

    @Test
    fun `continue as guest authenticates the session`() = runTest {
        coEvery { continueAsGuest() } returns Resource.Success(user)

        viewModel.onEvent(AuthUiEvent.ContinueAsGuest)

        assertThat(viewModel.uiState.value.isAuthenticated).isTrue()
    }

    @Test
    fun `sign in with google forwards the id token`() = runTest {
        coEvery { signInWithGoogle(any()) } returns Resource.Success(user)

        viewModel.onEvent(AuthUiEvent.SignInWithGoogle("id-token-123"))

        assertThat(viewModel.uiState.value.isAuthenticated).isTrue()
        coVerify { signInWithGoogle("id-token-123") }
    }

    @Test
    fun `password reset success sets passwordResetEmailSent`() = runTest {
        coEvery { sendPasswordReset(any()) } returns Resource.Success(Unit)

        viewModel.onEvent(AuthUiEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(AuthUiEvent.SendPasswordResetEmail)

        val state = viewModel.uiState.value
        assertThat(state.passwordResetEmailSent).isTrue()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `password reset failure surfaces an error message`() = runTest {
        val error = UiText.DynamicString("Enter a valid email address.")
        coEvery { sendPasswordReset(any()) } returns Resource.Error(error)

        viewModel.onEvent(AuthUiEvent.EmailChanged("not-an-email"))
        viewModel.onEvent(AuthUiEvent.SendPasswordResetEmail)

        val state = viewModel.uiState.value
        assertThat(state.passwordResetEmailSent).isFalse()
        assertThat(state.errorMessage).isEqualTo(error)
    }

    @Test
    fun `sign out resets state and marks the session signed out`() = runTest {
        coJustRun { signOut() }

        viewModel.onEvent(AuthUiEvent.SignOut)

        assertThat(viewModel.uiState.value).isEqualTo(AuthUiState(isSignedOut = true))
        coVerify { signOut() }
    }

    @Test
    fun `error shown clears the error message`() = runTest {
        val error = UiText.DynamicString("Invalid credentials")
        coEvery { signInWithEmail(any(), any()) } returns Resource.Error(error)
        viewModel.onEvent(AuthUiEvent.SignIn)
        assertThat(viewModel.uiState.value.errorMessage).isEqualTo(error)

        viewModel.onEvent(AuthUiEvent.ErrorShown)

        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
