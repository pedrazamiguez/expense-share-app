package es.pedrazamiguez.expenseshareapp.domain.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.abs

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
            assertEquals(0.5, result["user-1"]!!, 0.0001)
            assertEquals(0.5, result["user-2"]!!, 0.0001)
        }

        @Test
        fun `distributes evenly among 3 members`() {
            val result = service.distributeEvenly(listOf("user-1", "user-2", "user-3"))

            assertEquals(3, result.size)
            val expectedShare = 1.0 / 3.0
            result.values.forEach { share ->
                assertEquals(expectedShare, share, 0.0001)
            }
            assertTrue(abs(result.values.sum() - 1.0) < 0.0001)
        }

        @Test
        fun `single member gets 100 percent`() {
            val result = service.distributeEvenly(listOf("user-1"))

            assertEquals(1, result.size)
            assertEquals(1.0, result["user-1"]!!, 0.0001)
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
                editedShare = 0.6,
                otherMemberIds = listOf("user-2", "user-3")
            )

            assertEquals(2, result.size)
            assertEquals(0.2, result["user-2"]!!, 0.0001)
            assertEquals(0.2, result["user-3"]!!, 0.0001)
        }

        @Test
        fun `redistributes 50 percent to single other`() {
            val result = service.redistributeRemaining(
                editedShare = 0.5,
                otherMemberIds = listOf("user-2")
            )

            assertEquals(1, result.size)
            assertEquals(0.5, result["user-2"]!!, 0.0001)
        }

        @Test
        fun `edited share of 100 percent gives 0 to others`() {
            val result = service.redistributeRemaining(
                editedShare = 1.0,
                otherMemberIds = listOf("user-2", "user-3")
            )

            assertEquals(2, result.size)
            assertEquals(0.0, result["user-2"]!!, 0.0001)
            assertEquals(0.0, result["user-3"]!!, 0.0001)
        }

        @Test
        fun `edited share over 100 percent clamps to 0 for others`() {
            val result = service.redistributeRemaining(
                editedShare = 1.5,
                otherMemberIds = listOf("user-2")
            )

            assertEquals(1, result.size)
            assertEquals(0.0, result["user-2"]!!, 0.0001)
        }

        @Test
        fun `empty other members returns empty map`() {
            val result = service.redistributeRemaining(
                editedShare = 0.5,
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
            assertEquals(0.6, result["user-1"]!!, 0.0001)
            assertEquals(0.4, result["user-2"]!!, 0.0001)
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
            assertEquals(1.0, result["user-1"]!!, 0.0001)
            assertEquals(0.0, result["user-2"]!!, 0.0001)
        }

        @Test
        fun `parses decimal percentages correctly`() {
            val result = service.parseShareTexts(
                selectedMemberIds = listOf("user-1", "user-2"),
                memberShareTexts = mapOf("user-1" to "33.33", "user-2" to "66.67")
            )

            assertEquals(2, result.size)
            assertEquals(0.3333, result["user-1"]!!, 0.0001)
            assertEquals(0.6667, result["user-2"]!!, 0.0001)
        }
    }

    @Nested
    @DisplayName("formatShareForInput")
    inner class FormatShareForInput {

        @Test
        fun `formats 50 percent`() {
            assertEquals("50", service.formatShareForInput(0.5))
        }

        @Test
        fun `formats 100 percent`() {
            assertEquals("100", service.formatShareForInput(1.0))
        }

        @Test
        fun `formats fractional percentage`() {
            val result = service.formatShareForInput(0.3333)
            assertEquals("33.33", result)
        }

        @Test
        fun `formats 0 percent`() {
            assertEquals("0", service.formatShareForInput(0.0))
        }
    }
}

