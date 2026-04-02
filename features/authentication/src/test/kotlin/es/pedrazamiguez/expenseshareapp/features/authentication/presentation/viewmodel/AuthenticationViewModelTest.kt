package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignInWithEmailUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.auth.SignInWithGoogleUseCase
import es.pedrazamiguez.expenseshareapp.features.authentication.R
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiEvent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("AuthenticationViewModel")
class AuthenticationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var signInWithEmailUseCase: SignInWithEmailUseCase
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var viewModel: AuthenticationViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        signInWithEmailUseCase = mockk()
        signInWithGoogleUseCase = mockk()

        viewModel = AuthenticationViewModel(
            signInWithEmailUseCase = signInWithEmailUseCase,
            signInWithGoogleUseCase = signInWithGoogleUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── EmailChanged / PasswordChanged ──────────────────────────────────────

    @Nested
    @DisplayName("Field Updates")
    inner class FieldUpdates {

        @Test
        fun `EmailChanged updates email in state`() = runTest(testDispatcher) {
            viewModel.onEvent(AuthenticationUiEvent.EmailChanged("user@test.com")) {}

            assertEquals("user@test.com", viewModel.uiState.value.email)
        }

        @Test
        fun `PasswordChanged updates password in state`() = runTest(testDispatcher) {
            viewModel.onEvent(AuthenticationUiEvent.PasswordChanged("secret123")) {}

            assertEquals("secret123", viewModel.uiState.value.password)
        }
    }

    // ── SubmitLogin ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("SubmitLogin")
    inner class SubmitLogin {

        @Test
        fun `success calls onLoginSuccess and clears loading`() =
            runTest(testDispatcher) {
                coEvery {
                    signInWithEmailUseCase(any(), any())
                } returns Result.success("user-id-1")

                viewModel.onEvent(AuthenticationUiEvent.EmailChanged("a@b.com")) {}
                viewModel.onEvent(AuthenticationUiEvent.PasswordChanged("pass")) {}

                var successCalled = false
                viewModel.onEvent(AuthenticationUiEvent.SubmitLogin) { successCalled = true }
                advanceUntilIdle()

                assertTrue(successCalled)
                assertFalse(viewModel.uiState.value.isLoading)
                assertNull(viewModel.uiState.value.error)
            }

        @Test
        fun `failure sets error in state`() = runTest(testDispatcher) {
            coEvery {
                signInWithEmailUseCase(any(), any())
            } returns Result.failure(RuntimeException("Invalid credentials"))

            viewModel.onEvent(AuthenticationUiEvent.EmailChanged("a@b.com")) {}
            viewModel.onEvent(AuthenticationUiEvent.PasswordChanged("wrong")) {}

            viewModel.onEvent(AuthenticationUiEvent.SubmitLogin) {}
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertNotNull(viewModel.uiState.value.error)
            assertTrue(viewModel.uiState.value.error is UiText.DynamicString)
        }
    }

    // ── GoogleSignInResult ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GoogleSignInResult")
    inner class GoogleSignInResult {

        @Test
        fun `success calls onLoginSuccess and clears loading`() =
            runTest(testDispatcher) {
                coEvery {
                    signInWithGoogleUseCase(any())
                } returns Result.success("google-user-id")

                var successCalled = false
                viewModel.onEvent(
                    AuthenticationUiEvent.GoogleSignInResult("id-token-123")
                ) { successCalled = true }
                advanceUntilIdle()

                assertTrue(successCalled)
                assertFalse(viewModel.uiState.value.isGoogleLoading)
                assertNull(viewModel.uiState.value.error)
            }

        @Test
        fun `failure sets error in state`() = runTest(testDispatcher) {
            coEvery {
                signInWithGoogleUseCase(any())
            } returns Result.failure(RuntimeException("Token expired"))

            viewModel.onEvent(
                AuthenticationUiEvent.GoogleSignInResult("bad-token")
            ) {}
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isGoogleLoading)
            assertNotNull(viewModel.uiState.value.error)
            assertTrue(viewModel.uiState.value.error is UiText.DynamicString)
        }
    }

    // ── GoogleSignInFailed ──────────────────────────────────────────────────

    @Nested
    @DisplayName("GoogleSignInFailed")
    inner class GoogleSignInFailed {

        @Test
        fun `sets error as StringResource`() = runTest(testDispatcher) {
            viewModel.onEvent(AuthenticationUiEvent.GoogleSignInFailed) {}

            val error = viewModel.uiState.value.error
            assertNotNull(error)
            assertTrue(error is UiText.StringResource)
            assertEquals(
                R.string.login_google_error,
                (error as UiText.StringResource).resId
            )
            assertFalse(viewModel.uiState.value.isGoogleLoading)
        }
    }
}
