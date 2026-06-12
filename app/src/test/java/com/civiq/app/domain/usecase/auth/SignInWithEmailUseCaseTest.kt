package com.civiq.app.domain.usecase.auth

import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.utils.Resource
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignInWithEmailUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private lateinit var useCase: SignInWithEmailUseCase

    @Before
    fun setUp() {
        useCase = SignInWithEmailUseCase(authRepository)
    }

    @Test
    fun `delegates to repository with a trimmed email`() = runTest {
        val user = User(id = "u1", email = "user@example.com")
        coEvery { authRepository.signInWithEmail("user@example.com", "password1") } returns Resource.Success(user)

        val result = useCase("  user@example.com  ", "password1")

        assertThat(result).isEqualTo(Resource.Success(user))
        coVerify { authRepository.signInWithEmail("user@example.com", "password1") }
    }

    @Test
    fun `returns error without calling repository when email or password is blank`() = runTest {
        val result = useCase("", "password1")

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        coVerify(exactly = 0) { authRepository.signInWithEmail(any(), any()) }
    }

    @Test
    fun `returns error without calling repository for an invalid email`() = runTest {
        val result = useCase("not-an-email", "password1")

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        coVerify(exactly = 0) { authRepository.signInWithEmail(any(), any()) }
    }
}
