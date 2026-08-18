package es.pedrazamiguez.splittrip.features.balance.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions.toIconVector
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory
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
                every { subcategory } returns ExpenseSubcategory.CAFE_BREAKFAST
            },
            mockk<Expense> {
                every { groupAmount } returns 1500L
                every { category } returns ExpenseCategory.LODGING
                every { subcategory } returns ExpenseSubcategory.HOTEL
            },
            mockk<Expense> {
                every { groupAmount } returns -200L // Negative, should be filtered out
                every { category } returns ExpenseCategory.TRANSPORT
                every { subcategory } returns ExpenseSubcategory.TRAIN
            },
            mockk<Expense> {
                every { groupAmount } returns 0L // Zero, should be filtered out
                every { category } returns ExpenseCategory.TRANSPORT
                every { subcategory } returns ExpenseSubcategory.BUS
            },
            mockk<Expense> {
                every { groupAmount } returns 1000L
                every { category } returns ExpenseCategory.FOOD
                every { subcategory } returns ExpenseSubcategory.RESTAURANT
            }
        )

        // Total positive = 500 + 1500 + 1000 = 3000
        // FOOD = 1500, LODGING = 1500

        val result = mapper.mapExpenses(expenses, "USD")

        assertEquals(2, result.size)

        // FOOD category: RESTAURANT (1000) + CAFE_BREAKFAST (500)
        val foodCategory = result.first { it.categoryIcon == ExpenseCategory.FOOD.toIconVector() }
        assertEquals("Translated Category", foodCategory.categoryName)
        assertEquals("$15.00", foodCategory.formattedAmount)
        assertEquals(0.5f, foodCategory.progress)
        assertEquals(2, foodCategory.subcategories.size)

        val foodSub0 = foodCategory.subcategories[0]
        assertEquals("Translated Category", foodSub0.subcategoryName)
        assertEquals(ExpenseSubcategory.RESTAURANT.toIconVector(), foodSub0.subcategoryIcon)
        assertEquals("$10.00", foodSub0.formattedAmount)
        assertEquals(67, foodSub0.percentageOfCategory)
        assertEquals(1000L, foodSub0.rawAmountCents)

        val foodSub1 = foodCategory.subcategories[1]
        assertEquals("Translated Category", foodSub1.subcategoryName)
        assertEquals(ExpenseSubcategory.CAFE_BREAKFAST.toIconVector(), foodSub1.subcategoryIcon)
        assertEquals("$5.00", foodSub1.formattedAmount)
        assertEquals(33, foodSub1.percentageOfCategory)
        assertEquals(500L, foodSub1.rawAmountCents)

        // LODGING category: HOTEL (1500)
        val lodgingCategory = result.first { it.categoryIcon == ExpenseCategory.LODGING.toIconVector() }
        assertEquals("Translated Category", lodgingCategory.categoryName)
        assertEquals("$15.00", lodgingCategory.formattedAmount)
        assertEquals(0.5f, lodgingCategory.progress)
        assertEquals(1, lodgingCategory.subcategories.size)

        val lodgingSub = lodgingCategory.subcategories[0]
        assertEquals(ExpenseSubcategory.HOTEL.toIconVector(), lodgingSub.subcategoryIcon)
        assertEquals("$15.00", lodgingSub.formattedAmount)
        assertEquals(100, lodgingSub.percentageOfCategory)
        assertEquals(1500L, lodgingSub.rawAmountCents)
    }

    @Test
    fun `mapExpenses handles multiple subcategories with UNSPECIFIED correctly`() {
        val expenses = listOf(
            mockk<Expense> {
                every { groupAmount } returns 800L
                every { category } returns ExpenseCategory.TRANSPORT
                every { subcategory } returns ExpenseSubcategory.TRAIN
            },
            mockk<Expense> {
                every { groupAmount } returns 200L
                every { category } returns ExpenseCategory.TRANSPORT
                every { subcategory } returns ExpenseSubcategory.UNSPECIFIED
            }
        )

        val result = mapper.mapExpenses(expenses, "EUR")

        assertEquals(1, result.size)
        val transportCategory = result[0]
        assertEquals(2, transportCategory.subcategories.size)

        assertEquals(ExpenseSubcategory.TRAIN.toIconVector(), transportCategory.subcategories[0].subcategoryIcon)
        assertEquals(80, transportCategory.subcategories[0].percentageOfCategory)
        assertEquals(800L, transportCategory.subcategories[0].rawAmountCents)

        assertEquals(ExpenseSubcategory.UNSPECIFIED.toIconVector(), transportCategory.subcategories[1].subcategoryIcon)
        assertEquals(20, transportCategory.subcategories[1].percentageOfCategory)
        assertEquals(200L, transportCategory.subcategories[1].rawAmountCents)
    }

    @Test
    fun `mapExpenses returns empty list when no positive expenses`() {
        val expenses = listOf(
            mockk<Expense> {
                every { groupAmount } returns -200L
                every { category } returns ExpenseCategory.TRANSPORT
                every { subcategory } returns ExpenseSubcategory.TRAIN
            },
            mockk<Expense> {
                every { groupAmount } returns 0L
                every { category } returns ExpenseCategory.FOOD
                every { subcategory } returns ExpenseSubcategory.RESTAURANT
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
