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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
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

    private val otherCategory = CategoryUiModel(id = ExpenseCategory.OTHER.name, displayText = "Other")
    private val finishedStatus = PaymentStatusUiModel(id = PaymentStatus.FINISHED.name, displayText = "Finished")
    private val scheduledStatus = PaymentStatusUiModel(id = PaymentStatus.SCHEDULED.name, displayText = "Scheduled")

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

    @Nested
    inner class MapCategories {

        @Test
        fun `filters out CONTRIBUTION and REFUND`() {
            val result = mapper.mapCategories(ExpenseCategory.entries)

            val ids = result.map { it.id }
            assertFalse(ids.contains(ExpenseCategory.CONTRIBUTION.name))
            assertFalse(ids.contains(ExpenseCategory.REFUND.name))
        }

        @Test
        fun `includes all user-selectable categories`() {
            val result = mapper.mapCategories(ExpenseCategory.entries)

            val expectedIds = setOf(
                ExpenseCategory.TRANSPORT.name,
                ExpenseCategory.FOOD.name,
                ExpenseCategory.LODGING.name,
                ExpenseCategory.ACTIVITIES.name,
                ExpenseCategory.INSURANCE.name,
                ExpenseCategory.ENTERTAINMENT.name,
                ExpenseCategory.SHOPPING.name,
                ExpenseCategory.OTHER.name
            )
            assertEquals(expectedIds, result.map { it.id }.toSet())
        }

        @Test
        fun `maps category id from enum name`() {
            val result = mapper.mapCategories(listOf(ExpenseCategory.FOOD))

            assertEquals(1, result.size)
            assertEquals(ExpenseCategory.FOOD.name, result[0].id)
        }

        @Test
        fun `maps empty list to empty result`() {
            val result = mapper.mapCategories(emptyList())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `input containing only non-selectable categories returns empty`() {
            val result = mapper.mapCategories(
                listOf(ExpenseCategory.CONTRIBUTION, ExpenseCategory.REFUND)
            )

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class MapPaymentStatuses {

        @Test
        fun `includes only FINISHED and SCHEDULED`() {
            val result = mapper.mapPaymentStatuses(PaymentStatus.entries)

            val ids = result.map { it.id }.toSet()
            assertEquals(setOf(PaymentStatus.FINISHED.name, PaymentStatus.SCHEDULED.name), ids)
        }

        @Test
        fun `excludes RECEIVED, PENDING, CANCELLED`() {
            val result = mapper.mapPaymentStatuses(PaymentStatus.entries)

            val ids = result.map { it.id }
            assertFalse(ids.contains(PaymentStatus.RECEIVED.name))
            assertFalse(ids.contains(PaymentStatus.PENDING.name))
            assertFalse(ids.contains(PaymentStatus.CANCELLED.name))
        }

        @Test
        fun `maps id from enum name`() {
            val result = mapper.mapPaymentStatuses(
                listOf(PaymentStatus.FINISHED, PaymentStatus.SCHEDULED)
            )

            assertEquals(PaymentStatus.FINISHED.name, result[0].id)
            assertEquals(PaymentStatus.SCHEDULED.name, result[1].id)
        }

        @Test
        fun `returns empty for input with no selectable statuses`() {
            val result = mapper.mapPaymentStatuses(
                listOf(PaymentStatus.RECEIVED, PaymentStatus.PENDING, PaymentStatus.CANCELLED)
            )

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class FormatDueDateForDisplay {

        @Test
        fun `formats Jan 15 2025 UTC midnight correctly for US locale`() {
            // Jan 15 2025 00:00:00 UTC = 1736899200000L
            val jan15UtcMillis = java.time.LocalDate.of(2025, 1, 15)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

            val result = mapper.formatDueDateForDisplay(jan15UtcMillis)

            // US locale, FormatStyle.MEDIUM → "Jan 15, 2025"
            assertEquals("Jan 15, 2025", result)
        }

        @Test
        fun `uses UTC zone so date is not shifted for negative-UTC locales`() {
            // A timestamp at Jan 15 2025 02:00:00 UTC should still show Jan 15
            // (regardless of local timezone, since we always use UTC)
            val millis = java.time.LocalDate.of(2025, 1, 15)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

            val result = mapper.formatDueDateForDisplay(millis)

            assertTrue(result.contains("15"))
            assertTrue(result.contains("2025"))
        }
    }

    @Nested
    inner class MapToDomainNewFields {

        @Test
        fun `maps category from selected category UI model`() {
            val state = AddExpenseUiState(
                expenseTitle = "Dinner",
                sourceAmount = "25.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedCategory = CategoryUiModel(id = ExpenseCategory.FOOD.name, displayText = "Food")
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertEquals(ExpenseCategory.FOOD, result.getOrThrow().category)
        }

        @Test
        fun `defaults to OTHER category when selectedCategory is null`() {
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

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertEquals(ExpenseCategory.OTHER, result.getOrThrow().category)
        }

        @Test
        fun `defaults to OTHER category when selectedCategory id is unrecognized`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedCategory = CategoryUiModel(id = "UNKNOWN_CATEGORY", displayText = "?")
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertEquals(ExpenseCategory.OTHER, result.getOrThrow().category)
        }

        @Test
        fun `maps vendor from state`() {
            val state = AddExpenseUiState(
                expenseTitle = "Groceries",
                sourceAmount = "50.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                vendor = "SuperMarket"
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertEquals("SuperMarket", result.getOrThrow().vendor)
        }

        @Test
        fun `maps blank vendor as null`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                vendor = "   "
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().vendor)
        }

        @Test
        fun `maps FINISHED payment status`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedPaymentStatus = finishedStatus
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(PaymentStatus.FINISHED, expense.paymentStatus)
            assertNull(expense.dueDate)
        }

        @Test
        fun `maps SCHEDULED status with due date using UTC zone`() {
            // Jan 15 2025 00:00:00 UTC
            val jan15UtcMillis = java.time.LocalDate.of(2025, 1, 15)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

            val state = AddExpenseUiState(
                expenseTitle = "Hotel",
                sourceAmount = "200.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = bankTransferPaymentMethod,
                selectedPaymentStatus = scheduledStatus,
                dueDateMillis = jan15UtcMillis
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            val expense = result.getOrThrow()
            assertEquals(PaymentStatus.SCHEDULED, expense.paymentStatus)
            assertNotNull(expense.dueDate)
            // dueDate should be 2025-01-15 (not shifted by timezone)
            assertEquals(2025, expense.dueDate!!.year)
            assertEquals(1, expense.dueDate!!.monthValue)
            assertEquals(15, expense.dueDate!!.dayOfMonth)
        }

        @Test
        fun `dueDate is null for SCHEDULED status without dueDateMillis`() {
            val state = AddExpenseUiState(
                expenseTitle = "Hotel",
                sourceAmount = "200.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = bankTransferPaymentMethod,
                selectedPaymentStatus = scheduledStatus,
                dueDateMillis = null
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().dueDate)
        }

        @Test
        fun `dueDate is null when payment status is not SCHEDULED`() {
            val jan15UtcMillis = java.time.LocalDate.of(2025, 1, 15)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()

            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                selectedPaymentStatus = finishedStatus,
                dueDateMillis = jan15UtcMillis
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().dueDate)
        }

        @Test
        fun `maps receipt URI from state`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                receiptUri = "content://media/external/images/123"
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertEquals("content://media/external/images/123", result.getOrThrow().receiptLocalUri)
        }

        @Test
        fun `receipt URI is null when not provided`() {
            val state = AddExpenseUiState(
                expenseTitle = "Test",
                sourceAmount = "10.00",
                selectedCurrency = eurUi,
                groupCurrency = eurUi,
                displayExchangeRate = "1.0",
                calculatedGroupAmount = "",
                selectedPaymentMethod = cashPaymentMethod,
                receiptUri = null
            )

            val result = mapper.mapToDomain(state, "group-1")

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow().receiptLocalUri)
        }
    }
}
