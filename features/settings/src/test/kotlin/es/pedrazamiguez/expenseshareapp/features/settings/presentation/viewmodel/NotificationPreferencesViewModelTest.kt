package es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationCategory
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationPreferences
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.GetNotificationPreferencesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UpdateNotificationPreferenceUseCase
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.model.NotificationPreferencesUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("NotificationPreferencesViewModel")
class NotificationPreferencesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPreferencesUseCase: GetNotificationPreferencesUseCase
    private lateinit var updatePreferenceUseCase: UpdateNotificationPreferenceUseCase
    private lateinit var viewModel: NotificationPreferencesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getPreferencesUseCase = mockk()
        updatePreferenceUseCase = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        prefs: NotificationPreferences = NotificationPreferences()
    ): NotificationPreferencesViewModel {
        every { getPreferencesUseCase() } returns flowOf(prefs)
        return NotificationPreferencesViewModel(getPreferencesUseCase, updatePreferenceUseCase)
    }

    @Nested
    @DisplayName("uiState")
    inner class UiStateTests {

        @Test
        fun `initial state has isLoading true`() = runTest(testDispatcher) {
            viewModel = createViewModel()
            val state = viewModel.uiState.value
            assertTrue(state.isLoading)
        }

        @Test
        fun `emits preferences from use case`() = runTest(testDispatcher) {
            val prefs = NotificationPreferences(
                membershipEnabled = false,
                expensesEnabled = true,
                financialEnabled = false
            )
            viewModel = createViewModel(prefs)
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.membershipEnabled)
            assertTrue(state.expensesEnabled)
            assertFalse(state.financialEnabled)

            collectJob.cancel()
        }

        @Test
        fun `all defaults are true`() = runTest(testDispatcher) {
            viewModel = createViewModel()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.membershipEnabled)
            assertTrue(state.expensesEnabled)
            assertTrue(state.financialEnabled)

            collectJob.cancel()
        }
    }

    @Nested
    @DisplayName("onEvent")
    inner class OnEventTests {

        @Test
        fun `ToggleCategory MEMBERSHIP delegates to use case`() = runTest(testDispatcher) {
            viewModel = createViewModel()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onEvent(
                NotificationPreferencesUiEvent.ToggleCategory(
                    NotificationCategory.MEMBERSHIP, false
                )
            )
            advanceUntilIdle()

            coVerify { updatePreferenceUseCase(NotificationCategory.MEMBERSHIP, false) }
            collectJob.cancel()
        }

        @Test
        fun `ToggleCategory EXPENSES delegates to use case`() = runTest(testDispatcher) {
            viewModel = createViewModel()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onEvent(
                NotificationPreferencesUiEvent.ToggleCategory(
                    NotificationCategory.EXPENSES, true
                )
            )
            advanceUntilIdle()

            coVerify { updatePreferenceUseCase(NotificationCategory.EXPENSES, true) }
            collectJob.cancel()
        }

        @Test
        fun `ToggleCategory FINANCIAL delegates to use case`() = runTest(testDispatcher) {
            viewModel = createViewModel()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onEvent(
                NotificationPreferencesUiEvent.ToggleCategory(
                    NotificationCategory.FINANCIAL, false
                )
            )
            advanceUntilIdle()

            coVerify { updatePreferenceUseCase(NotificationCategory.FINANCIAL, false) }
            collectJob.cancel()
        }
    }

    @Nested
    @DisplayName("error handling")
    inner class ErrorHandling {

        @Test
        fun `toggle does not crash on use case exception`() = runTest(testDispatcher) {
            viewModel = createViewModel()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            coEvery {
                updatePreferenceUseCase(any(), any())
            } throws RuntimeException("Network error")

            viewModel.onEvent(
                NotificationPreferencesUiEvent.ToggleCategory(
                    NotificationCategory.MEMBERSHIP, false
                )
            )
            advanceUntilIdle()

            // Should not crash — error is caught and logged
            assertEquals(true, viewModel.uiState.value.membershipEnabled)
            collectJob.cancel()
        }
    }
}


