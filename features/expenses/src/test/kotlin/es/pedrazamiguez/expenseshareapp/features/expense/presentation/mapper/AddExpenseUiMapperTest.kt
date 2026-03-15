package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CategoryUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentStatusUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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

        // Stub all expense category string resources
        ExpenseCategory.entries.forEach { category ->
            every { resourceProvider.getString(category.toStringRes()) } returns category.name
        }

        // Stub all payment status string resources
        PaymentStatus.entries.forEach { status ->
            every { resourceProvider.getString(status.toStringRes()) } returns status.name
        }

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

        @Test
        fun `maps TND domain currency to UI model with 3 decimal places`() {
            val result = mapper.mapCurrency(tndDomain)
            assertEquals("TND", result.code)
            assertEquals(3, result.decimalDigits)
            assertTrue(result.displayText.contains("TND"))
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
            assertEquals("PIX", result[2].id)
            assertEquals("CREDIT_CARD", result[3].id)
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
            assertEquals(0, BigDecimal.ONE.compareTo(expense.exchangeRate))
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
            assertEquals(
                0,
                BigDecimal("0.92").compareTo(expense.exchangeRate.setScale(2, java.math.RoundingMode.HALF_UP))
            )
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
            assertEquals(
                0,
                BigDecimal("0.0067").compareTo(expense.exchangeRate.setScale(4, java.math.RoundingMode.HALF_UP))
            )
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
            assertEquals(0, BigDecimal.ONE.compareTo(expense.exchangeRate))
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

    @Nested
    inner class MapCategories {

        @Test
        fun `filters out CONTRIBUTION and REFUND`() {
            val result = mapper.mapCategories(ExpenseCategory.entries)

            val ids = result.map { it.id }
            assertTrue("CONTRIBUTION" !in ids)
            assertTrue("REFUND" !in ids)
        }

        @Test
        fun `includes user-selectable categories`() {
            val result = mapper.mapCategories(ExpenseCategory.entries)

            val ids = result.map { it.id }
            assertTrue("TRANSPORT" in ids)
            assertTrue("FOOD" in ids)
            assertTrue("LODGING" in ids)
            assertTrue("ACTIVITIES" in ids)
            assertTrue("INSURANCE" in ids)
            assertTrue("ENTERTAINMENT" in ids)
            assertTrue("SHOPPING" in ids)
            assertTrue("OTHER" in ids)
        }

        @Test
        fun `maps display text from resource provider`() {
            every { resourceProvider.getString(ExpenseCategory.FOOD.toStringRes()) } returns "Food & Restaurants"

            val result = mapper.mapCategories(listOf(ExpenseCategory.FOOD))

            assertEquals(1, result.size)
            assertEquals("FOOD", result[0].id)
            assertEquals("Food & Restaurants", result[0].displayText)
        }

        @Test
        fun `returns empty list for empty input`() {
            val result = mapper.mapCategories(emptyList())
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class MapPaymentStatuses {

        @Test
        fun `only includes FINISHED and SCHEDULED`() {
            val result = mapper.mapPaymentStatuses(PaymentStatus.entries)

            assertEquals(2, result.size)
            val ids = result.map { it.id }
            assertTrue("FINISHED" in ids)
            assertTrue("SCHEDULED" in ids)
        }

        @Test
        fun `excludes RECEIVED, PENDING, and CANCELLED`() {
            val result = mapper.mapPaymentStatuses(PaymentStatus.entries)

            val ids = result.map { it.id }
            assertTrue("RECEIVED" !in ids)
            assertTrue("PENDING" !in ids)
            assertTrue("CANCELLED" !in ids)
        }

        @Test
        fun `maps display text from resource provider`() {
            every { resourceProvider.getString(PaymentStatus.SCHEDULED.toStringRes()) } returns "Scheduled"

            val result = mapper.mapPaymentStatuses(listOf(PaymentStatus.SCHEDULED))

            assertEquals(1, result.size)
            assertEquals("SCHEDULED", result[0].id)
            assertEquals("Scheduled", result[0].displayText)
        }

        @Test
        fun `returns empty list for empty input`() {
            val result = mapper.mapPaymentStatuses(emptyList())
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class FormatDueDateForDisplay {

        @Test
        fun `formats UTC millis using US locale`() {
            // 2026-03-16 00:00:00 UTC in millis
            val millis = 1773619200000L

            val result = mapper.formatDueDateForDisplay(millis)

            // US locale MEDIUM format: "Mar 16, 2026"
            assertEquals("Mar 16, 2026", result)
        }

        @Test
        fun `formats UTC millis using Spanish locale`() {
            every { localeProvider.getCurrentLocale() } returns Locale.forLanguageTag("es-ES")
            // 2026-03-16 00:00:00 UTC in millis
            val millis = 1773619200000L

            val result = mapper.formatDueDateForDisplay(millis)

            // Spanish locale MEDIUM format: "16 mar 2026"
            assertTrue(result.contains("16"))
            assertTrue(result.contains("2026"))
        }
    }

    @Nested
    inner class MapToDomainExtended {

        @Test
        fun `maps category from selected category UI model`() {
            val state = AddExpenseUiState(
                expenseTitle = "Groceries",
                sourceAmount = "50.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedCategory = CategoryUiModel(id = "FOOD", displayText = "Food")
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertEquals(ExpenseCategory.FOOD, result.getOrThrow().category)
        }

        @Test
        fun `defaults category to OTHER when null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedCategory = null
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertEquals(ExpenseCategory.OTHER, result.getOrThrow().category)
        }

        @Test
        fun `maps vendor from state`() {
            val state = AddExpenseUiState(
                expenseTitle = "Coffee",
                sourceAmount = "5.00",
                vendor = "Starbucks",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertEquals("Starbucks", result.getOrThrow().vendor)
        }

        @Test
        fun `maps blank vendor to null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Coffee",
                sourceAmount = "5.00",
                vendor = "   ",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().vendor)
        }

        @Test
        fun `maps notes from state`() {
            val state = AddExpenseUiState(
                expenseTitle = "Coffee",
                sourceAmount = "5.00",
                notes = "Shared with team",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertEquals("Shared with team", result.getOrThrow().notes)
        }

        @Test
        fun `maps blank notes to null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Coffee",
                sourceAmount = "5.00",
                notes = "   ",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().notes)
        }

        @Test
        fun `maps empty notes to null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Coffee",
                sourceAmount = "5.00",
                notes = "",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().notes)
        }

        @Test
        fun `maps payment status from selected status UI model`() {
            val state = AddExpenseUiState(
                expenseTitle = "Bill",
                sourceAmount = "100.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedPaymentStatus = PaymentStatusUiModel(id = "SCHEDULED", displayText = "Scheduled")
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertEquals(PaymentStatus.SCHEDULED, result.getOrThrow().paymentStatus)
        }

        @Test
        fun `defaults payment status to FINISHED when null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedPaymentStatus = null
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertEquals(PaymentStatus.FINISHED, result.getOrThrow().paymentStatus)
        }

        @Test
        fun `maps due date when status is SCHEDULED and millis present`() {
            // 2026-03-16 00:00:00 UTC
            val millis = 1773619200000L
            val state = AddExpenseUiState(
                expenseTitle = "Rent",
                sourceAmount = "500.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedPaymentStatus = PaymentStatusUiModel(id = "SCHEDULED", displayText = "Scheduled"),
                dueDateMillis = millis
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertNotNull(expense.dueDate)
            assertEquals(2026, expense.dueDate?.year)
            assertEquals(3, expense.dueDate?.monthValue)
            assertEquals(16, expense.dueDate?.dayOfMonth)
        }

        @Test
        fun `due date is null when status is not SCHEDULED`() {
            val state = AddExpenseUiState(
                expenseTitle = "Lunch",
                sourceAmount = "15.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedPaymentStatus = PaymentStatusUiModel(id = "FINISHED", displayText = "Paid"),
                dueDateMillis = 1773619200000L
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().dueDate)
        }

        @Test
        fun `maps receipt URI from state`() {
            val state = AddExpenseUiState(
                expenseTitle = "Dinner",
                sourceAmount = "80.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                receiptUri = "content://media/photo/123"
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertEquals("content://media/photo/123", result.getOrThrow().receiptLocalUri)
        }

        @Test
        fun `receipt URI is null when not provided`() {
            val state = AddExpenseUiState(
                expenseTitle = "Dinner",
                sourceAmount = "80.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                receiptUri = null
            )

            val result = mapper.mapToDomain(state, "group-123")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().receiptLocalUri)
        }
    }
}
