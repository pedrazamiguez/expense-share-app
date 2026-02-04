package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.DeleteGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.GroupsUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.GroupsUiEvent
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getUserGroupsFlowUseCase: GetUserGroupsFlowUseCase
    private lateinit var deleteGroupUseCase: DeleteGroupUseCase
    private lateinit var groupUiMapper: GroupUiMapper
    private lateinit var viewModel: GroupsViewModel

    private val testGroup1 = Group(
        id = "group-1",
        name = "Trip to Paris",
        description = "Summer vacation",
        currency = "EUR",
        extraCurrencies = emptyList(),
        members = listOf("user-1", "user-2", "user-3"),
        createdAt = LocalDateTime.of(2024, 1, 15, 12, 0),
        lastUpdatedAt = LocalDateTime.of(2024, 1, 15, 12, 0)
    )

    private val testGroup2 = Group(
        id = "group-2",
        name = "Office Lunch",
        description = "Daily lunches",
        currency = "USD",
        extraCurrencies = emptyList(),
        members = listOf("user-1", "user-2", "user-3", "user-4", "user-5"),
        createdAt = LocalDateTime.of(2024, 2, 1, 10, 0),
        lastUpdatedAt = LocalDateTime.of(2024, 2, 1, 10, 0)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserGroupsFlowUseCase = mockk()
        deleteGroupUseCase = mockk()
        groupUiMapper = mockk()

        // Mock the mapper to return predictable UI models
        every { groupUiMapper.toGroupUiModelList(any()) } answers {
            val groups = firstArg<List<Group>>()
            groups.map { group ->
                GroupUiModel(
                    id = group.id,
                    name = group.name,
                    description = group.description,
                    currency = group.currency,
                    membersCountText = "${group.members.size} travelers",
                    dateText = group.createdAt?.toString() ?: ""
                )
            }.toImmutableList()
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class StateManagement {

        @Test
        fun `initial state is loading`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(emptyList())

            // When
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.isLoading)
            assertTrue(state.groups.isEmpty())
        }

        @Test
        fun `emits groups from use case`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1, testGroup2))
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            // Start collecting to activate the WhileSubscribed flow
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            // When
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.groups.size)
            assertEquals("Trip to Paris", state.groups[0].name)
            assertEquals("Office Lunch", state.groups[1].name)

            collectJob.cancel()
        }

        @Test
        fun `handles empty groups list`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(emptyList())
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            // When
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.groups.isEmpty())
            assertNull(state.errorMessage)

            collectJob.cancel()
        }

        @Test
        fun `handles error from use case`() = runTest(testDispatcher) {
            // Given
            val errorMessage = "Network error"
            every { getUserGroupsFlowUseCase() } returns flow {
                throw RuntimeException(errorMessage)
            }
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            // When
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.groups.isEmpty())
            assertEquals(errorMessage, state.errorMessage)

            collectJob.cancel()
        }
    }

    @Nested
    inner class DeleteGroupEvent {

        @Test
        fun `DeleteGroup event calls use case with correct groupId`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1, testGroup2))
            coEvery { deleteGroupUseCase(any()) } just Runs
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            // When
            viewModel.onEvent(GroupsUiEvent.DeleteGroup("group-1"))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { deleteGroupUseCase("group-1") }

            collectJob.cancel()
        }

        @Test
        fun `DeleteGroup event handles deletion gracefully`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1))
            coEvery { deleteGroupUseCase(any()) } just Runs
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            // When - Delete event should not throw
            viewModel.onEvent(GroupsUiEvent.DeleteGroup("group-1"))
            advanceUntilIdle()

            // Then - State should still be valid
            assertFalse(viewModel.uiState.value.isLoading)

            collectJob.cancel()
        }

        @Test
        fun `multiple delete events are handled independently`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1, testGroup2))
            coEvery { deleteGroupUseCase(any()) } just Runs
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            // When
            viewModel.onEvent(GroupsUiEvent.DeleteGroup("group-1"))
            viewModel.onEvent(GroupsUiEvent.DeleteGroup("group-2"))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { deleteGroupUseCase("group-1") }
            coVerify(exactly = 1) { deleteGroupUseCase("group-2") }

            collectJob.cancel()
        }

        @Test
        fun `DeleteGroup event emits error action when deletion fails`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1))
            val exception = RuntimeException("Database error")
            coEvery { deleteGroupUseCase(any()) } throws exception
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            // Collect actions in background
            val actions = mutableListOf<GroupsUiAction>()
            val actionsJob = backgroundScope.launch {
                viewModel.actions.collect { actions.add(it) }
            }

            // When
            viewModel.onEvent(GroupsUiEvent.DeleteGroup("group-1"))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { deleteGroupUseCase("group-1") }
            assertEquals(1, actions.size)
            assertTrue(actions[0] is GroupsUiAction.ShowDeleteError)

            actionsJob.cancel()
            collectJob.cancel()
        }

        @Test
        fun `DeleteGroup event emits success action when deletion succeeds`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1))
            coEvery { deleteGroupUseCase(any()) } just Runs
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            // Collect actions in background
            val actions = mutableListOf<GroupsUiAction>()
            val actionsJob = backgroundScope.launch {
                viewModel.actions.collect { actions.add(it) }
            }

            // When
            viewModel.onEvent(GroupsUiEvent.DeleteGroup("group-1"))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { deleteGroupUseCase("group-1") }
            assertEquals(1, actions.size)
            assertTrue(actions[0] is GroupsUiAction.ShowDeleteSuccess)

            actionsJob.cancel()
            collectJob.cancel()
        }
    }

    @Nested
    inner class ScrollPositionEvent {

        @Test
        fun `ScrollPositionChanged updates scroll state`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1))
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            // When
            viewModel.onEvent(GroupsUiEvent.ScrollPositionChanged(5, 100))
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(5, state.scrollPosition)
            assertEquals(100, state.scrollOffset)

            collectJob.cancel()
        }

        @Test
        fun `scroll position is preserved across state updates`() = runTest(testDispatcher) {
            // Given
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1))
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            // When - Set scroll position
            viewModel.onEvent(GroupsUiEvent.ScrollPositionChanged(3, 50))
            advanceUntilIdle()

            // Then - Scroll position should be preserved
            val state = viewModel.uiState.value
            assertEquals(3, state.scrollPosition)
            assertEquals(50, state.scrollOffset)

            collectJob.cancel()
        }
    }

    @Nested
    inner class LoadGroupsEvent {

        @Test
        fun `LoadGroups event is a no-op with stateIn`() = runTest(testDispatcher) {
            // Given - Data loads automatically via stateIn
            every { getUserGroupsFlowUseCase() } returns flowOf(listOf(testGroup1))
            viewModel = GroupsViewModel(getUserGroupsFlowUseCase, deleteGroupUseCase, groupUiMapper)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val initialState = viewModel.uiState.value

            // When - LoadGroups should have no effect
            viewModel.onEvent(GroupsUiEvent.LoadGroups)
            advanceUntilIdle()

            // Then - State should remain the same
            assertEquals(initialState.groups.size, viewModel.uiState.value.groups.size)

            collectJob.cancel()
        }
    }
}
