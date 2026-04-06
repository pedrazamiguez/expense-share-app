package es.pedrazamiguez.splittrip.features.expense.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.CurrencyUiModel
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.PaymentMethod
import es.pedrazamiguez.splittrip.domain.enums.PaymentStatus
import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.features.expense.presentation.extensions.toFundingSourceStringRes
import es.pedrazamiguez.splittrip.features.expense.presentation.extensions.toStringRes
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddExpenseOptionsUiMapperTest {

    private lateinit var mapper: AddExpenseOptionsUiMapper
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

    @BeforeEach
    fun setup() {
        resourceProvider = mockk(relaxed = true)

        // Stub all expense category string resources
        ExpenseCategory.entries.forEach { category ->
            every { resourceProvider.getString(category.toStringRes()) } returns category.name
        }

        // Stub all payment status string resources
        PaymentStatus.entries.forEach { status ->
            every { resourceProvider.getString(status.toStringRes()) } returns status.name
        }

        mapper = AddExpenseOptionsUiMapper(resourceProvider)
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
    inner class MapFundingSources {

        @BeforeEach
        fun stubFundingSourceStrings() {
            PayerType.entries
                .filter { it != PayerType.SUBUNIT }
                .forEach { payerType ->
                    every {
                        resourceProvider.getString(payerType.toFundingSourceStringRes())
                    } returns payerType.name
                }
        }

        @Test
        fun `excludes SUBUNIT from user-selectable funding sources`() {
            val result = mapper.mapFundingSources(PayerType.entries)

            val ids = result.map { it.id }
            assertTrue("SUBUNIT" !in ids)
        }

        @Test
        fun `includes GROUP and USER`() {
            val result = mapper.mapFundingSources(PayerType.entries)

            val ids = result.map { it.id }
            assertTrue("GROUP" in ids)
            assertTrue("USER" in ids)
        }

        @Test
        fun `maps display text from resource provider`() {
            every { resourceProvider.getString(PayerType.GROUP.toFundingSourceStringRes()) } returns "Group Pocket"
            every { resourceProvider.getString(PayerType.USER.toFundingSourceStringRes()) } returns "My Money"

            val result = mapper.mapFundingSources(listOf(PayerType.GROUP, PayerType.USER))

            assertEquals(2, result.size)
            assertEquals("GROUP", result[0].id)
            assertEquals("Group Pocket", result[0].displayText)
            assertEquals("USER", result[1].id)
            assertEquals("My Money", result[1].displayText)
        }

        @Test
        fun `returns empty list for empty input`() {
            val result = mapper.mapFundingSources(emptyList())
            assertTrue(result.isEmpty())
        }

        @Test
        fun `returns only GROUP when only GROUP and SUBUNIT provided`() {
            val result = mapper.mapFundingSources(listOf(PayerType.GROUP, PayerType.SUBUNIT))

            assertEquals(1, result.size)
            assertEquals("GROUP", result[0].id)
        }
    }
}
