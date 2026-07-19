package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DisputeSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ArchiveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ObserveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.GroupSettlementOverviewUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.archive.ArchiveWizardStep
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action.GroupSettlementOverviewUiAction
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.event.GroupSettlementOverviewUiEvent
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupSettlementOverviewUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupSettlementOverviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase
    private lateinit var getMemberProfilesUseCase: GetMemberProfilesUseCase
    private lateinit var observeGroupUseCase: ObserveGroupUseCase
    private lateinit var groupSettlementOverviewUiMapper: GroupSettlementOverviewUiMapper
    private lateinit var authenticationService: AuthenticationService
    private lateinit var confirmSettlementUseCase: ConfirmSettlementUseCase
    private lateinit var disputeSettlementUseCase: DisputeSettlementUseCase
    private lateinit var archiveGroupUseCase: ArchiveGroupUseCase
    private lateinit var getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase
    private lateinit var viewModel: GroupSettlementOverviewViewModel

    private val testGroupId = "group-123"
    private val testGroup = Group(
        id = testGroupId,
        name = "Summer Trip",
        currency = "EUR",
        members = listOf("user-1", "user-2", "user-3")
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getGroupSettlementsFlowUseCase = mockk()
        getMemberProfilesUseCase = mockk()
        observeGroupUseCase = mockk()
        groupSettlementOverviewUiMapper = mockk()
        authenticationService = mockk(relaxed = true)
        confirmSettlementUseCase = mockk(relaxed = true)
        disputeSettlementUseCase = mockk(relaxed = true)
        archiveGroupUseCase = mockk(relaxed = true)
        getSettlementSuggestionsUseCase = mockk(relaxed = true)

        coEvery { getSettlementSuggestionsUseCase.persistForGroup(any(), any()) } returns emptyList()
        every { getGroupSettlementsFlowUseCase(any()) } returns flowOf(emptyList())
        every { observeGroupUseCase(any()) } returns flowOf(testGroup)
        every { groupSettlementOverviewUiMapper.toUiState(any(), any(), any(), any(), any()) } returns
            GroupSettlementOverviewUiState(
                isLoading = false,
                areAllSettlementsResolved = true
            )
        every { authenticationService.requireUserId() } returns "user-1"

        viewModel = createViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = GroupSettlementOverviewViewModel(
        getGroupSettlementsFlowUseCase = getGroupSettlementsFlowUseCase,
        getMemberProfilesUseCase = getMemberProfilesUseCase,
        observeGroupUseCase = observeGroupUseCase,
        groupSettlementOverviewUiMapper = groupSettlementOverviewUiMapper,
        authenticationService = authenticationService,
        confirmSettlementUseCase = confirmSettlementUseCase,
        disputeSettlementUseCase = disputeSettlementUseCase,
        archiveGroupUseCase = archiveGroupUseCase,
        getSettlementSuggestionsUseCase = getSettlementSuggestionsUseCase
    )

    @Nested
    inner class InitialState {

        @Test
        fun `initial state is loading`() = runTest(testDispatcher) {
            assertTrue(viewModel.uiState.value.isLoading)
        }
    }

    @Nested
    inner class SetGroupId {

        @Test
        fun `blank group id does not trigger load`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.setGroupId("")
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isLoading)

            collectJob.cancel()
        }

        @Test
        fun `valid group id triggers state load`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)

            collectJob.cancel()
        }

        @Test
        fun `setGroupId calls persistForGroup to load fresh suggestions`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            coVerify(exactly = 1) { getSettlementSuggestionsUseCase.persistForGroup(testGroupId) }

            collectJob.cancel()
        }
    }

    @Nested
    inner class ConfirmFlow {

        @Test
        fun `ConfirmSettlement event calls confirmSettlementUseCase and emits ShowSuccess on success`() = runTest(
            testDispatcher
        ) {
            coEvery { confirmSettlementUseCase(testGroupId, "s-1") } returns Result.success(
                createRecord("s-1", SettlementStatus.CONFIRMED_BY_PAYER)
            )

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            val actions = mutableListOf<GroupSettlementOverviewUiAction>()
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.onEvent(GroupSettlementOverviewUiEvent.ConfirmSettlement("s-1"))
            advanceUntilIdle()

            coVerify(exactly = 1) { confirmSettlementUseCase(testGroupId, "s-1") }
            assertTrue(actions.any { it is GroupSettlementOverviewUiAction.ShowSuccess })

            actionsJob.cancel()
            collectJob.cancel()
        }

        @Test
        fun `ConfirmSettlement event emits ShowError on failure`() = runTest(testDispatcher) {
            coEvery { confirmSettlementUseCase(testGroupId, "s-1") } returns Result.failure(Exception("Confirm failed"))

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            val actions = mutableListOf<GroupSettlementOverviewUiAction>()
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.onEvent(GroupSettlementOverviewUiEvent.ConfirmSettlement("s-1"))
            advanceUntilIdle()

            assertTrue(actions.any { it is GroupSettlementOverviewUiAction.ShowError })

            actionsJob.cancel()
            collectJob.cancel()
        }
    }

    @Nested
    inner class DisputeFlow {

        @Test
        fun `DisputeSettlement event opens dispute dialog`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeSettlement("s-1"))
            advanceUntilIdle()

            assertEquals("s-1", viewModel.uiState.value.activeDisputeSettlementId)

            collectJob.cancel()
        }

        @Test
        fun `DisputeReasonChanged updates reason input`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeSettlement("s-1"))
            advanceUntilIdle()

            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeReasonChanged("Amount is wrong"))
            advanceUntilIdle()

            assertEquals("Amount is wrong", viewModel.uiState.value.disputeReasonInput)

            collectJob.cancel()
        }

        @Test
        fun `DisputeSubmitted calls disputeSettlementUseCase and emits ShowSuccess`() = runTest(testDispatcher) {
            coEvery { disputeSettlementUseCase(testGroupId, "s-1", "Amount is wrong") } returns Result.success(
                createRecord("s-1", SettlementStatus.DISPUTED)
            )

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            val actions = mutableListOf<GroupSettlementOverviewUiAction>()
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeSettlement("s-1"))
            advanceUntilIdle()
            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeReasonChanged("Amount is wrong"))
            advanceUntilIdle()
            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeSubmitted)
            advanceUntilIdle()

            coVerify(exactly = 1) { disputeSettlementUseCase(testGroupId, "s-1", "Amount is wrong") }
            assertTrue(actions.any { it is GroupSettlementOverviewUiAction.ShowSuccess })
            assertNull(viewModel.uiState.value.activeDisputeSettlementId)

            actionsJob.cancel()
            collectJob.cancel()
        }

        @Test
        fun `DisputeSubmitted with blank reason does nothing`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeSettlement("s-1"))
            advanceUntilIdle()
            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeSubmitted)
            advanceUntilIdle()

            coVerify(exactly = 0) { disputeSettlementUseCase(any(), any(), any()) }

            collectJob.cancel()
        }

        @Test
        fun `DisputeCancelled clears dispute state`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeSettlement("s-1"))
            advanceUntilIdle()
            viewModel.onEvent(GroupSettlementOverviewUiEvent.DisputeCancelled)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.activeDisputeSettlementId)
            assertEquals("", viewModel.uiState.value.disputeReasonInput)

            collectJob.cancel()
        }
    }

    @Nested
    inner class ArchiveFlow {

        @Test
        fun `CloseTripClicked with success emits NavigateBack action`() = runTest(testDispatcher) {
            coEvery { archiveGroupUseCase(testGroupId) } returns Result.success(Unit)

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            val actions = mutableListOf<GroupSettlementOverviewUiAction>()
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.onEvent(GroupSettlementOverviewUiEvent.CloseTripClicked)
            advanceUntilIdle()

            coVerify(exactly = 1) { archiveGroupUseCase(testGroupId) }
            assertTrue(actions.any { it is GroupSettlementOverviewUiAction.NavigateBack })
            assertFalse(viewModel.uiState.value.isArchiving)

            actionsJob.cancel()
            collectJob.cancel()
        }

        @Test
        fun `CloseTripClicked with failure emits ShowError action`() = runTest(testDispatcher) {
            coEvery { archiveGroupUseCase(testGroupId) } returns Result.failure(Exception("Archive failed"))

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            val actions = mutableListOf<GroupSettlementOverviewUiAction>()
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.onEvent(GroupSettlementOverviewUiEvent.CloseTripClicked)
            advanceUntilIdle()

            assertTrue(actions.any { it is GroupSettlementOverviewUiAction.ShowError })
            assertFalse(viewModel.uiState.value.isArchiving)

            actionsJob.cancel()
            collectJob.cancel()
        }
    }

    private fun createRecord(
        id: String,
        status: SettlementStatus,
        fromUser: String = "user-1",
        toUser: String = "user-2",
        amount: Long = 10000L
    ): SettlementRecord = SettlementRecord(
        id = id,
        groupId = testGroupId,
        settlement = Settlement(
            fromUserId = fromUser,
            toUserId = toUser,
            amount = amount,
            currency = "EUR",
            sourcePocket = SettlementPocketType.NET
        ),
        status = status,
        createdAt = LocalDateTime.now()
    )

    @Nested
    inner class WizardFlow {

        @Test
        fun `WizardNextClicked and WizardBackClicked update currentStep state`() = runTest(testDispatcher) {
            every { groupSettlementOverviewUiMapper.toUiState(any(), any(), any(), any(), any()) } returns
                GroupSettlementOverviewUiState(
                    isLoading = false,
                    currentStep = ArchiveWizardStep.SETTLEMENT_SUMMARY,
                    activeSteps = kotlinx.collections.immutable.persistentListOf(
                        ArchiveWizardStep.SETTLEMENT_SUMMARY,
                        ArchiveWizardStep.ACTION_REQUIRED,
                        ArchiveWizardStep.CONFIRMATION
                    )
                )

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            // Verify initial step
            assertEquals(ArchiveWizardStep.SETTLEMENT_SUMMARY, viewModel.uiState.value.currentStep)

            // Step forward
            viewModel.onEvent(GroupSettlementOverviewUiEvent.WizardNextClicked)
            advanceUntilIdle()
            assertEquals(ArchiveWizardStep.ACTION_REQUIRED, viewModel.uiState.value.currentStep)

            // Step forward again
            viewModel.onEvent(GroupSettlementOverviewUiEvent.WizardNextClicked)
            advanceUntilIdle()
            assertEquals(ArchiveWizardStep.CONFIRMATION, viewModel.uiState.value.currentStep)

            // Step backward
            viewModel.onEvent(GroupSettlementOverviewUiEvent.WizardBackClicked)
            advanceUntilIdle()
            assertEquals(ArchiveWizardStep.ACTION_REQUIRED, viewModel.uiState.value.currentStep)

            collectJob.cancel()
        }

        @Test
        fun `WizardCancelled or Back on first step navigates back`() = runTest(testDispatcher) {
            every { groupSettlementOverviewUiMapper.toUiState(any(), any(), any(), any(), any()) } returns
                GroupSettlementOverviewUiState(
                    isLoading = false,
                    currentStep = ArchiveWizardStep.SETTLEMENT_SUMMARY,
                    activeSteps = kotlinx.collections.immutable.persistentListOf(
                        ArchiveWizardStep.SETTLEMENT_SUMMARY,
                        ArchiveWizardStep.ACTION_REQUIRED,
                        ArchiveWizardStep.CONFIRMATION
                    )
                )

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            val actions = mutableListOf<GroupSettlementOverviewUiAction>()
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            // Wizard cancelled
            viewModel.onEvent(GroupSettlementOverviewUiEvent.WizardCancelled)
            advanceUntilIdle()
            assertTrue(actions.any { it is GroupSettlementOverviewUiAction.NavigateBack })

            actions.clear()

            // Back on first step
            viewModel.onEvent(GroupSettlementOverviewUiEvent.WizardBackClicked)
            advanceUntilIdle()
            assertTrue(actions.any { it is GroupSettlementOverviewUiAction.NavigateBack })

            actionsJob.cancel()
            collectJob.cancel()
        }

        @Test
        fun `WizardJumpToStep updates currentStep if step is active`() = runTest(testDispatcher) {
            every { groupSettlementOverviewUiMapper.toUiState(any(), any(), any(), any(), any()) } returns
                GroupSettlementOverviewUiState(
                    isLoading = false,
                    currentStep = ArchiveWizardStep.SETTLEMENT_SUMMARY,
                    activeSteps = kotlinx.collections.immutable.persistentListOf(
                        ArchiveWizardStep.SETTLEMENT_SUMMARY,
                        ArchiveWizardStep.ACTION_REQUIRED,
                        ArchiveWizardStep.CONFIRMATION
                    )
                )

            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setGroupId(testGroupId)
            advanceUntilIdle()

            viewModel.onEvent(GroupSettlementOverviewUiEvent.WizardJumpToStep(ArchiveWizardStep.CONFIRMATION))
            advanceUntilIdle()
            assertEquals(ArchiveWizardStep.CONFIRMATION, viewModel.uiState.value.currentStep)

            viewModel.onEvent(GroupSettlementOverviewUiEvent.WizardJumpToStep(ArchiveWizardStep.ACTION_REQUIRED))
            advanceUntilIdle()
            assertEquals(ArchiveWizardStep.ACTION_REQUIRED, viewModel.uiState.value.currentStep)

            collectJob.cancel()
        }

        @Test
        fun `currentStep is clamped to last active step when real-time update removes current step from activeSteps`() =
            runTest(testDispatcher) {
                // Simulate the 3-step flow (pending settlements exist)
                val threeStepState = GroupSettlementOverviewUiState(
                    isLoading = false,
                    currentStep = ArchiveWizardStep.SETTLEMENT_SUMMARY,
                    activeSteps = kotlinx.collections.immutable.persistentListOf(
                        ArchiveWizardStep.SETTLEMENT_SUMMARY,
                        ArchiveWizardStep.ACTION_REQUIRED,
                        ArchiveWizardStep.CONFIRMATION
                    )
                )
                // Simulate the streamlined flow after peer resolves all settlements in real-time
                val twoStepState = GroupSettlementOverviewUiState(
                    isLoading = false,
                    currentStep = ArchiveWizardStep.SETTLEMENT_SUMMARY,
                    activeSteps = kotlinx.collections.immutable.persistentListOf(
                        ArchiveWizardStep.SETTLEMENT_SUMMARY,
                        ArchiveWizardStep.CONFIRMATION
                    ),
                    areAllSettlementsResolved = true
                )

                // Use a replay=1 SharedFlow so the first emission is not lost before subscription
                val settlementsFlow = kotlinx.coroutines.flow.MutableSharedFlow<List<Nothing>>(replay = 1)

                // Reconfigure mocks BEFORE creating a new ViewModel so flatMapLatest picks up the
                // controlled flow on the very first setGroupId() call
                every { getGroupSettlementsFlowUseCase(any()) } returns settlementsFlow
                var callCount = 0
                every { groupSettlementOverviewUiMapper.toUiState(any(), any(), any(), any(), any()) } answers {
                    if (callCount++ == 0) threeStepState else twoStepState
                }
                val vm = createViewModel()

                val collectJob = backgroundScope.launch { vm.uiState.collect {} }
                vm.setGroupId(testGroupId)

                // Emit first batch (pending settlements → 3-step flow)
                settlementsFlow.emit(emptyList())
                advanceUntilIdle()
                assertEquals(ArchiveWizardStep.SETTLEMENT_SUMMARY, vm.uiState.value.currentStep)

                // User navigates to ACTION_REQUIRED
                vm.onEvent(GroupSettlementOverviewUiEvent.WizardNextClicked)
                advanceUntilIdle()
                assertEquals(ArchiveWizardStep.ACTION_REQUIRED, vm.uiState.value.currentStep)

                // Peer resolves settlement in real-time → mapper switches to 2-step flow
                settlementsFlow.emit(emptyList())
                advanceUntilIdle()

                // ACTION_REQUIRED no longer in activeSteps; step must be clamped to CONFIRMATION
                assertEquals(ArchiveWizardStep.CONFIRMATION, vm.uiState.value.currentStep)

                // Back must now work (not silently no-op)
                vm.onEvent(GroupSettlementOverviewUiEvent.WizardBackClicked)
                advanceUntilIdle()
                assertEquals(ArchiveWizardStep.SETTLEMENT_SUMMARY, vm.uiState.value.currentStep)

                collectJob.cancel()
            }
    }
}
