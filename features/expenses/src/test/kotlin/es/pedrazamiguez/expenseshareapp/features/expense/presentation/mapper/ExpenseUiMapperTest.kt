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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
}

