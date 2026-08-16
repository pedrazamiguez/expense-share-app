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
    @DisplayName("Matching by Vendor")
    inner class MatchVendor {

        @Test
        fun `matches vendor case-insensitively`() {
            val dinner = Expense(id = "1", title = "Dinner", vendor = "Mercadona")
            val coffee = Expense(id = "2", title = "Coffee", vendor = "Starbucks")
            val expenses = listOf(dinner, coffee)

            val result = service.search(expenses, "mercadona")

            assertEquals(listOf(dinner), result)
        }

        @Test
        fun `matches partial vendor name with mixed case`() {
            val hotel = Expense(id = "1", title = "Accommodation", vendor = "Chengdu Jinjiang Hotel")
            val flight = Expense(id = "2", title = "Flight", vendor = "Air China")
            val expenses = listOf(hotel, flight)

            val result = service.search(expenses, "CHENGDU")

            assertEquals(listOf(hotel), result)
        }

        @Test
        fun `handles null vendor safely without throwing exception`() {
            val e1 = Expense(id = "1", title = "Bus", vendor = null)
            val e2 = Expense(id = "2", title = "Groceries", vendor = "Carrefour")
            val expenses = listOf(e1, e2)

            val result = service.search(expenses, "carrefour")

            assertEquals(listOf(e2), result)
        }

        @Test
        fun `matches across title, vendor, and notes in different expenses`() {
            val e1 = Expense(id = "1", title = "Chengdu Flight", vendor = "Iberia", notes = "Direct flight")
            val e2 = Expense(id = "2", title = "Hotel Stay", vendor = "Chengdu Inn", notes = "Near downtown")
            val e3 = Expense(id = "3", title = "Dinner", vendor = "Local Restaurant", notes = "Chengdu hotpot")
            val e4 = Expense(id = "4", title = "Taxi", vendor = "Didi", notes = "Airport transfer")
            val expenses = listOf(e1, e2, e3, e4)

            val result = service.search(expenses, "chengdu")

            assertEquals(listOf(e1, e2, e3), result)
        }
    }

    @Nested
    @DisplayName("No Matches and Ordering")
    inner class NoMatchesAndOrdering {

        @Test
        fun `returns empty list when query does not match any title, vendor, or notes`() {
            val e1 = Expense(id = "1", title = "Dinner", vendor = "Pizzeria", notes = "Pizza")
            val e2 = Expense(id = "2", title = "Taxi", vendor = "Uber", notes = "Airport")
            val expenses = listOf(e1, e2)

            val result = service.search(expenses, "NonExistentKeyword")

            assertTrue(result.isEmpty())
        }

        @Test
        fun `preserves original relative order of matching expenses`() {
            val e1 = Expense(id = "1", title = "Breakfast", notes = "Coffee and croissant")
            val e2 = Expense(id = "2", title = "Lunch", vendor = "Cafe", notes = "Sandwich")
            val e3 = Expense(id = "3", title = "Dinner", notes = "Steak with coffee afterwards")
            val expenses = listOf(e1, e2, e3)

            val result = service.search(expenses, "coffee")

            assertEquals(listOf(e1, e3), result)
        }
    }
}
