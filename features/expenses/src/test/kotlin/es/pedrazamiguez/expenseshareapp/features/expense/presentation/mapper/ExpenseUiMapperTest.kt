package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.enums.ExpenseCategory
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

@DisplayName("ExpenseUiMapper")
class ExpenseUiMapperTest {

    private lateinit var mapper: ExpenseUiMapper
    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider

    @BeforeEach
    fun setUp() {
        localeProvider = mockk()
        resourceProvider = mockk()

        every { localeProvider.getCurrentLocale() } returns Locale.US

        // Stub paid_by pattern — vararg overload packs trailing args into an Array
        every { resourceProvider.getString(R.string.paid_by, *anyVararg()) } answers {
            val varargs = it.invocation.args[1] as Array<*>
            "Paid by ${varargs[0]}"
        }

        // Stub all payment method string resources
        PaymentMethod.entries.forEach { method ->
            every { resourceProvider.getString(method.toStringRes()) } returns method.name
        }

        // Stub all expense category string resources
        ExpenseCategory.entries.forEach { category ->
            every { resourceProvider.getString(category.toStringRes()) } returns category.name
        }

        // Stub all payment status string resources
        PaymentStatus.entries.forEach { status ->
            every { resourceProvider.getString(status.toStringRes()) } returns status.name
        }

        // Stub scheduled badge strings
        every { resourceProvider.getString(R.string.expense_scheduled_due_today) } returns "Due today"
        every { resourceProvider.getString(R.string.expense_scheduled_paid) } returns "Paid"
        every { resourceProvider.getString(R.string.expense_scheduled_due_on, *anyVararg()) } answers {
            val varargs = it.invocation.args[1] as Array<*>
            "Due on ${varargs[0]}"
        }

        mapper = ExpenseUiMapper(localeProvider, resourceProvider)
    }

    // ---------- formattedOriginalAmount ----------

    @Nested
    @DisplayName("formattedOriginalAmount mapping")
    inner class FormattedOriginalAmount {

        @Test
        fun `is null when sourceCurrency equals groupCurrency`() {
            val expense = Expense(
                id = "e1",
                sourceAmount = 5000,
                sourceCurrency = "EUR",
                groupAmount = 5000,
                groupCurrency = "EUR"
            )

            val result = mapper.map(expense)

            assertNull(result.formattedOriginalAmount)
        }

        @Test
        fun `is non-null when sourceCurrency differs from groupCurrency`() {
            val expense = Expense(
                id = "e2",
                sourceAmount = 9000,
                sourceCurrency = "THB",
                groupAmount = 248,
                groupCurrency = "EUR"
            )

            val result = mapper.map(expense)

            // THB 9000 cents = THB 90.00 formatted in US locale
            assertEquals("฿90.00", result.formattedOriginalAmount)
        }

        @Test
        fun `formats JPY source correctly with 0 decimals`() {
            val expense = Expense(
                id = "e3",
                sourceAmount = 15725,
                sourceCurrency = "JPY",
                groupAmount = 10000,
                groupCurrency = "EUR"
            )

            val result = mapper.map(expense)

            assertEquals("¥15,725", result.formattedOriginalAmount)
        }

        @Test
        fun `formats group amount regardless of currency match`() {
            val expense = Expense(
                id = "e4",
                sourceAmount = 5000,
                sourceCurrency = "USD",
                groupAmount = 4600,
                groupCurrency = "EUR"
            )

            val result = mapper.map(expense)

            // Group amount: 4600 cents EUR = €46.00
            assertEquals("€46.00", result.formattedAmount)
            // Source amount: 5000 cents USD = $50.00
            assertEquals("$50.00", result.formattedOriginalAmount)
        }
    }

    // ---------- paymentMethodText ----------

    @Nested
    @DisplayName("paymentMethodText mapping")
    inner class PaymentMethodText {

        @Test
        fun `resolves CASH payment method text`() {
            every { resourceProvider.getString(R.string.payment_method_cash) } returns "Cash"

            val expense = Expense(id = "e5", paymentMethod = PaymentMethod.CASH)

            val result = mapper.map(expense)

            assertEquals("Cash", result.paymentMethodText)
        }

        @Test
        fun `resolves CREDIT_CARD payment method text`() {
            every { resourceProvider.getString(R.string.payment_method_credit_card) } returns "Credit Card"

            val expense = Expense(id = "e6", paymentMethod = PaymentMethod.CREDIT_CARD)

            val result = mapper.map(expense)

            assertEquals("Credit Card", result.paymentMethodText)
        }

        @Test
        fun `resolves DEBIT_CARD payment method text`() {
            every { resourceProvider.getString(R.string.payment_method_debit_card) } returns "Debit Card"

            val expense = Expense(id = "e7", paymentMethod = PaymentMethod.DEBIT_CARD)

            val result = mapper.map(expense)

            assertEquals("Debit Card", result.paymentMethodText)
        }

        @Test
        fun `resolves OTHER payment method text`() {
            every { resourceProvider.getString(R.string.payment_method_other) } returns "Other"

            val expense = Expense(id = "e8", paymentMethod = PaymentMethod.OTHER)

            val result = mapper.map(expense)

            assertEquals("Other", result.paymentMethodText)
        }
    }

