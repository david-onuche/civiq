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

class RegisterWithEmailUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private lateinit var useCase: RegisterWithEmailUseCase

    @Before
    fun setUp() {
        useCase = RegisterWithEmailUseCase(authRepository)
    }

    @Test
    fun `delegates to repository with trimmed email and display name`() = runTest {
        val user = User(id = "u1", email = "user@example.com", displayName = "Ada")
        coEvery {
            authRepository.registerWithEmail("user@example.com", "password1", "Ada")
        } returns Resource.Success(user)

        val result = useCase(
            email = "  user@example.com  ",
            password = "password1",
            confirmPassword = "password1",
            displayName = "  Ada  ",
        )

        assertThat(result).isEqualTo(Resource.Success(user))
        coVerify { authRepository.registerWithEmail("user@example.com", "password1", "Ada") }
    }

    @Test
    fun `returns error when required fields are blank`() = runTest {
        val result = useCase(email = "", password = "password1", confirmPassword = "password1", displayName = "Ada")

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        coVerify(exactly = 0) { authRepository.registerWithEmail(any(), any(), any()) }
    }

    @Test
    fun `returns error for an invalid email`() = runTest {
        val result = useCase(
            email = "not-an-email",
            password = "password1",
            confirmPassword = "password1",
            displayName = "Ada",
        )

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        coVerify(exactly = 0) { authRepository.registerWithEmail(any(), any(), any()) }
    }

    @Test
    fun `returns error when password does not meet strength requirements`() = runTest {
        val result = useCase(
            email = "user@example.com",
            password = "weak",
            confirmPassword = "weak",
            displayName = "Ada",
        )

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        coVerify(exactly = 0) { authRepository.registerWithEmail(any(), any(), any()) }
    }

    @Test
    fun `returns error when passwords do not match`() = runTest {
        val result = useCase(
            email = "user@example.com",
            password = "password1",
            confirmPassword = "password2",
            displayName = "Ada",
        )

        assertThat(result).isInstanceOf(Resource.Error::class.java)
        coVerify(exactly = 0) { authRepository.registerWithEmail(any(), any(), any()) }
    }
}
