package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddExpenseUiMapperTest {

    private lateinit var mapper: AddExpenseUiMapper

    private val eur = Currency(
        code = "EUR",
        symbol = "€",
        defaultName = "Euro",
        decimalDigits = 2
    )

    private val usd = Currency(
        code = "USD",
        symbol = "$",
        defaultName = "US Dollar",
        decimalDigits = 2
    )

    private val jpy = Currency(
        code = "JPY",
        symbol = "¥",
        defaultName = "Japanese Yen",
        decimalDigits = 0
    )

    private val tnd = Currency(
        code = "TND",
        symbol = "د.ت",
        defaultName = "Tunisian Dinar",
        decimalDigits = 3
    )

    @BeforeEach
    fun setup() {
        mapper = AddExpenseUiMapper()
    }

    @Nested
    inner class MapToDomain {

        @Test
        fun `maps basic expense with same currency`() {
            val state = AddExpenseUiState(
                expenseTitle = "Lunch",
                sourceAmount = "10.50",
                selectedCurrency = eur,
                groupCurrency = eur,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
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
            // Display rate is "1 EUR = 1.087 USD" (inverse of internal rate 0.92)
            // User sees: 1 EUR = 1.086956522 USD
            // Internal rate stored: 1/1.086956522 ≈ 0.92
            val state = AddExpenseUiState(
                expenseTitle = "Dinner",
                sourceAmount = "100.00",
                selectedCurrency = usd,
                groupCurrency = eur,
                displayExchangeRate = "1.086956522",
                calculatedGroupAmount = "92.00",
                selectedPaymentMethod = PaymentMethod.CREDIT_CARD
            )

            val result = mapper.mapToDomain(state, "group-456")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(10000L, expense.sourceAmount)
            assertEquals("USD", expense.sourceCurrency)
            assertEquals(9200L, expense.groupAmount)
            assertEquals("EUR", expense.groupCurrency)
            // Internal rate = 1/1.086956522 ≈ 0.920000
            assertEquals(0.92, expense.exchangeRate, 0.0001)
        }

        @Test
        fun `calculates group amount when not explicitly set`() {
            // Display rate: "1 EUR = 1.086956522 USD"
            // Internal rate = 1/1.086956522 ≈ 0.92
            // Source: 5.00 USD = 500 cents
            // Group amount = 500 * 0.92 = 460 cents
            val state = AddExpenseUiState(
                expenseTitle = "Coffee",
                sourceAmount = "5.00",
                selectedCurrency = usd,
                groupCurrency = eur,
                displayExchangeRate = "1.086956522", // 1 EUR = 1.087 USD
                calculatedGroupAmount = "", // Empty means calculate
                selectedPaymentMethod = PaymentMethod.DEBIT_CARD
            )

            val result = mapper.mapToDomain(state, "group-789")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(500L, expense.sourceAmount)
            // 500 * (1/1.086956522) ≈ 500 * 0.92 = 460
            assertEquals(460L, expense.groupAmount)
        }

        @Test
        fun `handles European format with comma decimal`() {
            val state = AddExpenseUiState(
                expenseTitle = "Museum",
                sourceAmount = "15,50",
                selectedCurrency = eur,
                groupCurrency = eur,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
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
                selectedCurrency = usd,
                groupCurrency = usd,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CREDIT_CARD
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
                selectedCurrency = eur,
                groupCurrency = eur,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.BANK_TRANSFER
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
                selectedCurrency = jpy,
                groupCurrency = jpy,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            // JPY has 0 decimal places, so 1500 yen = 1500 (no cents)
            assertEquals(1500L, expense.sourceAmount)
            assertEquals("JPY", expense.sourceCurrency)
        }

        @Test
        fun `handles TND with 3 decimal places`() {
            val state = AddExpenseUiState(
                expenseTitle = "Taxi",
                sourceAmount = "10.500",
                selectedCurrency = tnd,
                groupCurrency = tnd,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            // TND has 3 decimal places, so 10.500 = 10500 millimes
            assertEquals(10500L, expense.sourceAmount)
            assertEquals("TND", expense.sourceCurrency)
        }

        @Test
        fun `converts between currencies with different decimal places`() {
            // Display rate "1 EUR = 149.25 JPY" (inverse of internal rate 0.0067)
            val state = AddExpenseUiState(
                expenseTitle = "Exchange",
                sourceAmount = "1000",
                selectedCurrency = jpy,
                groupCurrency = eur,
                displayExchangeRate = "149.2537313", // 1/0.0067 - user sees "1 EUR = 149.25 JPY"
                calculatedGroupAmount = "6.70",
                selectedPaymentMethod = PaymentMethod.CASH
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(1000L, expense.sourceAmount) // 1000 yen
            assertEquals("JPY", expense.sourceCurrency)
            assertEquals(670L, expense.groupAmount) // 6.70 EUR = 670 cents
            assertEquals("EUR", expense.groupCurrency)
            // Internal rate should be 1/149.2537313 ≈ 0.0067
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
                selectedCurrency = eur,
                groupCurrency = eur,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
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
                selectedCurrency = eur,
                groupCurrency = eur,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
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
                selectedPaymentMethod = PaymentMethod.CASH
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals("EUR", expense.sourceCurrency)
            assertEquals("EUR", expense.groupCurrency)
            // Default decimal places is 2
            assertEquals(1000L, expense.sourceAmount)
        }

        @Test
        fun `uses default rate of 1 when exchange rate is invalid`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eur,
                groupCurrency = eur,
                displayExchangeRate = "invalid",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
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
                selectedCurrency = eur,
                groupCurrency = eur,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = PaymentMethod.CASH
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(2599L, expense.sourceAmount)
        }
    }
}
