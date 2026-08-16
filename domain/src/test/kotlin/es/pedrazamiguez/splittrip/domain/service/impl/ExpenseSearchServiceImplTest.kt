package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.service.ExpenseSearchService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExpenseSearchServiceImplTest {

    private lateinit var service: ExpenseSearchService

    @BeforeEach
    fun setUp() {
        service = ExpenseSearchServiceImpl()
    }

    @Nested
    @DisplayName("Blank or Empty Query")
    inner class BlankQuery {

        @Test
        fun `empty query returns original list unmodified`() {
            val expenses = listOf(
                Expense(id = "1", title = "Dinner"),
                Expense(id = "2", title = "Taxi")
            )

            val result = service.search(expenses, "")

            assertEquals(expenses, result)
        }

        @Test
        fun `whitespace only query returns original list unmodified`() {
            val expenses = listOf(
                Expense(id = "1", title = "Dinner"),
                Expense(id = "2", title = "Taxi")
            )

            val result = service.search(expenses, "   ")

            assertEquals(expenses, result)
        }

        @Test
        fun `empty expenses list returns empty list`() {
            val result = service.search(emptyList(), "test")

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("Matching by Title")
    inner class MatchTitle {

        @Test
        fun `matches exact title case-insensitively`() {
            val dinner = Expense(id = "1", title = "Paella Dinner")
            val taxi = Expense(id = "2", title = "Airport Taxi")
            val expenses = listOf(dinner, taxi)

            val result = service.search(expenses, "paella")

            assertEquals(listOf(dinner), result)
        }

        @Test
        fun `matches partial title case-insensitively with mixed case`() {
            val dinner = Expense(id = "1", title = "Sushi Dinner at Tokyo")
            val lunch = Expense(id = "2", title = "Quick lunch")
            val museum = Expense(id = "3", title = "Museum ticket")
            val expenses = listOf(dinner, lunch, museum)

            val result = service.search(expenses, "DINNER")

            assertEquals(listOf(dinner), result)
        }

        @Test
        fun `trims query whitespace before searching title`() {
            val dinner = Expense(id = "1", title = "Hotel Stay")
            val taxi = Expense(id = "2", title = "Taxi Ride")
            val expenses = listOf(dinner, taxi)

            val result = service.search(expenses, "  Hotel  ")

            assertEquals(listOf(dinner), result)
        }
    }

    @Nested
    @DisplayName("Matching by Notes")
    inner class MatchNotes {

        @Test
        fun `matches notes case-insensitively`() {
            val dinner = Expense(id = "1", title = "Food", notes = "Seafood paella in Valencia")
            val coffee = Expense(id = "2", title = "Coffee", notes = "Espresso bar")
            val expenses = listOf(dinner, coffee)

            val result = service.search(expenses, "valencia")

            assertEquals(listOf(dinner), result)
        }

        @Test
        fun `matches either title or notes across multiple expenses`() {
            val e1 = Expense(id = "1", title = "Valencia Train", notes = "Renfe booking")
            val e2 = Expense(id = "2", title = "Dinner", notes = "Paella in Valencia")
            val e3 = Expense(id = "3", title = "Museum", notes = "Art gallery")
            val expenses = listOf(e1, e2, e3)

            val result = service.search(expenses, "valencia")

            assertEquals(listOf(e1, e2), result)
        }

        @Test
        fun `handles null notes safely without throwing exception`() {
            val e1 = Expense(id = "1", title = "Bus", notes = null)
            val e2 = Expense(id = "2", title = "Metro", notes = "Subway ticket")
            val expenses = listOf(e1, e2)

            val result = service.search(expenses, "subway")

            assertEquals(listOf(e2), result)
        }

        @Test
        fun `null notes expense still matches when title matches query`() {
            val e1 = Expense(id = "1", title = "Ferry Ride", notes = null)
            val e2 = Expense(id = "2", title = "Bus Ride", notes = null)
            val expenses = listOf(e1, e2)

            val result = service.search(expenses, "ferry")

            assertEquals(listOf(e1), result)
        }
    }

    @Nested
    @DisplayName("No Matches and Ordering")
    inner class NoMatchesAndOrdering {

        @Test
        fun `returns empty list when query does not match any title or notes`() {
            val e1 = Expense(id = "1", title = "Dinner", notes = "Pizza")
            val e2 = Expense(id = "2", title = "Taxi", notes = "Airport")
            val expenses = listOf(e1, e2)

            val result = service.search(expenses, "NonExistentKeyword")

            assertTrue(result.isEmpty())
        }

        @Test
        fun `preserves original relative order of matching expenses`() {
            val e1 = Expense(id = "1", title = "Breakfast", notes = "Coffee and croissant")
            val e2 = Expense(id = "2", title = "Lunch", notes = "Sandwich")
            val e3 = Expense(id = "3", title = "Dinner", notes = "Steak with coffee afterwards")
            val expenses = listOf(e1, e2, e3)

            val result = service.search(expenses, "coffee")

            assertEquals(listOf(e1, e3), result)
        }
    }
}
