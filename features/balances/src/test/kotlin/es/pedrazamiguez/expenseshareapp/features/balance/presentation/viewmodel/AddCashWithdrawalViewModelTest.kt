package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalConfigHandler
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalCurrencyHandler
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalFeeHandler
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler.WithdrawalSubmitHandler
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.CashWithdrawalStep
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCashWithdrawalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var configHandler: WithdrawalConfigHandler
    private lateinit var currencyHandler: WithdrawalCurrencyHandler
    private lateinit var feeHandler: WithdrawalFeeHandler
    private lateinit var submitHandler: WithdrawalSubmitHandler
    private lateinit var viewModel: AddCashWithdrawalViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        configHandler = mockk(relaxed = true)
        currencyHandler = mockk(relaxed = true)
        feeHandler = mockk(relaxed = true)
        submitHandler = mockk(relaxed = true)
        viewModel = AddCashWithdrawalViewModel(
            configHandler = configHandler,
            currencyHandler = currencyHandler,
            feeHandler = feeHandler,
            submitHandler = submitHandler
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Handler Delegation ───────────────────────────────────────────────

    @Nested
    inner class HandlerDelegation {

        @Test
        fun `LoadGroupConfig delegates to configHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.LoadGroupConfig("group-1"))
            advanceUntilIdle()
            verify { configHandler.loadGroupConfig("group-1") }
        }

        @Test
        fun `RetryLoadConfig resets configLoadFailed and delegates with forceRefresh`() =
            runTest(testDispatcher) {
                viewModel.onEvent(AddCashWithdrawalUiEvent.RetryLoadConfig("group-1"))
                advanceUntilIdle()
                // ViewModel resets flag inline before delegating
                assertEquals(false, viewModel.uiState.value.configLoadFailed)
                assertNull(viewModel.uiState.value.error)
                verify { configHandler.loadGroupConfig("group-1", forceRefresh = true) }
            }

        @Test
        fun `CurrencySelected delegates to currencyHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.CurrencySelected("USD"))
            advanceUntilIdle()
            verify { currencyHandler.handleCurrencySelected("USD") }
        }

        @Test
        fun `WithdrawalAmountChanged delegates to currencyHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.WithdrawalAmountChanged("100.00"))
            advanceUntilIdle()
            verify { currencyHandler.handleWithdrawalAmountChanged("100.00") }
        }

        @Test
        fun `DeductedAmountChanged delegates to currencyHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.DeductedAmountChanged("27.00"))
            advanceUntilIdle()
            verify { currencyHandler.handleDeductedAmountChanged("27.00") }
        }

        @Test
        fun `ExchangeRateChanged delegates to currencyHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.ExchangeRateChanged("37.0"))
            advanceUntilIdle()
            verify { currencyHandler.handleExchangeRateChanged("37.0") }
        }

        @Test
        fun `FeeToggled delegates to feeHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.FeeToggled(true))
            advanceUntilIdle()
            verify { feeHandler.handleFeeToggled(true) }
        }

        @Test
        fun `FeeAmountChanged delegates to feeHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.FeeAmountChanged("5.00"))
            advanceUntilIdle()
            verify { feeHandler.handleFeeAmountChanged("5.00") }
        }

        @Test
        fun `FeeCurrencySelected delegates to feeHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.FeeCurrencySelected("THB"))
            advanceUntilIdle()
            verify { feeHandler.handleFeeCurrencySelected("THB") }
        }

        @Test
        fun `FeeExchangeRateChanged delegates to feeHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.FeeExchangeRateChanged("37.0"))
            advanceUntilIdle()
            verify { feeHandler.handleFeeExchangeRateChanged("37.0") }
        }

        @Test
        fun `FeeConvertedAmountChanged delegates to feeHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.FeeConvertedAmountChanged("2.70"))
            advanceUntilIdle()
            verify { feeHandler.handleFeeConvertedAmountChanged("2.70") }
        }

        @Test
        fun `SubmitWithdrawal delegates to submitHandler`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.SubmitWithdrawal("group-1"))
            advanceUntilIdle()
            verify { submitHandler.submitWithdrawal(eq("group-1"), any()) }
        }
    }

    // ── Inline State Updates ─────────────────────────────────────────────

    @Nested
    inner class InlineStateUpdates {

        @Test
        fun `WithdrawalScopeSelected sets scope to GROUP and clears subunitId`() =
            runTest(testDispatcher) {
                viewModel.onEvent(AddCashWithdrawalUiEvent.WithdrawalScopeSelected(PayerType.GROUP))
                advanceUntilIdle()
                assertEquals(PayerType.GROUP, viewModel.uiState.value.withdrawalScope)
                assertNull(viewModel.uiState.value.selectedSubunitId)
            }

        @Test
        fun `WithdrawalScopeSelected with SUBUNIT sets subunitId`() = runTest(testDispatcher) {
            viewModel.onEvent(
                AddCashWithdrawalUiEvent.WithdrawalScopeSelected(PayerType.SUBUNIT, subunitId = "sub-1")
            )
            advanceUntilIdle()
            assertEquals(PayerType.SUBUNIT, viewModel.uiState.value.withdrawalScope)
            assertEquals("sub-1", viewModel.uiState.value.selectedSubunitId)
        }

        @Test
        fun `TitleChanged updates title in state`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.TitleChanged("Airport ATM"))
            advanceUntilIdle()
            assertEquals("Airport ATM", viewModel.uiState.value.title)
        }

        @Test
        fun `NotesChanged updates notes in state`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.NotesChanged("Charged commission"))
            advanceUntilIdle()
            assertEquals("Charged commission", viewModel.uiState.value.notes)
        }
    }

    // ── Wizard Navigation ────────────────────────────────────────────────

    @Nested
    inner class WizardNavigation {

        @Test
        fun `initial state starts at AMOUNT step`() = runTest(testDispatcher) {
            assertEquals(CashWithdrawalStep.AMOUNT, viewModel.uiState.value.currentStep)
        }

        @Test
        fun `NextStep advances to the next applicable step`() = runTest(testDispatcher) {
            // Default state: [AMOUNT, SCOPE, DETAILS, REVIEW] — no exchange-rate or fee steps
            viewModel.onEvent(AddCashWithdrawalUiEvent.NextStep)
            advanceUntilIdle()
            assertEquals(CashWithdrawalStep.SCOPE, viewModel.uiState.value.currentStep)
        }

        @Test
        fun `NextStep does not advance past the last applicable step`() = runTest(testDispatcher) {
            val steps = viewModel.uiState.value.applicableSteps
            repeat(steps.lastIndex) {
                viewModel.onEvent(AddCashWithdrawalUiEvent.NextStep)
                advanceUntilIdle()
            }
            assertEquals(CashWithdrawalStep.REVIEW, viewModel.uiState.value.currentStep)

            // One extra NextStep on REVIEW — should stay at REVIEW
            viewModel.onEvent(AddCashWithdrawalUiEvent.NextStep)
            advanceUntilIdle()
            assertEquals(CashWithdrawalStep.REVIEW, viewModel.uiState.value.currentStep)
        }

        @Test
        fun `PreviousStep goes back to the previous applicable step`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.NextStep)
            advanceUntilIdle()
            assertEquals(CashWithdrawalStep.SCOPE, viewModel.uiState.value.currentStep)

            viewModel.onEvent(AddCashWithdrawalUiEvent.PreviousStep)
            advanceUntilIdle()
            assertEquals(CashWithdrawalStep.AMOUNT, viewModel.uiState.value.currentStep)
        }

        @Test
        fun `PreviousStep on first step emits NavigateBack action`() = runTest(testDispatcher) {
            assertEquals(CashWithdrawalStep.AMOUNT, viewModel.uiState.value.currentStep)

            val emittedActions = mutableListOf<AddCashWithdrawalUiAction>()
            val job = launch { viewModel.actions.collect { emittedActions.add(it) } }

            viewModel.onEvent(AddCashWithdrawalUiEvent.PreviousStep)
            advanceUntilIdle()

            assertTrue(emittedActions.any { it is AddCashWithdrawalUiAction.NavigateBack })
            job.cancel()
        }

        @Test
        fun `NextStep then PreviousStep returns to original step`() = runTest(testDispatcher) {
            viewModel.onEvent(AddCashWithdrawalUiEvent.NextStep)
            advanceUntilIdle()
            val stepAfterNext = viewModel.uiState.value.currentStep

            viewModel.onEvent(AddCashWithdrawalUiEvent.PreviousStep)
            advanceUntilIdle()

            assertEquals(CashWithdrawalStep.AMOUNT, viewModel.uiState.value.currentStep)
            assertEquals(CashWithdrawalStep.SCOPE, stepAfterNext)
        }
    }
}
