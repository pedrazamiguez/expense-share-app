package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel

import es.pedrazamiguez.splittrip.domain.enums.Currency
import es.pedrazamiguez.splittrip.domain.usecase.auth.SignOutUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("SettingsViewModel")
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var getUserDefaultCurrencyUseCase: GetUserDefaultCurrencyUseCase

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        signOutUseCase = mockk()
        getUserDefaultCurrencyUseCase = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel = SettingsViewModel(
        signOutUseCase = signOutUseCase,
        getUserDefaultCurrencyUseCase = getUserDefaultCurrencyUseCase
    )

    // ── currentCurrency StateFlow ───────────────────────────────────────────

    @Nested
    @DisplayName("currentCurrency")
    inner class CurrentCurrency {

        @Test
        fun `emits Currency from use case flow`() = runTest(testDispatcher) {
            every { getUserDefaultCurrencyUseCase() } returns flowOf("USD")

            val viewModel = createViewModel()

            // Subscribe to trigger WhileSubscribed upstream
            val collectJob = launch { viewModel.currentCurrency.collect {} }
            advanceUntilIdle()

            assertEquals(Currency.USD, viewModel.currentCurrency.value)
            collectJob.cancel()
        }

        @Test
        fun `invalid code falls back to EUR`() = runTest(testDispatcher) {
            every { getUserDefaultCurrencyUseCase() } returns flowOf("INVALID_CODE")

            val viewModel = createViewModel()

            val collectJob = launch { viewModel.currentCurrency.collect {} }
            advanceUntilIdle()

            assertEquals(Currency.EUR, viewModel.currentCurrency.value)
            collectJob.cancel()
        }

        @Test
        fun `initial value is null before flow emits`() = runTest(testDispatcher) {
            every { getUserDefaultCurrencyUseCase() } returns flowOf("EUR")

            val viewModel = createViewModel()
            // Don't advance — check initial value
            assertNull(viewModel.currentCurrency.value)
        }
    }

    // ── updateNotificationPermission ────────────────────────────────────────

    @Nested
    @DisplayName("updateNotificationPermission")
    inner class UpdateNotificationPermission {

        @Test
        fun `updates hasNotificationPermission state`() = runTest(testDispatcher) {
            every { getUserDefaultCurrencyUseCase() } returns flowOf("EUR")
            val viewModel = createViewModel()

            assertFalse(viewModel.hasNotificationPermission.value)

            viewModel.updateNotificationPermission(true)

            assertTrue(viewModel.hasNotificationPermission.value)
        }
    }

    // ── signOut ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("signOut")
    inner class SignOut {

        @Test
        fun `success calls onSignedOut`() = runTest(testDispatcher) {
            every { getUserDefaultCurrencyUseCase() } returns flowOf("EUR")
            coEvery { signOutUseCase() } returns Result.success(Unit)

            val viewModel = createViewModel()

            var signedOutCalled = false
            viewModel.signOut { signedOutCalled = true }
            advanceUntilIdle()

            assertTrue(signedOutCalled)
        }

        @Test
        fun `failure does not call onSignedOut`() = runTest(testDispatcher) {
            every { getUserDefaultCurrencyUseCase() } returns flowOf("EUR")
            coEvery { signOutUseCase() } returns Result.failure(RuntimeException("Error"))

            val viewModel = createViewModel()

            var signedOutCalled = false
            viewModel.signOut { signedOutCalled = true }
            advanceUntilIdle()

            assertFalse(signedOutCalled)
        }
    }
}