    // ---------- Other existing fields ----------

    @Nested
    @DisplayName("Basic field mapping")
    inner class BasicFields {

        @Test
        fun `maps id and title`() {
            val expense = Expense(id = "abc-123", title = "Dinner")

            val result = mapper.map(expense)

            assertEquals("abc-123", result.id)
            assertEquals("Dinner", result.title)
        }

        @Test
        fun `maps paidByText using resourceProvider`() {
            val expense = Expense(id = "e9", createdBy = "Alice")

            val result = mapper.map(expense)

            assertEquals("Paid by Alice", result.paidByText)
        }

        @Test
        fun `maps dateText from createdAt`() {
            val expense = Expense(
                id = "e10",
                createdAt = LocalDateTime.of(2025, 1, 15, 12, 30)
            )

            val result = mapper.map(expense)

            // US locale short date: "15 Jan"
            assertEquals("15 Jan", result.dateText)
        }

        @Test
        fun `maps dateText as empty when createdAt is null`() {
            val expense = Expense(id = "e11", createdAt = null)

            val result = mapper.map(expense)

            assertEquals("", result.dateText)
        }
    }

    // ---------- mapList ----------

    @Nested
    @DisplayName("mapList")
    inner class MapList {

        @Test
        fun `maps a list of expenses preserving order`() {
            val expenses = listOf(
                Expense(id = "1", title = "First"),
                Expense(id = "2", title = "Second"),
                Expense(id = "3", title = "Third")
            )

            val result = mapper.mapList(expenses)

            assertEquals(3, result.size)
            assertEquals("1", result[0].id)
            assertEquals("2", result[1].id)
            assertEquals("3", result[2].id)
        }

        @Test
        fun `returns empty immutable list for empty input`() {
            val result = mapper.mapList(emptyList())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `correctly maps mixed currency expenses in list`() {
            val expenses = listOf(
                Expense(
                    id = "same",
                    sourceCurrency = "EUR",
                    groupCurrency = "EUR"
                ),
                Expense(
                    id = "different",
                    sourceAmount = 9000,
                    sourceCurrency = "THB",
                    groupAmount = 248,
                    groupCurrency = "EUR"
                )
            )

            val result = mapper.mapList(expenses)

            assertNull(result[0].formattedOriginalAmount)
            assertEquals("฿90.00", result[1].formattedOriginalAmount)
        }
    }

    // ---------- Locale-dependent formatting ----------

    @Nested
    @DisplayName("Locale-dependent formatting")
    inner class LocaleFormatting {

        @Test
        fun `formats amounts using Spanish locale`() {
            every { localeProvider.getCurrentLocale() } returns Locale.forLanguageTag("es-ES")

            val expense = Expense(
                id = "e12",
                sourceAmount = 9000,
                sourceCurrency = "THB",
                groupAmount = 248,
                groupCurrency = "EUR"
            )

            val result = mapper.map(expense)

            // Spanish locale: "2,48 €" (with NBSP)
            assertEquals("2,48\u00A0€", result.formattedAmount)
            // Spanish locale uses Baht symbol: "90,00 ฿" (with NBSP)
            assertEquals("90,00\u00A0฿", result.formattedOriginalAmount)
        }
    }

    // ---------- mapGroupedByDate ----------

    @Nested
    @DisplayName("mapGroupedByDate")
    inner class MapGroupedByDate {

        @Test
        fun `returns empty list for empty input`() {
            val result = mapper.mapGroupedByDate(emptyList())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `groups single expense into single date group`() {
            val expenses = listOf(
                Expense(
                    id = "1",
                    title = "Lunch",
                    groupAmount = 1500,
                    groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 10, 12, 0)
                )
            )

            val result = mapper.mapGroupedByDate(expenses)

            assertEquals(1, result.size)
            assertEquals(1, result[0].expenses.size)
            assertEquals("1", result[0].expenses[0].id)
        }

        @Test
        fun `groups expenses from same day into one group`() {
            val expenses = listOf(
                Expense(
                    id = "1", title = "Breakfast",
                    groupAmount = 500, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 10, 8, 0)
                ),
                Expense(
                    id = "2", title = "Lunch",
                    groupAmount = 1500, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 10, 13, 0)
                )
            )

            val result = mapper.mapGroupedByDate(expenses)

            assertEquals(1, result.size)
            assertEquals(2, result[0].expenses.size)
        }

