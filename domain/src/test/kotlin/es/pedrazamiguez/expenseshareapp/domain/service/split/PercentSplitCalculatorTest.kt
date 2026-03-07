package es.pedrazamiguez.expenseshareapp.domain.service.split

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.exception.InvalidSplitException
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PercentSplitCalculatorTest {

    private lateinit var calculator: PercentSplitCalculator

    @BeforeEach
    fun setUp() {
        calculator = PercentSplitCalculator()
    }

    @Nested
    inner class CalculateShares {

        @Test
        fun `splits 50-50 correctly`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 0, percentage = BigDecimal("50")),
                ExpenseSplit(userId = "user2", amountCents = 0, percentage = BigDecimal("50"))
            )

            val shares = calculator.calculateShares(
                1000L,
                listOf("user1", "user2"),
                existingSplits
            )

            assertEquals(2, shares.size)
            assertEquals(500L, shares[0].amountCents)
            assertEquals(500L, shares[1].amountCents)
            assertEquals(1000L, shares.sumOf { it.amountCents })
        }

        @Test
        fun `splits 70-30 correctly`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 0, percentage = BigDecimal("70")),
                ExpenseSplit(userId = "user2", amountCents = 0, percentage = BigDecimal("30"))
            )

            val shares = calculator.calculateShares(
                1000L,
                listOf("user1", "user2"),
                existingSplits
            )

            assertEquals(2, shares.size)
            assertEquals(700L, shares[0].amountCents)
            assertEquals(300L, shares[1].amountCents)
            assertEquals(1000L, shares.sumOf { it.amountCents })
        }

        @Test
        fun `handles remainder distribution for 33-33-34 split`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 0, percentage = BigDecimal("33.33")),
                ExpenseSplit(userId = "user2", amountCents = 0, percentage = BigDecimal("33.33")),
                ExpenseSplit(userId = "user3", amountCents = 0, percentage = BigDecimal("33.34"))
            )

            val shares = calculator.calculateShares(
                1000L,
                listOf("user1", "user2", "user3"),
                existingSplits
            )

            assertEquals(3, shares.size)
            // Total must always equal original amount (conservation invariant)
            assertEquals(1000L, shares.sumOf { it.amountCents })
        }

        @Test
        fun `100 percent to single user`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 0, percentage = BigDecimal("100"))
            )

            val shares = calculator.calculateShares(
                5000L,
                listOf("user1"),
                existingSplits
            )

            assertEquals(1, shares.size)
            assertEquals(5000L, shares[0].amountCents)
        }

        @Test
        fun `preserves percentage in returned splits`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 0, percentage = BigDecimal("60")),
                ExpenseSplit(userId = "user2", amountCents = 0, percentage = BigDecimal("40"))
            )

            val shares = calculator.calculateShares(
                1000L,
                listOf("user1", "user2"),
                existingSplits
            )

            assertEquals(BigDecimal("60"), shares[0].percentage)
            assertEquals(BigDecimal("40"), shares[1].percentage)
        }
    }

    @Nested
    inner class Validation {

        @Test
        fun `throws InvalidSplitException when percentages do not sum to 100`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 0, percentage = BigDecimal("60")),
                ExpenseSplit(userId = "user2", amountCents = 0, percentage = BigDecimal("30"))
            )

            val exception = assertThrows(InvalidSplitException::class.java) {
                calculator.calculateShares(
                    1000L,
                    listOf("user1", "user2"),
                    existingSplits
                )
            }
            assertEquals(SplitType.PERCENT, exception.splitType)
        }

        @Test
        fun `throws InvalidSplitException for empty participants`() {
            val exception = assertThrows(InvalidSplitException::class.java) {
                calculator.calculateShares(1000L, emptyList())
            }
            assertEquals(SplitType.PERCENT, exception.splitType)
        }

        @Test
        fun `accepts percentages summing to 100 within tolerance`() {
            val existingSplits = listOf(
                ExpenseSplit(userId = "user1", amountCents = 0, percentage = BigDecimal("33.33")),
                ExpenseSplit(userId = "user2", amountCents = 0, percentage = BigDecimal("33.33")),
                ExpenseSplit(userId = "user3", amountCents = 0, percentage = BigDecimal("33.34"))
            )

            // Should not throw — sum is 100.00
            val shares = calculator.calculateShares(
                1000L,
                listOf("user1", "user2", "user3"),
                existingSplits
            )
            assertEquals(3, shares.size)
        }
    }
}


