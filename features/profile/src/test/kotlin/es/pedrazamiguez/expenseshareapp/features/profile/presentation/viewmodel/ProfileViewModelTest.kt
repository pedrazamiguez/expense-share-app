package es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetCurrentUserProfileUseCase
import es.pedrazamiguez.expenseshareapp.features.profile.R
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.mapper.ProfileUiMapper
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.model.ProfileUiModel
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.action.ProfileUiAction
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase
    private lateinit var profileUiMapper: ProfileUiMapper
    private lateinit var viewModel: ProfileViewModel

    private val testUser = User(
        userId = "user-123",
        email = "test@example.com",
        displayName = "Test User",
        profileImagePath = "https://example.com/photo.jpg",
        createdAt = LocalDateTime.of(2024, 6, 15, 10, 30)
    )

    private val testProfileUiModel = ProfileUiModel(
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = "https://example.com/photo.jpg",
        memberSinceText = "June 2024"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserProfileUseCase = mockk()
        profileUiMapper = mockk()

        every { profileUiMapper.toProfileUiModel(testUser) } returns testProfileUiModel
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class InitialLoad {

        @Test
        fun `loads profile successfully on init`() = runTest(testDispatcher) {
            // Given
            coEvery { getCurrentUserProfileUseCase() } returns testUser

            // When
            viewModel = ProfileViewModel(getCurrentUserProfileUseCase, profileUiMapper)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.profile)
            assertEquals("Test User", state.profile?.displayName)
            assertEquals("test@example.com", state.profile?.email)
            assertEquals("https://example.com/photo.jpg", state.profile?.profileImageUrl)
            assertEquals("June 2024", state.profile?.memberSinceText)
            assertNull(state.errorMessage)
        }

        @Test
        fun `sets error state when user is null`() = runTest(testDispatcher) {
            // Given
            coEvery { getCurrentUserProfileUseCase() } returns null

            // When
            viewModel = ProfileViewModel(getCurrentUserProfileUseCase, profileUiMapper)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNull(state.profile)
            assertNotNull(state.errorMessage)
            assertTrue(state.errorMessage is UiText.StringResource)
            assertEquals(
                R.string.profile_error_loading,
                (state.errorMessage as UiText.StringResource).resId
            )
        }

        @Test
        fun `emits ShowError action when user is null`() = runTest(testDispatcher) {
            // Given — init loads null, so set up accordingly
            coEvery { getCurrentUserProfileUseCase() } returns null
            viewModel = ProfileViewModel(getCurrentUserProfileUseCase, profileUiMapper)

            // Start collecting actions
            val emittedActions = mutableListOf<ProfileUiAction>()
            val collectJob = backgroundScope.launch {
                viewModel.actions.collect { emittedActions.add(it) }
            }

            // When — reload triggers another null → ShowError
            viewModel.onEvent(
                es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.event.ProfileUiEvent.LoadProfile
            )
            advanceUntilIdle()

            // Then — actions from init + reload (both emit ShowError)
            assertTrue(emittedActions.isNotEmpty())
            assertTrue(emittedActions.all { it is ProfileUiAction.ShowError })

            collectJob.cancel()
        }

        @Test
        fun `sets error state when use case throws exception`() = runTest(testDispatcher) {
            // Given
            coEvery { getCurrentUserProfileUseCase() } throws RuntimeException("Network error")

            // When
            viewModel = ProfileViewModel(getCurrentUserProfileUseCase, profileUiMapper)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNull(state.profile)
            assertNotNull(state.errorMessage)
            assertTrue(state.errorMessage is UiText.StringResource)
            assertEquals(
                R.string.profile_error_loading,
                (state.errorMessage as UiText.StringResource).resId
            )
        }

        @Test
        fun `emits ShowError action when use case throws exception`() =
            runTest(testDispatcher) {
                // Given — init throws, set up accordingly
                coEvery { getCurrentUserProfileUseCase() } throws RuntimeException("Network error")
                viewModel = ProfileViewModel(getCurrentUserProfileUseCase, profileUiMapper)

                // Start collecting actions
                val emittedActions = mutableListOf<ProfileUiAction>()
                val collectJob = backgroundScope.launch {
                    viewModel.actions.collect { emittedActions.add(it) }
                }

                // When — reload triggers another exception → ShowError
                viewModel.onEvent(
                    es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.event.ProfileUiEvent.LoadProfile
                )
                advanceUntilIdle()

                // Then — actions from init + reload (both emit ShowError)
                assertTrue(emittedActions.isNotEmpty())
                assertTrue(emittedActions.all { it is ProfileUiAction.ShowError })

                collectJob.cancel()
            }
    }

    @Nested
    inner class ReloadProfile {

        @Test
        fun `reloads profile on LoadProfile event`() = runTest(testDispatcher) {
            // Given - first load returns null
            coEvery { getCurrentUserProfileUseCase() } returns null
            viewModel = ProfileViewModel(getCurrentUserProfileUseCase, profileUiMapper)
            advanceUntilIdle()

            // Verify initial error state
            assertNull(viewModel.uiState.value.profile)

            // Given - second load returns user
            coEvery { getCurrentUserProfileUseCase() } returns testUser

            // When
            viewModel.onEvent(
                es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.event.ProfileUiEvent.LoadProfile
            )
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.profile)
            assertEquals("Test User", state.profile?.displayName)
            assertNull(state.errorMessage)
        }
    }
}







