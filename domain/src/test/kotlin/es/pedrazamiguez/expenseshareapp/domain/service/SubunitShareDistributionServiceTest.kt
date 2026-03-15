package es.pedrazamiguez.expenseshareapp.domain.service

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SubunitShareDistributionService")
class SubunitShareDistributionServiceTest {

    private lateinit var service: SubunitShareDistributionService

    @BeforeEach
    fun setUp() {
        service = SubunitShareDistributionService()
    }

    @Nested
    @DisplayName("distributeEvenly")
    inner class DistributeEvenly {

        @Test
        fun `distributes evenly among 2 members`() {
            val result = service.distributeEvenly(listOf("user-1", "user-2"))

            assertEquals(2, result.size)
            assertEquals(0, BigDecimal("0.5").compareTo(result["user-1"]))
            assertEquals(0, BigDecimal("0.5").compareTo(result["user-2"]))
        }

        @Test
        fun `distributes evenly among 3 members`() {
            val result = service.distributeEvenly(listOf("user-1", "user-2", "user-3"))

            assertEquals(3, result.size)
            val expectedShare = BigDecimal.ONE.divide(BigDecimal(3), 10, java.math.RoundingMode.DOWN)
            result.values.forEach { share ->
                assertEquals(0, expectedShare.compareTo(share))
            }
            val sum = result.values.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }
            assertTrue(sum.subtract(BigDecimal.ONE).abs() < BigDecimal("0.0001"))
        }

        @Test
        fun `single member gets 100 percent`() {
            val result = service.distributeEvenly(listOf("user-1"))

            assertEquals(1, result.size)
            assertEquals(0, BigDecimal.ONE.compareTo(result["user-1"]))
        }

        @Test
        fun `empty list returns empty map`() {
            val result = service.distributeEvenly(emptyList())

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("redistributeRemaining")
    inner class RedistributeRemaining {

        @Test
        fun `redistributes 40 percent among 2 others when one has 60 percent`() {
            val result = service.redistributeRemaining(
                editedShare = BigDecimal("0.6"),
                otherMemberIds = listOf("user-2", "user-3")
            )

            assertEquals(2, result.size)
            assertEquals(0, BigDecimal("0.2").compareTo(result["user-2"]))
            assertEquals(0, BigDecimal("0.2").compareTo(result["user-3"]))
        }

        @Test
        fun `redistributes 50 percent to single other`() {
            val result = service.redistributeRemaining(
                editedShare = BigDecimal("0.5"),
                otherMemberIds = listOf("user-2")
            )

            assertEquals(1, result.size)
            assertEquals(0, BigDecimal("0.5").compareTo(result["user-2"]))
        }

        @Test
        fun `edited share of 100 percent gives 0 to others`() {
            val result = service.redistributeRemaining(
                editedShare = BigDecimal.ONE,
                otherMemberIds = listOf("user-2", "user-3")
            )

            assertEquals(2, result.size)
            assertEquals(0, BigDecimal.ZERO.compareTo(result["user-2"]))
            assertEquals(0, BigDecimal.ZERO.compareTo(result["user-3"]))
        }

        @Test
        fun `edited share over 100 percent clamps to 0 for others`() {
            val result = service.redistributeRemaining(
                editedShare = BigDecimal("1.5"),
                otherMemberIds = listOf("user-2")
            )

            assertEquals(1, result.size)
            assertEquals(0, BigDecimal.ZERO.compareTo(result["user-2"]))
        }

        @Test
        fun `empty other members returns empty map`() {
            val result = service.redistributeRemaining(
                editedShare = BigDecimal("0.5"),
                otherMemberIds = emptyList()
            )

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("parseShareTexts")
    inner class ParseShareTexts {

        @Test
        fun `parses valid percentage texts`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1", "user-2"),
                memberShareTexts = mapOf("user-1" to "60", "user-2" to "40")
            )

            assertEquals(2, result.size)
            assertEquals(0, BigDecimal("0.6").compareTo(result["user-1"]))
            assertEquals(0, BigDecimal("0.4").compareTo(result["user-2"]))
        }

        @Test
        fun `returns empty map when all entries are blank`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1", "user-2"),
                memberShareTexts = mapOf("user-1" to "", "user-2" to "")
            )

            assertTrue(result.isEmpty())
        }

        @Test
        fun `returns empty map when share texts map is empty`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1"),
                memberShareTexts = emptyMap()
            )

            assertTrue(result.isEmpty())
        }

        @Test
        fun `returns empty map when any entry is unparseable and non-blank`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1", "user-2"),
                memberShareTexts = mapOf("user-1" to "50", "user-2" to "abc")
            )

            assertTrue(result.isEmpty())
        }

        @Test
        fun `missing member in texts defaults to 0`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1", "user-2"),
                memberShareTexts = mapOf("user-1" to "100")
            )

            assertEquals(2, result.size)
            assertEquals(0, BigDecimal.ONE.compareTo(result["user-1"]))
            assertEquals(0, BigDecimal.ZERO.compareTo(result["user-2"]))
        }

        @Test
        fun `parses decimal percentages correctly`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1", "user-2"),
                memberShareTexts = mapOf("user-1" to "33.33", "user-2" to "66.67")
            )

            assertEquals(2, result.size)
            assertTrue(result["user-1"]!!.subtract(BigDecimal("0.3333")).abs() < BigDecimal("0.0001"))
            assertTrue(result["user-2"]!!.subtract(BigDecimal("0.6667")).abs() < BigDecimal("0.0001"))
        }

        @Test
        fun `parses comma-decimal percentages correctly (ES locale format)`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1", "user-2"),
                memberShareTexts = mapOf("user-1" to "33,33", "user-2" to "66,67")
            )

            assertEquals(2, result.size)
            assertTrue(result["user-1"]!!.subtract(BigDecimal("0.3333")).abs() < BigDecimal("0.0001"))
            assertTrue(result["user-2"]!!.subtract(BigDecimal("0.6667")).abs() < BigDecimal("0.0001"))
        }
    }
}
