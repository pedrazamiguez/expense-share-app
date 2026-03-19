package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.model.CashRatePreview
import es.pedrazamiguez.expenseshareapp.domain.model.CashRatePreviewResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.PreviewCashExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyEventHandlerTest {

    private lateinit var handler: CurrencyEventHandler
    private lateinit var previewCashExchangeRateUseCase: PreviewCashExchangeRateUseCase
    private lateinit var getExchangeRateUseCase: GetExchangeRateUseCase
    private lateinit var expenseCalculatorService: ExpenseCalculatorService

    private lateinit var uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var actions: MutableSharedFlow<AddExpenseUiAction>

    private val eurCurrency = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)
    private val thbCurrency = CurrencyUiModel(code = "THB", displayText = "THB (฿)", decimalDigits = 2)

    private val cashPaymentMethod = PaymentMethodUiModel(id = "CASH", displayText = "Cash")

    /** Initial state simulating a CASH + foreign-currency scenario. */
    private val cashForeignState = AddExpenseUiState(
        loadedGroupId = "group-1",
        groupCurrency = eurCurrency,
        selectedCurrency = thbCurrency,
        selectedPaymentMethod = cashPaymentMethod,
        showExchangeRateSection = true,
        isExchangeRateLocked = true
    )

    @BeforeEach
    fun setUp() {
        previewCashExchangeRateUseCase = mockk()
        getExchangeRateUseCase = mockk(relaxed = true)
        expenseCalculatorService = ExpenseCalculatorService()

        val localeProvider = mockk<LocaleProvider>()
        val resourceProvider = mockk<ResourceProvider>(relaxed = true)
        every { localeProvider.getCurrentLocale() } returns Locale.US

        handler = CurrencyEventHandler(
            getExchangeRateUseCase = getExchangeRateUseCase,
            previewCashExchangeRateUseCase = previewCashExchangeRateUseCase,
            expenseCalculatorService = expenseCalculatorService,
            addExpenseUiMapper = AddExpenseUiMapper(localeProvider, resourceProvider)
        )

        uiState = MutableStateFlow(cashForeignState)
        actions = MutableSharedFlow()
    }

    // ── InsufficientCash ────────────────────────────────────────────────────

    @Nested
    inner class InsufficientCash {

        @Test
        fun `sets placeholder and warning hint when cash is insufficient`() = runTest {
            // Given: user typed 25225 THB but not enough cash
            uiState.value = cashForeignState.copy(sourceAmount = "25225")
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.InsufficientCash

            handler.bind(uiState, actions, this)

            // When
            handler.fetchCashRate()
            advanceUntilIdle()

            // Then
            val state = uiState.value
            assertEquals("—", state.displayExchangeRate)
            assertEquals("—", state.calculatedGroupAmount)
            assertTrue(state.isInsufficientCash)
            assertTrue(state.isExchangeRateLocked)
            assertEquals(
                UiText.StringResource(R.string.add_expense_cash_insufficient_hint),
                state.exchangeRateLockedHint
            )
        }

        @Test
        fun `clears insufficient cash when valid amount is entered after overshoot`() = runTest {
            // Given: previously insufficient
            uiState.value = cashForeignState.copy(
                sourceAmount = "100",
                displayExchangeRate = "—",
                calculatedGroupAmount = "—",
                isInsufficientCash = true
            )
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.Available(
                CashRatePreview(
                    displayRate = BigDecimal("37.000000"),
                    groupAmountCents = 270L
                )
            )

            handler.bind(uiState, actions, this)

            // When
            handler.fetchCashRate()
            advanceUntilIdle()

            // Then
            val state = uiState.value
            assertFalse(state.isInsufficientCash)
            assertEquals("37", state.displayExchangeRate)
            assertEquals("2.70", state.calculatedGroupAmount)
            assertEquals(
                UiText.StringResource(R.string.add_expense_cash_rate_locked_hint),
                state.exchangeRateLockedHint
            )
        }
    }

    // ── NoWithdrawals ───────────────────────────────────────────────────────

    @Nested
    inner class NoWithdrawals {

        @Test
        fun `sets placeholder and generic hint when no withdrawals exist`() = runTest {
            uiState.value = cashForeignState.copy(sourceAmount = "100")
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.NoWithdrawals

            handler.bind(uiState, actions, this)

            // When
            handler.fetchCashRate()
            advanceUntilIdle()

            // Then
            val state = uiState.value
            assertEquals("—", state.displayExchangeRate)
            assertEquals("—", state.calculatedGroupAmount)
            assertFalse(state.isInsufficientCash)
            assertTrue(state.isExchangeRateLocked)
            assertEquals(
                UiText.StringResource(R.string.add_expense_cash_rate_locked_hint),
                state.exchangeRateLockedHint
            )
        }
    }

    // ── Available (FIFO) ────────────────────────────────────────────────────

    @Nested
    inner class Available {

        @Test
        fun `sets formatted rate and group amount for FIFO result`() = runTest {
            uiState.value = cashForeignState.copy(sourceAmount = "500")
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.Available(
                CashRatePreview(
                    displayRate = BigDecimal("37.037037"),
                    groupAmountCents = 1350L
                )
            )

            handler.bind(uiState, actions, this)

            // When
            handler.fetchCashRate()
            advanceUntilIdle()

            // Then
            val state = uiState.value
            assertFalse(state.isInsufficientCash)
            assertEquals("37.037037", state.displayExchangeRate)
            assertEquals("13.50", state.calculatedGroupAmount)
            assertTrue(state.isExchangeRateLocked)
        }

        @Test
        fun `sets weighted-average rate without group amount when no amount entered`() = runTest {
            // sourceAmount is empty — preview returns weighted avg (groupAmountCents = 0)
            uiState.value = cashForeignState.copy(sourceAmount = "")
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.Available(
                CashRatePreview(
                    displayRate = BigDecimal("36.855037"),
                    groupAmountCents = 0L
                )
            )

            handler.bind(uiState, actions, this)

            // When
            handler.fetchCashRate()
            advanceUntilIdle()

            // Then
            val state = uiState.value
            assertFalse(state.isInsufficientCash)
            assertEquals("36.855037", state.displayExchangeRate)
            assertEquals("", state.calculatedGroupAmount)
            assertTrue(state.isExchangeRateLocked)
        }

        @Test
        fun `clears stale group amount when switching from FIFO to weighted-average`() = runTest {
            // Given: user previously had a FIFO-simulated result with a calculated group amount
            uiState.value = cashForeignState.copy(
                sourceAmount = "",
                calculatedGroupAmount = "13.50",
                displayExchangeRate = "37.037037"
            )
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.Available(
                CashRatePreview(
                    displayRate = BigDecimal("36.855037"),
                    groupAmountCents = 0L
                )
            )

            handler.bind(uiState, actions, this)

            // When: source amount is now empty, so weighted-average preview fires
            handler.fetchCashRate()
            advanceUntilIdle()

            // Then: stale "13.50" must be cleared
            val state = uiState.value
            assertFalse(state.isInsufficientCash)
            assertEquals("36.855037", state.displayExchangeRate)
            assertEquals("", state.calculatedGroupAmount)
            assertTrue(state.isExchangeRateLocked)
        }
    }
}
