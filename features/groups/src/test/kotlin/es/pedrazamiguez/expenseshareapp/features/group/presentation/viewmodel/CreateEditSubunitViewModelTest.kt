package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.service.SubunitShareDistributionService
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.CreateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.UpdateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateEditSubunitUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateEditSubunitUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("CreateEditSubunitViewModel")
class CreateEditSubunitViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var createSubunitUseCase: CreateSubunitUseCase
    private lateinit var updateSubunitUseCase: UpdateSubunitUseCase
    private lateinit var getGroupByIdUseCase: GetGroupByIdUseCase
    private lateinit var getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase
    private lateinit var getMemberProfilesUseCase: GetMemberProfilesUseCase
    private lateinit var subunitUiMapper: SubunitUiMapper
    private lateinit var shareDistributionService: SubunitShareDistributionService
    private lateinit var viewModel: CreateEditSubunitViewModel

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
        memberShares = mapOf("user-1" to BigDecimal("0.5"), "user-2" to BigDecimal("0.5"))
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
        createSubunitUseCase = mockk()
        updateSubunitUseCase = mockk()
        getGroupByIdUseCase = mockk()
        getGroupSubunitsFlowUseCase = mockk()
        getMemberProfilesUseCase = mockk()
        subunitUiMapper = mockk()
        shareDistributionService = SubunitShareDistributionService()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = CreateEditSubunitViewModel(
            createSubunitUseCase = createSubunitUseCase,
            updateSubunitUseCase = updateSubunitUseCase,
            getGroupByIdUseCase = getGroupByIdUseCase,
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            getMemberProfilesUseCase = getMemberProfilesUseCase,
            subunitUiMapper = subunitUiMapper,
            shareDistributionService = shareDistributionService
        )
    }

    private fun setupDefaultMocks(subunits: List<Subunit> = emptyList()) {
        coEvery { getGroupByIdUseCase("group-1") } returns testGroup
        coEvery { getMemberProfilesUseCase(testGroup.members) } returns testMemberProfiles
        every { getGroupSubunitsFlowUseCase("group-1") } returns flowOf(subunits)
        every {
            subunitUiMapper.toMemberUiModelList(any(), any(), any(), any())
        } returns testMemberUiModels
        // Stub formatShareAsPercentage — called by toggleMember/updateMemberShare/edit-mode pre-fill
        every { subunitUiMapper.formatShareAsPercentage(any()) } answers {
            val share = firstArg<BigDecimal>()
            val percent = share.multiply(BigDecimal("100"))
            if (percent.stripTrailingZeros().scale() <= 0) {
                percent.toLong().toString()
            } else {
                percent.toPlainString()
            }
        }
    }

    @Nested
    @DisplayName("Create Mode")
    inner class CreateMode {

        @Test
        fun `initial state is loading`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val state = viewModel.uiState.value
            assertTrue(state.isLoading)
        }

        @Test
        fun `loads empty form after init in create mode`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isEditing)
            assertEquals("", state.name)
            assertTrue(state.selectedMemberIds.isEmpty())
            assertEquals(3, state.availableMembers.size)

            collectJob.cancel()
        }

        @Test
        fun `UpdateName updates name and clears error`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.UpdateName("Family"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Family", state.name)
            assertNull(state.nameError)

            collectJob.cancel()
        }

        @Test
        fun `ToggleMember adds and removes members`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            // Add a member
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-1"))
            advanceUntilIdle()
            assertEquals(1, viewModel.uiState.value.selectedMemberIds.size)
            assertTrue(viewModel.uiState.value.selectedMemberIds.contains("user-1"))

            // Remove the member
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-1"))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.selectedMemberIds.isEmpty())

            collectJob.cancel()
        }

        @Test
        fun `ToggleMember distributes shares evenly`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            // Toggle two members on
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-1"))
            advanceUntilIdle()
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-2"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.selectedMemberIds.size)
            assertEquals("50", state.memberShares["user-1"])
            assertEquals("50", state.memberShares["user-2"])

            collectJob.cancel()
        }

        @Test
        fun `UpdateMemberShare redistributes remaining to other members`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            // Select two members
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-1"))
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-2"))
            advanceUntilIdle()

            // Update user-1's share to 60%
            viewModel.onEvent(CreateEditSubunitUiEvent.UpdateMemberShare("user-1", "60"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("60", state.memberShares["user-1"])
            assertEquals("40", state.memberShares["user-2"])

            collectJob.cancel()
        }

        @Test
        fun `Save shows name error when name is blank`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.Save)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.nameError)
            assertTrue(state.nameError is UiText.StringResource)
            assertEquals(
                R.string.subunit_error_name_empty,
                (state.nameError as UiText.StringResource).resId
            )

            collectJob.cancel()
        }

        @Test
        fun `Save shows members error when no members selected`() = runTest(testDispatcher) {
            setupDefaultMocks()
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.UpdateName("Family"))
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.Save)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.membersError)
            assertTrue(state.membersError is UiText.StringResource)
            assertEquals(
                R.string.subunit_error_no_members,
                (state.membersError as UiText.StringResource).resId
            )

            collectJob.cancel()
        }

        @Test
        fun `creates subunit and emits success then navigate back`() = runTest(testDispatcher) {
            setupDefaultMocks()
            coEvery { createSubunitUseCase(any(), any()) } returns Result.success("new-sub-id")
            createViewModel()

            val actions = mutableListOf<CreateEditSubunitUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.UpdateName("Family"))
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-3"))
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.Save)
            advanceUntilIdle()

            coVerify { createSubunitUseCase("group-1", any()) }
            assertTrue(actions.any { it is CreateEditSubunitUiAction.ShowSuccess })
            assertTrue(actions.any { it is CreateEditSubunitUiAction.NavigateBack })

            collectJob.cancel()
            actionsJob.cancel()
        }

        @Test
        fun `emits error action when save fails`() = runTest(testDispatcher) {
            setupDefaultMocks()
            coEvery { createSubunitUseCase(any(), any()) } returns Result.failure(Exception("Network error"))
            createViewModel()

            val actions = mutableListOf<CreateEditSubunitUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.init("group-1", null)
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.UpdateName("Family"))
            viewModel.onEvent(CreateEditSubunitUiEvent.ToggleMember("user-3"))
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.Save)
            advanceUntilIdle()

            assertTrue(actions.any { it is CreateEditSubunitUiAction.ShowError })
            assertFalse(actions.any { it is CreateEditSubunitUiAction.NavigateBack })

            collectJob.cancel()
            actionsJob.cancel()
        }
    }

    @Nested
    @DisplayName("Edit Mode")
    inner class EditMode {

        @Test
        fun `loads pre-filled form in edit mode`() = runTest(testDispatcher) {
            setupDefaultMocks(subunits = listOf(testSubunit))
            createViewModel()

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.init("group-1", "sub-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isEditing)
            assertEquals("Couple", state.name)
            assertEquals(2, state.selectedMemberIds.size)
            assertTrue(state.selectedMemberIds.contains("user-1"))
            assertTrue(state.selectedMemberIds.contains("user-2"))

            collectJob.cancel()
        }

        @Test
        fun `updates subunit and emits success then navigate back`() = runTest(testDispatcher) {
            setupDefaultMocks(subunits = listOf(testSubunit))
            coEvery { updateSubunitUseCase(any(), any()) } returns Result.success(Unit)
            createViewModel()

            val actions = mutableListOf<CreateEditSubunitUiAction>()
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.init("group-1", "sub-1")
            advanceUntilIdle()

            viewModel.onEvent(CreateEditSubunitUiEvent.Save)
            advanceUntilIdle()

            coVerify { updateSubunitUseCase("group-1", any()) }
            assertTrue(actions.any { it is CreateEditSubunitUiAction.ShowSuccess })
            assertTrue(actions.any { it is CreateEditSubunitUiAction.NavigateBack })

            collectJob.cancel()
            actionsJob.cancel()
        }
    }
}