        @Test
        fun `groups expenses from different days into separate groups`() {
            val expenses = listOf(
                Expense(
                    id = "1", title = "Day 1 expense",
                    groupAmount = 1000, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 10, 12, 0)
                ),
                Expense(
                    id = "2", title = "Day 2 expense",
                    groupAmount = 2000, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 11, 12, 0)
                )
            )

            val result = mapper.mapGroupedByDate(expenses)

            assertEquals(2, result.size)
        }

        @Test
        fun `computes correct daily total for a group`() {
            val expenses = listOf(
                Expense(
                    id = "1", groupAmount = 1000, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 10, 8, 0)
                ),
                Expense(
                    id = "2", groupAmount = 2500, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 10, 14, 0)
                )
            )

            val result = mapper.mapGroupedByDate(expenses)

            assertEquals(1, result.size)
            // 1000 + 2500 = 3500 cents = €35.00 in US locale
            assertEquals("€35.00", result[0].formattedDayTotal)
        }

        @Test
        fun `computes separate daily totals for different days`() {
            val expenses = listOf(
                Expense(
                    id = "1", groupAmount = 1000, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 10, 12, 0)
                ),
                Expense(
                    id = "2", groupAmount = 5000, groupCurrency = "EUR",
                    createdAt = LocalDateTime.of(2025, 3, 11, 12, 0)
                )
            )

            val result = mapper.mapGroupedByDate(expenses)

            assertEquals(2, result.size)
            val totals = result.map { it.formattedDayTotal }.toSet()
            assertTrue(totals.contains("€10.00"))
            assertTrue(totals.contains("€50.00"))
        }

        @Test
        fun `expense with null createdAt is grouped under empty dateText`() {
            val expenses = listOf(
                Expense(id = "1", groupAmount = 1000, groupCurrency = "EUR", createdAt = null)
            )

            val result = mapper.mapGroupedByDate(expenses)

            assertEquals(1, result.size)
            assertEquals("", result[0].dateText)
        }
    }

    // ---------- buildScheduledBadge (via map) ----------

    @Nested
    @DisplayName("buildScheduledBadge")
    inner class BuildScheduledBadge {

        @Test
        fun `returns null badge for non-scheduled expense`() {
            val expense = Expense(
                id = "1",
                paymentStatus = PaymentStatus.FINISHED,
                dueDate = null
            )

            val result = mapper.map(expense)

            assertNull(result.scheduledBadgeText)
            assertFalse(result.isScheduledPastDue)
        }

        @Test
        fun `returns null badge for SCHEDULED expense with no dueDate`() {
            val expense = Expense(
                id = "1",
                paymentStatus = PaymentStatus.SCHEDULED,
                dueDate = null
            )

            val result = mapper.map(expense)

            assertNull(result.scheduledBadgeText)
            assertFalse(result.isScheduledPastDue)
        }

        @Test
        fun `returns future badge text and isPastDue=false for upcoming scheduled expense`() {
            val futureDate = LocalDateTime.now().plusDays(10)
            val expense = Expense(
                id = "1",
                paymentStatus = PaymentStatus.SCHEDULED,
                dueDate = futureDate
            )

            val result = mapper.map(expense)

            assertNotNull(result.scheduledBadgeText)
            assertTrue(result.scheduledBadgeText!!.startsWith("Due on "))
            assertFalse(result.isScheduledPastDue)
        }

        @Test
        fun `returns due today badge and isPastDue=true for today scheduled expense`() {
            val todayNoon = LocalDate.now().atTime(12, 0)
            val expense = Expense(
                id = "1",
                paymentStatus = PaymentStatus.SCHEDULED,
                dueDate = todayNoon
            )

            val result = mapper.map(expense)

            assertEquals("Due today", result.scheduledBadgeText)
            assertTrue(result.isScheduledPastDue)
        }

        @Test
        fun `returns paid badge and isPastDue=true for past scheduled expense`() {
            val pastDate = LocalDateTime.now().minusDays(5)
            val expense = Expense(
                id = "1",
                paymentStatus = PaymentStatus.SCHEDULED,
                dueDate = pastDate
            )

            val result = mapper.map(expense)

            assertEquals("Paid", result.scheduledBadgeText)
            assertTrue(result.isScheduledPastDue)
        }
    }
}

