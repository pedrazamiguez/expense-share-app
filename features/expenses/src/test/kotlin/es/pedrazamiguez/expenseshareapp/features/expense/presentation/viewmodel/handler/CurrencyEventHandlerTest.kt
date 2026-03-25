package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.model.CashRatePreview
import es.pedrazamiguez.expenseshareapp.domain.model.CashRatePreviewResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.PreviewCashExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseOptionsUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.jupiter.api.Assertions.assertNull
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
    private lateinit var exchangeRateCalculationService: ExchangeRateCalculationService

    private lateinit var uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var actions: MutableSharedFlow<AddExpenseUiAction>

    private val eurCurrency = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)
    private val thbCurrency = CurrencyUiModel(code = "THB", displayText = "THB (฿)", decimalDigits = 2)

    private val cashPaymentMethod = PaymentMethodUiModel(id = "CASH", displayText = "Cash")
    private val debitCardPaymentMethod = PaymentMethodUiModel(id = "DEBIT_CARD", displayText = "Debit Card")

    /** Initial state simulating a CASH + foreign-currency scenario. */
    private val cashForeignState = AddExpenseUiState(
        loadedGroupId = "group-1",
        groupCurrency = eurCurrency,
        selectedCurrency = thbCurrency,
        selectedPaymentMethod = cashPaymentMethod,
        showExchangeRateSection = true,
        isExchangeRateLocked = true
    )

    /** Initial state simulating a non-CASH + foreign-currency scenario with a custom rate. */
    private val nonCashForeignState = AddExpenseUiState(
        loadedGroupId = "group-1",
        groupCurrency = eurCurrency,
        selectedCurrency = thbCurrency,
        selectedPaymentMethod = debitCardPaymentMethod,
        showExchangeRateSection = true,
        isExchangeRateLocked = false,
        displayExchangeRate = "35.5",
        sourceAmount = "1000"
    )

    @BeforeEach
    fun setUp() {
        previewCashExchangeRateUseCase = mockk()
        getExchangeRateUseCase = mockk(relaxed = true)
        expenseCalculatorService = ExpenseCalculatorService()
        exchangeRateCalculationService = ExchangeRateCalculationService()

        val localeProvider = mockk<LocaleProvider>()
        val resourceProvider = mockk<ResourceProvider>(relaxed = true)
        every { localeProvider.getCurrentLocale() } returns Locale.US

        val formattingHelper = FormattingHelper(localeProvider)
        val splitPreviewService = SplitPreviewService()

        handler = CurrencyEventHandler(
            getExchangeRateUseCase = getExchangeRateUseCase,
            previewCashExchangeRateUseCase = previewCashExchangeRateUseCase,
            exchangeRateCalculationService = exchangeRateCalculationService,
            expenseCalculatorService = expenseCalculatorService,
            splitPreviewService = splitPreviewService,
            formattingHelper = formattingHelper,
            addExpenseOptionsMapper = AddExpenseOptionsUiMapper(resourceProvider)
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

    // ── handlePaymentMethodChanged ────────────────────────────────────────────

    @Nested
    inner class PaymentMethodChanged {

        @Test
        fun `switching between non-CASH methods preserves custom exchange rate`() = runTest {
            // Given: user has a custom rate of "35.5" on DEBIT_CARD
            uiState.value = nonCashForeignState
            handler.bind(uiState, actions, this)

            // When: user switches to another non-CASH method (e.g. PAYPAL)
            handler.handlePaymentMethodChanged(isCash = false)
            advanceUntilIdle()

            // Then: rate is unchanged, no API call
            val state = uiState.value
            assertEquals("35.5", state.displayExchangeRate)
            assertFalse(state.isExchangeRateLocked)
            coVerify(exactly = 0) { getExchangeRateUseCase(any(), any()) }
        }

        @Test
        fun `switching to CASH saves current rate and locks exchange rate`() = runTest {
            // Given: user has a custom rate of "35.5"
            uiState.value = nonCashForeignState
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.Available(
                CashRatePreview(
                    displayRate = BigDecimal("37.000000"),
                    groupAmountCents = 2703L
                )
            )
            handler.bind(uiState, actions, this)

            // When: user switches to CASH
            handler.handlePaymentMethodChanged(isCash = true)
            advanceUntilIdle()

            // Then: the custom rate is saved and the cash rate is applied
            val state = uiState.value
            assertEquals("35.5", state.preCashExchangeRate)
            assertTrue(state.isExchangeRateLocked)
            // Cash rate overwrites displayExchangeRate
            assertEquals("37", state.displayExchangeRate)
        }

        @Test
        fun `switching from CASH to non-CASH restores saved rate`() = runTest {
            // Given: user was on CASH with a saved pre-cash rate
            uiState.value = cashForeignState.copy(
                displayExchangeRate = "37.000000",
                preCashExchangeRate = "35.5",
                sourceAmount = "1000"
            )
            handler.bind(uiState, actions, this)

            // When: user switches back to non-CASH
            handler.handlePaymentMethodChanged(isCash = false)
            advanceUntilIdle()

            // Then: the saved rate is restored, no API call
            val state = uiState.value
            assertEquals("35.5", state.displayExchangeRate)
            assertFalse(state.isExchangeRateLocked)
            assertNull(state.preCashExchangeRate)
            coVerify(exactly = 0) { getExchangeRateUseCase(any(), any()) }
        }

        @Test
        fun `switching from CASH to non-CASH fetches API rate when no saved rate exists`() = runTest {
            // Given: user was on CASH but currency changed while on CASH (no saved rate)
            uiState.value = cashForeignState.copy(
                displayExchangeRate = "37.000000",
                preCashExchangeRate = null,
                sourceAmount = "1000"
            )
            coEvery {
                getExchangeRateUseCase(any(), any())
            } returns BigDecimal("36.8")
            handler.bind(uiState, actions, this)

            // When: user switches back to non-CASH
            handler.handlePaymentMethodChanged(isCash = false)
            advanceUntilIdle()

            // Then: API rate is fetched as fallback
            val state = uiState.value
            assertFalse(state.isExchangeRateLocked)
            coVerify(exactly = 1) { getExchangeRateUseCase(any(), any()) }
        }

        @Test
        fun `round trip CASH and back preserves original custom rate`() = runTest {
            // Given: user has a custom rate of "35.5" on DEBIT_CARD
            uiState.value = nonCashForeignState
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } returns CashRatePreviewResult.Available(
                CashRatePreview(
                    displayRate = BigDecimal("37.000000"),
                    groupAmountCents = 2703L
                )
            )
            handler.bind(uiState, actions, this)

            // When: user switches to CASH, then back to non-CASH
            handler.handlePaymentMethodChanged(isCash = true)
            advanceUntilIdle()
            handler.handlePaymentMethodChanged(isCash = false)
            advanceUntilIdle()

            // Then: original custom rate is restored
            val state = uiState.value
            assertEquals("35.5", state.displayExchangeRate)
            assertFalse(state.isExchangeRateLocked)
            assertNull(state.preCashExchangeRate)
            // No API call should have been made
            coVerify(exactly = 0) { getExchangeRateUseCase(any(), any()) }
        }

        @Test
        fun `switching to non-CASH with same currency does not fetch rate`() = runTest {
            // Given: same currency (not foreign)
            uiState.value = AddExpenseUiState(
                loadedGroupId = "group-1",
                groupCurrency = eurCurrency,
                selectedCurrency = eurCurrency,
                selectedPaymentMethod = cashPaymentMethod,
                isExchangeRateLocked = true
            )
            handler.bind(uiState, actions, this)

            // When
            handler.handlePaymentMethodChanged(isCash = false)
            advanceUntilIdle()

            // Then: no rate fetch, just unlock
            val state = uiState.value
            assertFalse(state.isExchangeRateLocked)
            coVerify(exactly = 0) { getExchangeRateUseCase(any(), any()) }
        }

        @Test
        fun `in-flight cash rate job does not overwrite restored rate after switching away`() = runTest {
            // Given: user is on non-CASH with custom rate "35.5"
            uiState.value = nonCashForeignState
            // Simulate a slow CASH rate response
            coEvery {
                previewCashExchangeRateUseCase(any(), any(), any())
            } coAnswers {
                kotlinx.coroutines.delay(500L) // slow response
                CashRatePreviewResult.Available(
                    CashRatePreview(
                        displayRate = BigDecimal("37.000000"),
                        groupAmountCents = 2703L
                    )
                )
            }
            handler.bind(uiState, actions, this)

            // When: user switches to CASH (triggers async fetchCashRate)
            handler.handlePaymentMethodChanged(isCash = true)
            // Then immediately switches back to non-CASH before cash rate arrives
            handler.handlePaymentMethodChanged(isCash = false)
            // Now let all pending coroutines complete
            advanceUntilIdle()

            // Then: the restored custom rate must survive — the cancelled cash job
            // must NOT overwrite it
            val state = uiState.value
            assertEquals("35.5", state.displayExchangeRate)
            assertFalse(state.isExchangeRateLocked)
        }
    }
}
