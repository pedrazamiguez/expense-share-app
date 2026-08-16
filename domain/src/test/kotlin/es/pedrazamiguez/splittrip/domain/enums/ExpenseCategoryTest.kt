package es.pedrazamiguez.splittrip.domain.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ExpenseCategoryTest {

    @Nested
    inner class FromString {

        @ParameterizedTest
        @EnumSource(ExpenseCategory::class)
        fun `resolves all enum entries by exact name`(category: ExpenseCategory) {
            assertEquals(category, ExpenseCategory.fromString(category.name))
        }

        @Test
        fun `resolves case-insensitive input`() {
            assertEquals(ExpenseCategory.TRANSPORT, ExpenseCategory.fromString("transport"))
            assertEquals(ExpenseCategory.FOOD, ExpenseCategory.fromString("food"))
            assertEquals(ExpenseCategory.OTHER, ExpenseCategory.fromString("other"))
        }

        @Test
        fun `throws IllegalArgumentException for unknown category`() {
            assertThrows(IllegalArgumentException::class.java) {
                ExpenseCategory.fromString("NONEXISTENT")
            }
        }

        @Test
        fun `throws IllegalArgumentException for removed categories CONTRIBUTION and REFUND`() {
            assertThrows(IllegalArgumentException::class.java) {
                ExpenseCategory.fromString("CONTRIBUTION")
            }
            assertThrows(IllegalArgumentException::class.java) {
                ExpenseCategory.fromString("REFUND")
            }
        }
    }
}
