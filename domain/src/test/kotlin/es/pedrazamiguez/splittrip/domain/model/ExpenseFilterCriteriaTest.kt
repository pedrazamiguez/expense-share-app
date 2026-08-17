package es.pedrazamiguez.splittrip.domain.model

import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ExpenseFilterCriteria")
class ExpenseFilterCriteriaTest {

    @Nested
    @DisplayName("Filter status properties")
    inner class FilterStatusProperties {

        @Test
        fun `default criteria is not active and has zero active count`() {
            val criteria = ExpenseFilterCriteria()
            assertFalse(criteria.isActive)
            assertFalse(criteria.isCategoryFiltered)
            assertFalse(criteria.isMemberFiltered)
            assertFalse(criteria.isDateFiltered)
            assertFalse(criteria.isSearchFiltered)
            assertEquals(0, criteria.activeFilterCount)
        }

        @Test
        fun `category filter flags active status`() {
            val criteria = ExpenseFilterCriteria(selectedCategories = setOf(ExpenseCategory.FOOD))
            assertTrue(criteria.isActive)
            assertTrue(criteria.isCategoryFiltered)
            assertEquals(1, criteria.activeFilterCount)
        }

        @Test
        fun `subcategory filter flags active status`() {
            val criteria = ExpenseFilterCriteria(selectedSubcategories = setOf(ExpenseSubcategory.RESTAURANT))
            assertTrue(criteria.isActive)
            assertTrue(criteria.isCategoryFiltered)
            assertEquals(1, criteria.activeFilterCount)
        }

        @Test
        fun `member filter flags active status`() {
            val criteria = ExpenseFilterCriteria(selectedMemberIds = setOf("user_1"))
            assertTrue(criteria.isActive)
            assertTrue(criteria.isMemberFiltered)
            assertEquals(1, criteria.activeFilterCount)
        }

        @Test
        fun `date filter flags active status`() {
            val criteria = ExpenseFilterCriteria(startDate = LocalDate.now())
            assertTrue(criteria.isActive)
            assertTrue(criteria.isDateFiltered)
            assertEquals(1, criteria.activeFilterCount)
        }

        @Test
        fun `search query flags active status without incrementing activeFilterCount`() {
            val criteria = ExpenseFilterCriteria(searchQuery = "dinner")
            assertTrue(criteria.isActive)
            assertTrue(criteria.isSearchFiltered)
            assertEquals(0, criteria.activeFilterCount)
        }

        @Test
        fun `multi-criteria computes correct activeFilterCount`() {
            val criteria = ExpenseFilterCriteria(
                searchQuery = "dinner",
                selectedCategories = setOf(ExpenseCategory.FOOD),
                selectedMemberIds = setOf("user_1"),
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(1)
            )
            assertTrue(criteria.isActive)
            assertEquals(3, criteria.activeFilterCount)
        }
    }

    @Nested
    @DisplayName("Reset methods")
    inner class ResetMethods {

        @Test
        fun `clearNonSearchFilters preserves search query and resets all dimensions`() {
            val criteria = ExpenseFilterCriteria(
                searchQuery = "dinner",
                selectedCategories = setOf(ExpenseCategory.FOOD),
                selectedSubcategories = setOf(ExpenseSubcategory.RESTAURANT),
                selectedMemberIds = setOf("user_1"),
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(2)
            )
            val cleared = criteria.clearNonSearchFilters()
            assertEquals("dinner", cleared.searchQuery)
            assertFalse(cleared.isCategoryFiltered)
            assertFalse(cleared.isMemberFiltered)
            assertFalse(cleared.isDateFiltered)
            assertEquals(0, cleared.activeFilterCount)
        }

        @Test
        fun `clearAll returns clean default criteria`() {
            val criteria = ExpenseFilterCriteria(
                searchQuery = "dinner",
                selectedCategories = setOf(ExpenseCategory.FOOD)
            )
            val cleared = criteria.clearAll()
            assertEquals(ExpenseFilterCriteria(), cleared)
        }
    }

    @Nested
    @DisplayName("Serialization")
    inner class Serialization {

        @Test
        fun `serializes and deserializes correctly via Java Serializable`() {
            val original = ExpenseFilterCriteria(
                searchQuery = "flight ticket",
                selectedCategories = setOf(ExpenseCategory.TRANSPORT),
                selectedSubcategories = setOf(ExpenseSubcategory.INTERNATIONAL_FLIGHT),
                selectedMemberIds = setOf("user_1", "user_2"),
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 15)
            )

            val byteArrayOutputStream = ByteArrayOutputStream()
            ObjectOutputStream(byteArrayOutputStream).use { it.writeObject(original) }

            val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            val deserialized = ObjectInputStream(byteArrayInputStream).use {
                it.readObject() as ExpenseFilterCriteria
            }

            assertEquals(original, deserialized)
        }
    }
}
