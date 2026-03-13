package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.CreateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.DeleteSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.UpdateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.SubunitManagementUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.SubunitManagementUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

@OptIn(ExperimentalCoroutinesApi::class)
class SubunitManagementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase
    private lateinit var createSubunitUseCase: CreateSubunitUseCase
    private lateinit var updateSubunitUseCase: UpdateSubunitUseCase
    private lateinit var deleteSubunitUseCase: DeleteSubunitUseCase
    private lateinit var getGroupByIdUseCase: GetGroupByIdUseCase
    private lateinit var getMemberProfilesUseCase: GetMemberProfilesUseCase
    private lateinit var subunitUiMapper: SubunitUiMapper
    private lateinit var viewModel: SubunitManagementViewModel

    private val testGroup = Group(
        id = "group-1",
        name = "Trip to Paris",
        members = listOf("user-1", "user-2", "user-3")
    )

    private val testSubunit = Subunit(
        id = "sub-1",
        groupId = "group-1",
        name = "Couple",
        memberIds = listOf("user-1", "user-2"),
        memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5)
    )

    private val testSubunitUiModel = SubunitUiModel(
        id = "sub-1",
        name = "Couple",
        memberNames = persistentListOf("Alice", "Bob"),
        memberCount = "2 members",
        sharesSummary = "50% / 50%"
    )

    private val testMemberProfiles = mapOf(
        "user-1" to User(userId = "user-1", email = "alice@test.com", displayName = "Alice"),
        "user-2" to User(userId = "user-2", email = "bob@test.com", displayName = "Bob"),
        "user-3" to User(userId = "user-3", email = "charlie@test.com", displayName = "Charlie")
    )

    private val testMemberUiModels = listOf(
        MemberUiModel(userId = "user-1", displayName = "Alice"),
        MemberUiModel(userId = "user-2", displayName = "Bob"),
        MemberUiModel(userId = "user-3", displayName = "Charlie")
    ).toImmutableList()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getGroupSubunitsFlowUseCase = mockk()
        createSubunitUseCase = mockk()
        updateSubunitUseCase = mockk()
        deleteSubunitUseCase = mockk()
        getGroupByIdUseCase = mockk()
        getMemberProfilesUseCase = mockk()
        subunitUiMapper = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = SubunitManagementViewModel(
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            createSubunitUseCase = createSubunitUseCase,
            updateSubunitUseCase = updateSubunitUseCase,
            deleteSubunitUseCase = deleteSubunitUseCase,
            getGroupByIdUseCase = getGroupByIdUseCase,
            getMemberProfilesUseCase = getMemberProfilesUseCase,
            subunitUiMapper = subunitUiMapper
        )
    }

    private fun setupDefaultMocks(subunits: List<Subunit> = listOf(testSubunit)) {
        coEvery { getGroupByIdUseCase("group-1") } returns testGroup
        coEvery { getMemberProfilesUseCase(testGroup.members) } returns testMemberProfiles
        every { getGroupSubunitsFlowUseCase("group-1") } returns flowOf(subunits)
        every {
            subunitUiMapper.toSubunitUiModelList(any(), any())
        } returns listOf(testSubunitUiModel).toImmutableList()
        every {
            subunitUiMapper.toMemberUiModelList(any(), any(), any(), any())
        } returns testMemberUiModels
    }

    @Nested
    inner class StateManagement {

        @Test
        fun `initial state is loading`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val state = viewModel.uiState.value
            assertTrue(state.isLoading)
            assertTrue(state.subunits.isEmpty())
        }

        @Test
        fun `emits subunits after setting groupId`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("group-1", state.groupId)
            assertEquals("Trip to Paris", state.groupName)
            assertEquals(1, state.subunits.size)
            assertEquals("Couple", state.subunits[0].name)

            collectJob.cancel()
        }

        @Test
        fun `does not re-emit when same groupId is set`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.setGroupId("group-1")
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            coVerify(exactly = 1) { getGroupByIdUseCase("group-1") }

            collectJob.cancel()
        }
    }

    @Nested
    inner class DialogEvents {

        @Test
        fun `ShowCreateDialog opens dialog with empty form`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isDialogVisible)
            assertNotNull(state.editingSubunit)
            assertEquals("", state.editingSubunit?.id)
            assertEquals("", state.editingSubunit?.name)
            assertTrue(state.editingSubunit?.selectedMemberIds?.isEmpty() == true)
            assertFalse(state.editingSubunit?.isEditing == true)

            collectJob.cancel()
        }

        @Test
        fun `ShowEditDialog opens dialog with pre-filled form`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowEditDialog("sub-1"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isDialogVisible)
            assertNotNull(state.editingSubunit)
            assertEquals("sub-1", state.editingSubunit?.id)
            assertEquals("Couple", state.editingSubunit?.name)
            assertTrue(state.editingSubunit?.isEditing == true)
            assertEquals(2, state.editingSubunit?.selectedMemberIds?.size)

            collectJob.cancel()
        }

        @Test
        fun `DismissDialog closes dialog`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isDialogVisible)

            viewModel.onEvent(SubunitManagementUiEvent.DismissDialog)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isDialogVisible)
            assertNull(viewModel.uiState.value.editingSubunit)

            collectJob.cancel()
        }

        @Test
        fun `UpdateName clears name error`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.UpdateName("Family"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Family", state.editingSubunit?.name)
            assertNull(state.nameError)

            collectJob.cancel()
        }

        @Test
        fun `ToggleMember adds and removes members`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()

            // Add a member
            viewModel.onEvent(SubunitManagementUiEvent.ToggleMember("user-1"))
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.editingSubunit?.selectedMemberIds?.size)
            assertTrue(viewModel.uiState.value.editingSubunit?.selectedMemberIds?.contains("user-1") == true)

            // Remove the member
            viewModel.onEvent(SubunitManagementUiEvent.ToggleMember("user-1"))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.editingSubunit?.selectedMemberIds?.isEmpty() == true)

            collectJob.cancel()
        }
    }

    @Nested
    inner class ValidationEvents {

        @Test
        fun `SaveSubunit shows name error when name is blank`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.SaveSubunit)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.nameError)
            assertTrue(state.nameError is UiText.StringResource)
            assertEquals(R.string.subunit_error_name_empty, (state.nameError as UiText.StringResource).resId)

            collectJob.cancel()
        }

        @Test
        fun `SaveSubunit shows members error when no members selected`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.UpdateName("Family"))
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.SaveSubunit)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.membersError)
            assertTrue(state.membersError is UiText.StringResource)
            assertEquals(R.string.subunit_error_no_members, (state.membersError as UiText.StringResource).resId)

            collectJob.cancel()
        }
    }

    @Nested
    inner class SaveSubunit {

        @Test
        fun `creates subunit and emits success action`() = runTest(testDispatcher) {
            setupDefaultMocks()
            coEvery { createSubunitUseCase(any(), any()) } returns Result.success("new-sub-id")
            createViewModel()

            val actions = mutableListOf<SubunitManagementUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()
            viewModel.onEvent(SubunitManagementUiEvent.UpdateName("Family"))
            viewModel.onEvent(SubunitManagementUiEvent.ToggleMember("user-3"))
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.SaveSubunit)
            advanceUntilIdle()

            coVerify { createSubunitUseCase("group-1", any()) }
            assertFalse(viewModel.uiState.value.isDialogVisible)
            assertTrue(actions.any { it is SubunitManagementUiAction.ShowSuccess })

            collectJob.cancel()
            actionsJob.cancel()
        }

        @Test
        fun `updates subunit and emits success action`() = runTest(testDispatcher) {
            setupDefaultMocks()
            coEvery { updateSubunitUseCase(any(), any()) } returns Result.success(Unit)
            createViewModel()

            val actions = mutableListOf<SubunitManagementUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowEditDialog("sub-1"))
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.SaveSubunit)
            advanceUntilIdle()

            coVerify { updateSubunitUseCase("group-1", any()) }
            assertFalse(viewModel.uiState.value.isDialogVisible)
            assertTrue(actions.any { it is SubunitManagementUiAction.ShowSuccess })

            collectJob.cancel()
            actionsJob.cancel()
        }

        @Test
        fun `emits error action when save fails`() = runTest(testDispatcher) {
            setupDefaultMocks()
            coEvery { createSubunitUseCase(any(), any()) } returns Result.failure(Exception("Network error"))
            createViewModel()

            val actions = mutableListOf<SubunitManagementUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ShowCreateDialog)
            advanceUntilIdle()
            viewModel.onEvent(SubunitManagementUiEvent.UpdateName("Family"))
            viewModel.onEvent(SubunitManagementUiEvent.ToggleMember("user-3"))
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.SaveSubunit)
            advanceUntilIdle()

            assertTrue(actions.any { it is SubunitManagementUiAction.ShowError })

            collectJob.cancel()
            actionsJob.cancel()
        }
    }

    @Nested
    inner class DeleteSubunit {

        @Test
        fun `ConfirmDeleteSubunit calls use case and emits success`() = runTest(testDispatcher) {
            setupDefaultMocks()
            coEvery { deleteSubunitUseCase(any(), any()) } returns Unit
            createViewModel()

            val actions = mutableListOf<SubunitManagementUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ConfirmDeleteSubunit("sub-1"))
            advanceUntilIdle()

            coVerify { deleteSubunitUseCase("group-1", "sub-1") }
            assertTrue(actions.any { it is SubunitManagementUiAction.ShowSuccess })

            collectJob.cancel()
            actionsJob.cancel()
        }

        @Test
        fun `ConfirmDeleteSubunit emits error action when deletion fails`() = runTest(testDispatcher) {
            setupDefaultMocks()
            coEvery { deleteSubunitUseCase(any(), any()) } throws RuntimeException("Delete failed")
            createViewModel()

            val actions = mutableListOf<SubunitManagementUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.setGroupId("group-1")
            advanceUntilIdle()

            viewModel.onEvent(SubunitManagementUiEvent.ConfirmDeleteSubunit("sub-1"))
            advanceUntilIdle()

            assertTrue(actions.any { it is SubunitManagementUiAction.ShowError })

            collectJob.cancel()
            actionsJob.cancel()
        }
    }
}

