package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.domain.usecase.balance.DeleteCashWithdrawalUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DeleteContributionUseCase
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action.BalancesUiAction
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state.BalancesActivitySelectionState
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BalancesActivityEventHandlerImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var deleteContributionUseCase: DeleteContributionUseCase
    private lateinit var deleteCashWithdrawalUseCase: DeleteCashWithdrawalUseCase
    private lateinit var selectionState: MutableStateFlow<BalancesActivitySelectionState>
    private lateinit var actionsFlow: MutableSharedFlow<BalancesUiAction>
    private lateinit var handler: BalancesActivityEventHandlerImpl

    private val testGroupId = "group-123"

    private val testContribution = ContributionUiModel(
        id = "contrib-1",
        displayName = "Alice",
        formattedAmount = "€50.00"
    )

    private val testWithdrawal = CashWithdrawalUiModel(
        id = "withdrawal-1",
        displayName = "Bob",
        formattedAmount = "€100.00"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        deleteContributionUseCase = mockk()
        deleteCashWithdrawalUseCase = mockk()
        selectionState = MutableStateFlow(BalancesActivitySelectionState())
        actionsFlow = MutableSharedFlow(replay = 1)

        handler = BalancesActivityEventHandlerImpl(
            deleteContributionUseCase = deleteContributionUseCase,
            deleteCashWithdrawalUseCase = deleteCashWithdrawalUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Contribution delete flow ──────────────────────────────────────────────

    @Nested
    inner class ContributionDeleteFlow {

        @Test
        fun `handleDeleteContributionRequested sets contributionToDelete in state`() = runTest(testDispatcher) {
            // Given
            handler.bind(selectionState, actionsFlow, this)

            // When
            handler.handleDeleteContributionRequested(testContribution)

            // Then
            assertEquals(testContribution, selectionState.value.contributionToDelete)
        }

        @Test
        fun `handleDeleteContributionDismissed clears contributionToDelete`() = runTest(testDispatcher) {
            // Given
            handler.bind(selectionState, actionsFlow, this)
            selectionState.value = BalancesActivitySelectionState(contributionToDelete = testContribution)

            // When
            handler.handleDeleteContributionDismissed()

            // Then
            assertNull(selectionState.value.contributionToDelete)
        }

        @Test
        fun `handleDeleteContributionConfirmed clears state and calls use case`() = runTest(testDispatcher) {
            // Given
            coEvery {
                deleteContributionUseCase(testGroupId, testContribution.id)
            } just Runs
            handler.bind(selectionState, actionsFlow, this)
            selectionState.value = BalancesActivitySelectionState(contributionToDelete = testContribution)

            // When
            handler.handleDeleteContributionConfirmed(testGroupId, testContribution.id)
            advanceUntilIdle()

            // Then
            assertNull(selectionState.value.contributionToDelete)
            coVerify(exactly = 1) { deleteContributionUseCase(testGroupId, testContribution.id) }
        }

        @Test
        fun `handleDeleteContributionConfirmed emits success action on success`() = runTest(testDispatcher) {
            // Given
            coEvery {
                deleteContributionUseCase(testGroupId, testContribution.id)
            } just Runs
            handler.bind(selectionState, actionsFlow, this)
            val actions = mutableListOf<BalancesUiAction>()
            val collectJob = launch { actionsFlow.collect { actions.add(it) } }

            // When
            handler.handleDeleteContributionConfirmed(testGroupId, testContribution.id)
            advanceUntilIdle()

            // Then
            assertTrue(
                actions.any { it is BalancesUiAction.ShowDeleteContributionSuccess },
                "Expected ShowDeleteContributionSuccess action"
            )
            collectJob.cancel()
        }

        @Test
        fun `handleDeleteContributionConfirmed emits error action on failure`() = runTest(testDispatcher) {
            // Given
            coEvery {
                deleteContributionUseCase(testGroupId, testContribution.id)
            } throws RuntimeException("Network error")
            handler.bind(selectionState, actionsFlow, this)
            val actions = mutableListOf<BalancesUiAction>()
            val collectJob = launch { actionsFlow.collect { actions.add(it) } }

            // When
            handler.handleDeleteContributionConfirmed(testGroupId, testContribution.id)
            advanceUntilIdle()

            // Then
            assertTrue(
                actions.any { it is BalancesUiAction.ShowDeleteContributionError },
                "Expected ShowDeleteContributionError action"
            )
            collectJob.cancel()
        }
    }

    // ── Cash withdrawal delete flow ───────────────────────────────────────────

    @Nested
    inner class WithdrawalDeleteFlow {

        @Test
        fun `handleDeleteWithdrawalRequested sets withdrawalToDelete in state`() = runTest(testDispatcher) {
            // Given
            handler.bind(selectionState, actionsFlow, this)

            // When
            handler.handleDeleteWithdrawalRequested(testWithdrawal)

            // Then
            assertEquals(testWithdrawal, selectionState.value.withdrawalToDelete)
        }

        @Test
        fun `handleDeleteWithdrawalDismissed clears withdrawalToDelete`() = runTest(testDispatcher) {
            // Given
            handler.bind(selectionState, actionsFlow, this)
            selectionState.value = BalancesActivitySelectionState(withdrawalToDelete = testWithdrawal)

            // When
            handler.handleDeleteWithdrawalDismissed()

            // Then
            assertNull(selectionState.value.withdrawalToDelete)
        }

        @Test
        fun `handleDeleteWithdrawalConfirmed clears state and calls use case`() = runTest(testDispatcher) {
            // Given
            coEvery {
                deleteCashWithdrawalUseCase(testGroupId, testWithdrawal.id)
            } just Runs
            handler.bind(selectionState, actionsFlow, this)
            selectionState.value = BalancesActivitySelectionState(withdrawalToDelete = testWithdrawal)

            // When
            handler.handleDeleteWithdrawalConfirmed(testGroupId, testWithdrawal.id)
            advanceUntilIdle()

            // Then
            assertNull(selectionState.value.withdrawalToDelete)
            coVerify(exactly = 1) { deleteCashWithdrawalUseCase(testGroupId, testWithdrawal.id) }
        }

        @Test
        fun `handleDeleteWithdrawalConfirmed emits success action on success`() = runTest(testDispatcher) {
            // Given
            coEvery {
                deleteCashWithdrawalUseCase(testGroupId, testWithdrawal.id)
            } just Runs
            handler.bind(selectionState, actionsFlow, this)
            val actions = mutableListOf<BalancesUiAction>()
            val collectJob = launch { actionsFlow.collect { actions.add(it) } }

            // When
            handler.handleDeleteWithdrawalConfirmed(testGroupId, testWithdrawal.id)
            advanceUntilIdle()

            // Then
            assertTrue(
                actions.any { it is BalancesUiAction.ShowDeleteWithdrawalSuccess },
                "Expected ShowDeleteWithdrawalSuccess action"
            )
            collectJob.cancel()
        }

        @Test
        fun `handleDeleteWithdrawalConfirmed emits error action on failure`() = runTest(testDispatcher) {
            // Given
            coEvery {
                deleteCashWithdrawalUseCase(testGroupId, testWithdrawal.id)
            } throws RuntimeException("Network error")
            handler.bind(selectionState, actionsFlow, this)
            val actions = mutableListOf<BalancesUiAction>()
            val collectJob = launch { actionsFlow.collect { actions.add(it) } }

            // When
            handler.handleDeleteWithdrawalConfirmed(testGroupId, testWithdrawal.id)
            advanceUntilIdle()

            // Then
            assertTrue(
                actions.any { it is BalancesUiAction.ShowDeleteWithdrawalError },
                "Expected ShowDeleteWithdrawalError action"
            )
            collectJob.cancel()
        }
    }

    // ── State isolation ───────────────────────────────────────────────────────

    @Nested
    inner class StateIsolation {

        @Test
        fun `contribution request does not affect withdrawal state`() = runTest(testDispatcher) {
            // Given
            handler.bind(selectionState, actionsFlow, this)
            selectionState.value = BalancesActivitySelectionState(withdrawalToDelete = testWithdrawal)

            // When
            handler.handleDeleteContributionRequested(testContribution)

            // Then
            assertNotNull(selectionState.value.withdrawalToDelete)
            assertNotNull(selectionState.value.contributionToDelete)
        }

        @Test
        fun `contribution dismissed does not affect withdrawal state`() = runTest(testDispatcher) {
            // Given
            handler.bind(selectionState, actionsFlow, this)
            selectionState.value = BalancesActivitySelectionState(
                contributionToDelete = testContribution,
                withdrawalToDelete = testWithdrawal
            )

            // When
            handler.handleDeleteContributionDismissed()

            // Then
            assertNull(selectionState.value.contributionToDelete)
            assertNotNull(selectionState.value.withdrawalToDelete)
        }
    }
}
