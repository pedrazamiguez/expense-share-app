package es.pedrazamiguez.splittrip.features.expense.presentation.extensions

import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Bed
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Car
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Category
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Coin
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MasksTheater
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ReceiptRefund
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Run
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Shield
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShoppingBag
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ToolsKitchen2
import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.features.expense.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
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
            assertSame(TablerIcons.Outline.Coin, ExpenseCategory.CONTRIBUTION.toIconVector())
        }

        @Test
        fun `REFUND maps to ReceiptRefund icon`() {
            assertSame(TablerIcons.Outline.ReceiptRefund, ExpenseCategory.REFUND.toIconVector())
        }

        @Test
        fun `TRANSPORT maps to Car icon`() {
            assertSame(TablerIcons.Outline.Car, ExpenseCategory.TRANSPORT.toIconVector())
        }

        @Test
        fun `FOOD maps to ToolsKitchen2 icon`() {
            assertSame(TablerIcons.Outline.ToolsKitchen2, ExpenseCategory.FOOD.toIconVector())
        }

        @Test
        fun `LODGING maps to Bed icon`() {
            assertSame(TablerIcons.Outline.Bed, ExpenseCategory.LODGING.toIconVector())
        }

        @Test
        fun `ACTIVITIES maps to Run icon`() {
            assertSame(TablerIcons.Outline.Run, ExpenseCategory.ACTIVITIES.toIconVector())
        }

        @Test
        fun `INSURANCE maps to Shield icon`() {
            assertSame(TablerIcons.Outline.Shield, ExpenseCategory.INSURANCE.toIconVector())
        }

        @Test
        fun `ENTERTAINMENT maps to MasksTheater icon`() {
            assertSame(TablerIcons.Outline.MasksTheater, ExpenseCategory.ENTERTAINMENT.toIconVector())
        }

        @Test
        fun `SHOPPING maps to ShoppingBag icon`() {
            assertSame(TablerIcons.Outline.ShoppingBag, ExpenseCategory.SHOPPING.toIconVector())
        }

        @Test
        fun `OTHER maps to Category icon`() {
            assertSame(TablerIcons.Outline.Category, ExpenseCategory.OTHER.toIconVector())
        }

        @Test
        fun `all categories return non-null icon vectors`() {
            ExpenseCategory.entries.forEach { category ->
                assertNotNull(category.toIconVector(), "Icon for $category should not be null")
            }
        }

        @Test
        fun `icon vectors are cached — same reference returned on repeated calls`() {
            // TablerIcons backing fields cache the built ImageVector after first access;
            // assertSame verifies referential identity, not just structural equality.
            assertSame(
                ExpenseCategory.CONTRIBUTION.toIconVector(),
                ExpenseCategory.CONTRIBUTION.toIconVector()
            )
        }
    }
}
