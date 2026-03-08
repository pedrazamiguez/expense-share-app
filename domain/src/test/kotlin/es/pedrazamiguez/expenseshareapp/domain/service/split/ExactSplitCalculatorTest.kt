package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.exception.InvalidSplitException
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExactSplitCalculatorTest {

    private lateinit var calculator: ExactSplitCalculator

    @BeforeEach
    fun setUp() {
        calculator = ExactSplitCalculator()
    }

    @Nested
    inner class CalculateShares {

        @Test
        fun `returns splits when amounts sum to total`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 600L),
                ExpenseSplit(userId = "user2", amountCents = 400L)
            )

            val shares = calculator.calculateShares(
                1000L,
                listOf("user1", "user2"),
                existingSplits
            )

            assertEquals(2, shares.size)
            assertEquals(600L, shares[0].amountCents)
            assertEquals(400L, shares[1].amountCents)
            assertEquals(1000L, shares.sumOf { it.amountCents })
        }

        @Test
        fun `excludes excluded users from result`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 1000L),
                ExpenseSplit(userId = "user2", amountCents = 0L, isExcluded = true)
            )

            val shares = calculator.calculateShares(
                1000L,
                listOf("user1", "user2"),
                existingSplits
            )

            assertEquals(1, shares.size)
            assertEquals("user1", shares[0].userId)
            assertEquals(1000L, shares[0].amountCents)
        }
    }

    @Nested
    inner class Validation {

        @Test
        fun `throws InvalidSplitException when amounts do not sum to total`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 600L),
                ExpenseSplit(userId = "user2", amountCents = 300L)
            )

            val exception = assertThrows(InvalidSplitException::class.java) {
                calculator.calculateShares(
                    1000L,
                    listOf("user1", "user2"),
                    existingSplits
                )
            }
            assertEquals(SplitType.EXACT, exception.splitType)
        }

        @Test
        fun `throws InvalidSplitException for empty participants`() {
            val exception = assertThrows(InvalidSplitException::class.java) {
                calculator.calculateShares(1000L, emptyList())
            }
            assertEquals(SplitType.EXACT, exception.splitType)
        }
    }
}

