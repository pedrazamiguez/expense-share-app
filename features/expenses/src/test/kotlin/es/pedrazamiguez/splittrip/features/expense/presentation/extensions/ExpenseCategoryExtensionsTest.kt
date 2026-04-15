package es.pedrazamiguez.splittrip.features.expense.presentation.extensions

import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Coin
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.features.expense.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExpenseCategoryExtensionsTest {

    @Nested
    inner class StringResMapping {

        @Test
        fun `CONTRIBUTION maps to contribution string resource`() {
            assertEquals(R.string.expense_category_contribution, ExpenseCategory.CONTRIBUTION.toStringRes())
        }

        @Test
        fun `REFUND maps to refund string resource`() {
            assertEquals(R.string.expense_category_refund, ExpenseCategory.REFUND.toStringRes())
        }

        @Test
        fun `TRANSPORT maps to transport string resource`() {
            assertEquals(R.string.expense_category_transport, ExpenseCategory.TRANSPORT.toStringRes())
        }

        @Test
        fun `FOOD maps to food string resource`() {
            assertEquals(R.string.expense_category_food, ExpenseCategory.FOOD.toStringRes())
        }

        @Test
        fun `LODGING maps to lodging string resource`() {
            assertEquals(R.string.expense_category_lodging, ExpenseCategory.LODGING.toStringRes())
        }

        @Test
        fun `ACTIVITIES maps to activities string resource`() {
            assertEquals(R.string.expense_category_activities, ExpenseCategory.ACTIVITIES.toStringRes())
        }

        @Test
        fun `INSURANCE maps to insurance string resource`() {
            assertEquals(R.string.expense_category_insurance, ExpenseCategory.INSURANCE.toStringRes())
        }

        @Test
        fun `ENTERTAINMENT maps to entertainment string resource`() {
            assertEquals(R.string.expense_category_entertainment, ExpenseCategory.ENTERTAINMENT.toStringRes())
        }

        @Test
        fun `SHOPPING maps to shopping string resource`() {
            assertEquals(R.string.expense_category_shopping, ExpenseCategory.SHOPPING.toStringRes())
        }

        @Test
        fun `OTHER maps to other string resource`() {
            assertEquals(R.string.expense_category_other, ExpenseCategory.OTHER.toStringRes())
        }

        @Test
        fun `all categories map to distinct string resources`() {
            val resIds = ExpenseCategory.entries.map { it.toStringRes() }
            assertEquals(resIds.size, resIds.toSet().size)
        }
    }

    @Nested
    inner class IconVectorMapping {

        @Test
        fun `CONTRIBUTION maps to Coin icon`() {
            assertNotNull(ExpenseCategory.CONTRIBUTION.toIconVector())
            assertEquals("Outline.Coin", ExpenseCategory.CONTRIBUTION.toIconVector().name)
        }

        @Test
        fun `REFUND maps to ReceiptRefund icon`() {
            assertNotNull(ExpenseCategory.REFUND.toIconVector())
            assertEquals("Outline.ReceiptRefund", ExpenseCategory.REFUND.toIconVector().name)
        }

        @Test
        fun `TRANSPORT maps to Car icon`() {
            assertNotNull(ExpenseCategory.TRANSPORT.toIconVector())
            assertEquals("Outline.Car", ExpenseCategory.TRANSPORT.toIconVector().name)
        }

        @Test
        fun `FOOD maps to ToolsKitchen2 icon`() {
            assertNotNull(ExpenseCategory.FOOD.toIconVector())
            assertEquals("Outline.ToolsKitchen2", ExpenseCategory.FOOD.toIconVector().name)
        }

        @Test
        fun `LODGING maps to Bed icon`() {
            assertNotNull(ExpenseCategory.LODGING.toIconVector())
            assertEquals("Outline.Bed", ExpenseCategory.LODGING.toIconVector().name)
        }

        @Test
        fun `ACTIVITIES maps to Run icon`() {
            assertNotNull(ExpenseCategory.ACTIVITIES.toIconVector())
            assertEquals("Outline.Run", ExpenseCategory.ACTIVITIES.toIconVector().name)
        }

        @Test
        fun `INSURANCE maps to Shield icon`() {
            assertNotNull(ExpenseCategory.INSURANCE.toIconVector())
            assertEquals("Outline.Shield", ExpenseCategory.INSURANCE.toIconVector().name)
        }

        @Test
        fun `ENTERTAINMENT maps to MasksTheater icon`() {
            assertNotNull(ExpenseCategory.ENTERTAINMENT.toIconVector())
            assertEquals("Outline.MasksTheater", ExpenseCategory.ENTERTAINMENT.toIconVector().name)
        }

        @Test
        fun `SHOPPING maps to ShoppingBag icon`() {
            assertNotNull(ExpenseCategory.SHOPPING.toIconVector())
            assertEquals("Outline.ShoppingBag", ExpenseCategory.SHOPPING.toIconVector().name)
        }

        @Test
        fun `OTHER maps to Category icon`() {
            assertNotNull(ExpenseCategory.OTHER.toIconVector())
            assertEquals("Outline.Category", ExpenseCategory.OTHER.toIconVector().name)
        }

        @Test
        fun `all categories return non-null icon vectors`() {
            ExpenseCategory.entries.forEach { category ->
                assertNotNull(category.toIconVector(), "Icon for $category should not be null")
            }
        }

        @Test
        fun `icon vectors are cached — same reference returned on repeated calls`() {
            // TablerIcons backing fields cache the built ImageVector after first access
            assertNotNull(TablerIcons.Outline.Coin)
            assertEquals(
                ExpenseCategory.CONTRIBUTION.toIconVector(),
                ExpenseCategory.CONTRIBUTION.toIconVector()
            )
        }
    }
}
