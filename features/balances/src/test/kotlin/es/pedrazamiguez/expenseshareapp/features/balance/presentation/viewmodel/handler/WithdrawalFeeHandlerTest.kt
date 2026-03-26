package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
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
class WithdrawalFeeHandlerTest {

    private lateinit var handler: WithdrawalFeeHandler
    private lateinit var getExchangeRateUseCase: GetExchangeRateUseCase
    private lateinit var exchangeRateCalculationService: ExchangeRateCalculationService
    private lateinit var addCashWithdrawalUiMapper: AddCashWithdrawalUiMapper
    private lateinit var formattingHelper: FormattingHelper

    private lateinit var uiState: MutableStateFlow<AddCashWithdrawalUiState>
    private lateinit var actions: MutableSharedFlow<AddCashWithdrawalUiAction>

    private val eurModel = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)
    private val thbModel = CurrencyUiModel(code = "THB", displayText = "THB (฿)", decimalDigits = 2)

    private val baseState = AddCashWithdrawalUiState(
        isConfigLoaded = true,
        groupCurrency = eurModel,
        selectedCurrency = eurModel,
        availableCurrencies = listOf(eurModel, thbModel).toImmutableList()
    )

    @BeforeEach
    fun setUp() {
        getExchangeRateUseCase = mockk(relaxed = true)
        exchangeRateCalculationService = mockk(relaxed = true)
        addCashWithdrawalUiMapper = mockk(relaxed = true)
        formattingHelper = mockk(relaxed = true)

        every {
            exchangeRateCalculationService.calculateGroupAmountFromDisplayRate(any(), any(), any(), any())
        } returns "2.70"
        every {
            exchangeRateCalculationService.calculateImpliedDisplayRateFromStrings(any(), any(), any())
        } returns "37.0"
        every { formattingHelper.formatForDisplay(any(), any(), any()) } returns "2.70"
        every { formattingHelper.formatRateForDisplay(any()) } returns "37.0"
        every { addCashWithdrawalUiMapper.buildFeeConvertedLabel(any()) } returns "Converted (EUR)"
        every { addCashWithdrawalUiMapper.buildExchangeRateLabel(any(), any()) } returns "1 EUR = X THB"

        uiState = MutableStateFlow(baseState)
        actions = MutableSharedFlow(extraBufferCapacity = 1)

        handler = WithdrawalFeeHandler(
            getExchangeRateUseCase = getExchangeRateUseCase,
            exchangeRateCalculationService = exchangeRateCalculationService,
            addCashWithdrawalUiMapper = addCashWithdrawalUiMapper,
            formattingHelper = formattingHelper
        )
    }

    // ── FeeToggled ────────────────────────────────────────────────────────

    @Nested
    inner class FeeToggled {

        @Test
        fun `enabling fee sets hasFee to true`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(true)
            advanceUntilIdle()

            assertTrue(uiState.value.hasFee)
        }

        @Test
        fun `enabling fee sets feeCurrency to the group currency`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(true)
            advanceUntilIdle()

            assertEquals("EUR", uiState.value.feeCurrency?.code)
        }

        @Test
        fun `enabling fee resets feeAmount to empty`() = runTest {
            uiState.value = baseState.copy(feeAmount = "5.00")
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(true)
            advanceUntilIdle()

            assertEquals("", uiState.value.feeAmount)
        }

        @Test
        fun `enabling fee does not show fee exchange rate section`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(true)
            advanceUntilIdle()

            assertFalse(uiState.value.showFeeExchangeRateSection)
        }

        @Test
        fun `enabling fee sets isFeeAmountValid to true`() = runTest {
            uiState.value = baseState.copy(isFeeAmountValid = false)
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(true)
            advanceUntilIdle()

            assertTrue(uiState.value.isFeeAmountValid)
        }

        @Test
        fun `disabling fee sets hasFee to false`() = runTest {
            uiState.value = baseState.copy(hasFee = true, feeCurrency = eurModel)
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(false)
            advanceUntilIdle()

            assertFalse(uiState.value.hasFee)
        }

        @Test
        fun `disabling fee clears feeAmount`() = runTest {
            uiState.value = baseState.copy(hasFee = true, feeAmount = "3.50")
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(false)
            advanceUntilIdle()

            assertEquals("", uiState.value.feeAmount)
        }

        @Test
        fun `disabling fee clears feeCurrency`() = runTest {
            uiState.value = baseState.copy(hasFee = true, feeCurrency = thbModel)
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(false)
            advanceUntilIdle()

            assertNull(uiState.value.feeCurrency)
        }

        @Test
        fun `disabling fee hides fee exchange rate section`() = runTest {
            uiState.value = baseState.copy(hasFee = true, showFeeExchangeRateSection = true)
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(false)
            advanceUntilIdle()

            assertFalse(uiState.value.showFeeExchangeRateSection)
        }

        @Test
        fun `enabling fee does nothing when groupCurrency is null`() = runTest {
            uiState.value = baseState.copy(groupCurrency = null)
            handler.bind(uiState, actions, this)
            handler.handleFeeToggled(true)
            advanceUntilIdle()

            assertFalse(uiState.value.hasFee)
        }
    }

    // ── FeeAmountChanged ──────────────────────────────────────────────────

    @Nested
    inner class FeeAmountChanged {

        @Test
        fun `updates feeAmount in state`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeAmountChanged("5.00")
            advanceUntilIdle()

            assertEquals("5.00", uiState.value.feeAmount)
        }

        @Test
        fun `resets isFeeAmountValid to true`() = runTest {
            uiState.value = baseState.copy(isFeeAmountValid = false)
            handler.bind(uiState, actions, this)
            handler.handleFeeAmountChanged("5.00")
            advanceUntilIdle()

            assertTrue(uiState.value.isFeeAmountValid)
        }

        @Test
        fun `recalculates converted amount when fee exchange rate section is not shown`() = runTest {
            uiState.value = baseState.copy(
                hasFee = true,
                feeAmount = "",
                feeCurrency = eurModel,
                showFeeExchangeRateSection = false
            )
            handler.bind(uiState, actions, this)
            handler.handleFeeAmountChanged("5.00")
            advanceUntilIdle()

            // When not foreign, feeConvertedAmount mirrors feeAmount
            assertEquals("5.00", uiState.value.feeConvertedAmount)
        }
    }

    // ── FeeCurrencySelected ───────────────────────────────────────────────

    @Nested
    inner class FeeCurrencySelected {

        @Test
        fun `selecting the group currency does not show fee exchange rate section`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeCurrencySelected("EUR")
            advanceUntilIdle()

            assertFalse(uiState.value.showFeeExchangeRateSection)
        }

        @Test
        fun `selecting a foreign fee currency shows fee exchange rate section`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeCurrencySelected("THB")
            advanceUntilIdle()

            assertTrue(uiState.value.showFeeExchangeRateSection)
        }

        @Test
        fun `selecting a foreign fee currency updates feeCurrency`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeCurrencySelected("THB")
            advanceUntilIdle()

            assertEquals("THB", uiState.value.feeCurrency?.code)
        }

        @Test
        fun `selecting the group fee currency clears feeExchangeRateLabel`() = runTest {
            uiState.value = baseState.copy(feeExchangeRateLabel = "1 EUR = X THB")
            handler.bind(uiState, actions, this)
            handler.handleFeeCurrencySelected("EUR")
            advanceUntilIdle()

            assertEquals("", uiState.value.feeExchangeRateLabel)
        }

        @Test
        fun `selecting a currency not in availableCurrencies does not change feeCurrency`() = runTest {
            val originalFeeCurrency = uiState.value.feeCurrency
            handler.bind(uiState, actions, this)
            handler.handleFeeCurrencySelected("XYZ")
            advanceUntilIdle()

            assertEquals(originalFeeCurrency, uiState.value.feeCurrency)
        }
    }

    // ── FeeExchangeRateChanged ────────────────────────────────────────────

    @Nested
    inner class FeeExchangeRateChanged {

        @Test
        fun `updates feeExchangeRate in state`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeExchangeRateChanged("37.5")
            advanceUntilIdle()

            assertEquals("37.5", uiState.value.feeExchangeRate)
        }

        @Test
        fun `triggers recalculation of feeConvertedAmount when fee exchange rate section is shown`() =
            runTest {
                uiState.value = baseState.copy(
                    hasFee = true,
                    feeCurrency = thbModel,
                    groupCurrency = eurModel,
                    showFeeExchangeRateSection = true
                )
                handler.bind(uiState, actions, this)
                handler.handleFeeExchangeRateChanged("37.0")
                advanceUntilIdle()

                // formattingHelper.formatForDisplay stub returns "2.70"
                assertEquals("2.70", uiState.value.feeConvertedAmount)
            }
    }

    // ── FeeConvertedAmountChanged ─────────────────────────────────────────

    @Nested
    inner class FeeConvertedAmountChanged {

        @Test
        fun `updates feeConvertedAmount in state`() = runTest {
            handler.bind(uiState, actions, this)
            handler.handleFeeConvertedAmountChanged("2.70")
            advanceUntilIdle()

            assertEquals("2.70", uiState.value.feeConvertedAmount)
        }

        @Test
        fun `triggers implied rate recalculation when fee exchange rate section is shown`() = runTest {
            uiState.value = baseState.copy(
                hasFee = true,
                feeCurrency = thbModel,
                feeAmount = "100",
                showFeeExchangeRateSection = true
            )
            handler.bind(uiState, actions, this)
            handler.handleFeeConvertedAmountChanged("2.70")
            advanceUntilIdle()

            // formattingHelper.formatRateForDisplay stub returns "37.0"
            assertEquals("37.0", uiState.value.feeExchangeRate)
        }
    }
}
