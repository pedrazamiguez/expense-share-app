package es.pedrazamiguez.splittrip.features.balance.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.foundation.ChartColors
import es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions.toIconVector
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.model.Expense
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CategorySpendingUiMapperTest {

    private val localeProvider = mockk<LocaleProvider>()
    private val resourceProvider = mockk<ResourceProvider>()
    private lateinit var mapper: CategorySpendingUiMapper

    @BeforeEach
    fun setup() {
        every { localeProvider.getCurrentLocale() } returns Locale.US
        every { resourceProvider.getString(any()) } answers { "Translated Category" }
        mapper = CategorySpendingUiMapper(localeProvider, resourceProvider)
    }

    @Test
    fun `mapExpenses maps positive expenses and sorts them by category amount descending`() {
        val expenses = listOf(
            mockk<Expense> {
                every { groupAmount } returns 500L
                every { category } returns ExpenseCategory.FOOD
            },
            mockk<Expense> {
                every { groupAmount } returns 1500L
                every { category } returns ExpenseCategory.LODGING
            },
            mockk<Expense> {
                every { groupAmount } returns -200L // Negative, should be filtered out
                every { category } returns ExpenseCategory.TRANSPORT
            },
            mockk<Expense> {
                every { groupAmount } returns 0L // Zero, should be filtered out
                every { category } returns ExpenseCategory.TRANSPORT
            },
            mockk<Expense> {
                every { groupAmount } returns 1000L
                every { category } returns ExpenseCategory.FOOD
            }
        )

        // Total positive = 500 + 1500 + 1000 = 3000
        // FOOD = 1500, LODGING = 1500

        val result = mapper.mapExpenses(expenses, "USD")

        assertEquals(2, result.size)

        // Both are 1500, so 50% each
        assertEquals("Translated Category", result[0].categoryName)
        assertEquals(ExpenseCategory.FOOD.toIconVector(), result[0].categoryIcon)
        assertEquals("$15.00", result[0].formattedAmount)
        assertEquals(0.5f, result[0].progress)
        assertEquals(ChartColors[0], result[0].color)

        assertEquals("Translated Category", result[1].categoryName)
        assertEquals(ExpenseCategory.LODGING.toIconVector(), result[1].categoryIcon)
        assertEquals("$15.00", result[1].formattedAmount)
        assertEquals(0.5f, result[1].progress)
        assertEquals(ChartColors[1], result[1].color)
    }

    @Test
    fun `mapExpenses returns empty list when no positive expenses`() {
        val expenses = listOf(
            mockk<Expense> {
                every { groupAmount } returns -200L
                every { category } returns ExpenseCategory.TRANSPORT
            },
            mockk<Expense> {
                every { groupAmount } returns 0L
                every { category } returns ExpenseCategory.FOOD
            }
        )

        val result = mapper.mapExpenses(expenses, "USD")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `formatTotalAmount formats correctly when total is positive`() {
        val result = mapper.formatTotalAmount(2500L, "USD")
        assertEquals("$25.00", result)
    }

    @Test
    fun `formatTotalAmount returns empty string when total is zero or negative`() {
        assertEquals("", mapper.formatTotalAmount(0L, "USD"))
        assertEquals("", mapper.formatTotalAmount(-100L, "USD"))
    }
}
