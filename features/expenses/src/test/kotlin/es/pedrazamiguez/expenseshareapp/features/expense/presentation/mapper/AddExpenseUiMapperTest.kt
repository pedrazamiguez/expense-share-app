package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

class AddExpenseUiMapperTest {

    private lateinit var mapper: AddExpenseUiMapper
    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider

    private val eurDomain = Currency(
        code = "EUR",
        symbol = "€",
        defaultName = "Euro",
        decimalDigits = 2
    )

    private val usdDomain = Currency(
        code = "USD",
        symbol = "$",
        defaultName = "US Dollar",
        decimalDigits = 2
    )

    private val jpyDomain = Currency(
        code = "JPY",
        symbol = "¥",
        defaultName = "Japanese Yen",
        decimalDigits = 0
    )

    private val tndDomain = Currency(
        code = "TND",
        symbol = "د.ت",
        defaultName = "Tunisian Dinar",
        decimalDigits = 3
    )

    // UI Models for test state construction
    private val eurUi = CurrencyUiModel(code = "EUR", displayText = "EUR (€)", decimalDigits = 2)
    private val usdUi = CurrencyUiModel(code = "USD", displayText = "USD ($)", decimalDigits = 2)
    private val jpyUi = CurrencyUiModel(code = "JPY", displayText = "JPY (¥)", decimalDigits = 0)
    private val tndUi = CurrencyUiModel(code = "TND", displayText = "TND (د.ت)", decimalDigits = 3)

    private val cashPaymentMethod = PaymentMethodUiModel(id = "CASH", displayText = "Cash")
    private val creditCardPaymentMethod = PaymentMethodUiModel(id = "CREDIT_CARD", displayText = "Credit Card")
    private val debitCardPaymentMethod = PaymentMethodUiModel(id = "DEBIT_CARD", displayText = "Debit Card")
    private val bankTransferPaymentMethod = PaymentMethodUiModel(id = "BANK_TRANSFER", displayText = "Transfer")

    @BeforeEach
    fun setup() {
        localeProvider = mockk()
        resourceProvider = mockk(relaxed = true)
        every { localeProvider.getCurrentLocale() } returns Locale.US
        mapper = AddExpenseUiMapper(localeProvider, resourceProvider)
    }

    @Nested
    inner class MapCurrency {

        @Test
        fun `maps EUR domain currency to UI model`() {
            val result = mapper.mapCurrency(eurDomain)
            assertEquals("EUR", result.code)
            assertEquals(2, result.decimalDigits)
            assertTrue(result.displayText.contains("EUR"))
        }

        @Test
        fun `maps JPY domain currency to UI model`() {
            val result = mapper.mapCurrency(jpyDomain)
            assertEquals("JPY", result.code)
            assertEquals(0, result.decimalDigits)
            assertTrue(result.displayText.contains("JPY"))
        }
    }

