package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.exception.InvalidSplitException
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EqualSplitCalculatorTest {

    private lateinit var calculator: EqualSplitCalculator

    @BeforeEach
    fun setUp() {
        calculator = EqualSplitCalculator(ExpenseCalculatorService())
    }

    @Nested
    inner class CalculateShares {

        @Test
        fun `splits 1000 cents equally among 2 users`() {
            val shares = calculator.calculateShares(1000L, listOf("user1", "user2"))

            assertEquals(2, shares.size)
            assertEquals(500L, shares[0].amountCents)
            assertEquals(500L, shares[1].amountCents)
            assertEquals(1000L, shares.sumOf { it.amountCents })
        }

        @Test
        fun `splits 1000 cents equally among 3 users with remainder`() {
            val shares = calculator.calculateShares(1000L, listOf("user1", "user2", "user3"))

            assertEquals(3, shares.size)
            // 1000 / 3 = 333.33... → first user gets 334, others 333
            assertEquals(334L, shares[0].amountCents)
            assertEquals(333L, shares[1].amountCents)
            assertEquals(333L, shares[2].amountCents)
            assertEquals(1000L, shares.sumOf { it.amountCents })
        }

        @Test
        fun `splits 1 cent among 3 users`() {
            val shares = calculator.calculateShares(1L, listOf("user1", "user2", "user3"))

            assertEquals(3, shares.size)
            assertEquals(1L, shares[0].amountCents)
            assertEquals(0L, shares[1].amountCents)
            assertEquals(0L, shares[2].amountCents)
            assertEquals(1L, shares.sumOf { it.amountCents })
        }

        @Test
        fun `splits among single user returns full amount`() {
            val shares = calculator.calculateShares(5000L, listOf("user1"))

            assertEquals(1, shares.size)
            assertEquals(5000L, shares[0].amountCents)
        }

        @Test
        fun `user IDs are correctly assigned`() {
            val shares = calculator.calculateShares(600L, listOf("alice", "bob"))

            assertEquals("alice", shares[0].userId)
            assertEquals("bob", shares[1].userId)
        }
    }

    @Nested
    inner class Validation {

        @Test
        fun `throws InvalidSplitException for empty participants`() {
            val exception = assertThrows(InvalidSplitException::class.java) {
                calculator.calculateShares(1000L, emptyList())
            }
            assertEquals(SplitType.EQUAL, exception.splitType)
        }
    }
}