    @Nested
    inner class MapCurrencies {

        @Test
        fun `maps list of domain currencies to UI models`() {
            val result = mapper.mapCurrencies(listOf(eurDomain, usdDomain, jpyDomain))
            assertEquals(3, result.size)
            assertEquals("EUR", result[0].code)
            assertEquals("USD", result[1].code)
            assertEquals("JPY", result[2].code)
        }

        @Test
        fun `maps empty list`() {
            val result = mapper.mapCurrencies(emptyList())
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class MapPaymentMethods {

        @Test
        fun `maps payment methods to UI models`() {
            every { resourceProvider.getString(any()) } answers {
                when (firstArg<Int>()) {
                    else -> "Mocked"
                }
            }
            val result = mapper.mapPaymentMethods(PaymentMethod.entries)
            assertEquals(PaymentMethod.entries.size, result.size)
            assertEquals("CASH", result[0].id)
            assertEquals("CREDIT_CARD", result[2].id)
        }
    }

    @Nested
    inner class BuildLabels {

        @Test
        fun `builds exchange rate label`() {
            every { resourceProvider.getString(any(), any(), any()) } returns "1 EUR (€) = ? USD ($)"
            val result = mapper.buildExchangeRateLabel(eurUi, usdUi)
            assertEquals("1 EUR (€) = ? USD ($)", result)
        }

        @Test
        fun `builds group amount label`() {
            every { resourceProvider.getString(any(), any()) } returns "in EUR (€)"
            val result = mapper.buildGroupAmountLabel(eurUi)
            assertEquals("in EUR (€)", result)
        }
    }

    @Nested
    inner class MapToDomain {

        @Test
        fun `maps basic expense with same currency`() {
            val state = AddExpenseUiState(
                expenseTitle = "Lunch",
                sourceAmount = "10.50",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals("group-123", expense.groupId)
            assertEquals("Lunch", expense.title)
            assertEquals(1050L, expense.sourceAmount)
            assertEquals("EUR", expense.sourceCurrency)
            assertEquals(1050L, expense.groupAmount)
            assertEquals("EUR", expense.groupCurrency)
            assertEquals(1.0, expense.exchangeRate)
            assertEquals(PaymentMethod.CASH, expense.paymentMethod)
        }

        @Test
        fun `maps expense with different currencies and explicit group amount`() {
            val state = AddExpenseUiState(
                expenseTitle = "Dinner",
                sourceAmount = "100.00",
                selectedCurrency = usdUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.086956522",
                calculatedGroupAmount = "92.00",
                selectedPaymentMethod = creditCardPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-456")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(10000L, expense.sourceAmount)
            assertEquals("USD", expense.sourceCurrency)
            assertEquals(9200L, expense.groupAmount)
            assertEquals("EUR", expense.groupCurrency)
            assertEquals(0.92, expense.exchangeRate, 0.0001)
        }

        @Test
        fun `calculates group amount when not explicitly set`() {
            val state = AddExpenseUiState(
                expenseTitle = "Coffee",
                sourceAmount = "5.00",
                selectedCurrency = usdUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.086956522",
                calculatedGroupAmount = "",
                selectedPaymentMethod = debitCardPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-789")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(500L, expense.sourceAmount)
            assertEquals(460L, expense.groupAmount)
        }

        @Test
        fun `handles European format with comma decimal`() {
            val state = AddExpenseUiState(
                expenseTitle = "Museum",
                sourceAmount = "15,50",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(1550L, expense.sourceAmount)
        }

        @Test
        fun `handles US format with thousand separator`() {
            val state = AddExpenseUiState(
                expenseTitle = "Hotel",
                sourceAmount = "1,250.00",
                selectedCurrency = usdUi,
                groupCurrency = usdUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = creditCardPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(125000L, expense.sourceAmount)
        }

        @Test
        fun `handles European format with thousand separator`() {
            val state = AddExpenseUiState(
                expenseTitle = "Rent",
                sourceAmount = "1.250,00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = bankTransferPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(125000L, expense.sourceAmount)
        }
    }

    @Nested
    inner class CurrencyDecimalPlaces {

        @Test
        fun `handles JPY with 0 decimal places`() {
            val state = AddExpenseUiState(
                expenseTitle = "Sushi",
                sourceAmount = "1500",
                selectedCurrency = jpyUi,
                groupCurrency = jpyUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(1500L, expense.sourceAmount)
            assertEquals("JPY", expense.sourceCurrency)
        }

        @Test
        fun `handles TND with 3 decimal places`() {
            val state = AddExpenseUiState(
                expenseTitle = "Taxi",
                sourceAmount = "10.500",
                selectedCurrency = tndUi,
                groupCurrency = tndUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(10500L, expense.sourceAmount)
            assertEquals("TND", expense.sourceCurrency)
        }

        @Test
        fun `converts between currencies with different decimal places`() {
            val state = AddExpenseUiState(
                expenseTitle = "Exchange",
                sourceAmount = "1000",
                selectedCurrency = jpyUi,
                groupCurrency = eurUi,
                displayExchangeRate = "149.2537313",
                calculatedGroupAmount = "6.70",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(1000L, expense.sourceAmount)
            assertEquals("JPY", expense.sourceCurrency)
            assertEquals(670L, expense.groupAmount)
            assertEquals("EUR", expense.groupCurrency)
            assertEquals(0.0067, expense.exchangeRate, 0.0001)
        }
    }

    @Nested
    inner class EdgeCases {

        @Test
        fun `handles empty amount as zero`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(0L, expense.sourceAmount)
        }

        @Test
        fun `handles whitespace amount as zero`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "   ",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(0L, expense.sourceAmount)
        }

        @Test
        fun `uses default EUR when currency is null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = null,
                groupCurrency = null,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals("EUR", expense.sourceCurrency)
            assertEquals("EUR", expense.groupCurrency)
            assertEquals(1000L, expense.sourceAmount)
        }

        @Test
        fun `uses default rate of 1 when exchange rate is invalid`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "invalid",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(1.0, expense.exchangeRate)
        }

        @Test
        fun `handles amount with leading and trailing whitespace`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "  25.99  ",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(2599L, expense.sourceAmount)
        }

        @Test
        fun `uses CASH when payment method is null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = null
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(PaymentMethod.CASH, expense.paymentMethod)
        }
    }
}
