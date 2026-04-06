package es.pedrazamiguez.splittrip.data.local.converter

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StringBigDecimalMapConverterTest {

    private val converter = StringBigDecimalMapConverter()

    @Nested
    inner class FromStringBigDecimalMap {

        @Test
        fun `converts non-empty map to JSON string`() {
            val map = mapOf("userA" to BigDecimal("0.5"), "userB" to BigDecimal("0.5"))

            val json = converter.fromStringBigDecimalMap(map)

            // Parse back to verify the JSON is valid and correct
            val result = converter.toStringBigDecimalMap(json)
            assertEquals(2, result.size)
            assertEquals(0, BigDecimal("0.5").compareTo(result["userA"]))
            assertEquals(0, BigDecimal("0.5").compareTo(result["userB"]))
        }

        @Test
        fun `converts empty map to empty JSON object string`() {
            val json = converter.fromStringBigDecimalMap(emptyMap())

            assertEquals("{}", json)
        }

        @Test
        fun `converts single entry map correctly`() {
            val map = mapOf("soloUser" to BigDecimal.ONE)

            val json = converter.fromStringBigDecimalMap(map)
            val result = converter.toStringBigDecimalMap(json)

            assertEquals(1, result.size)
            assertEquals(0, BigDecimal.ONE.compareTo(result["soloUser"]))
        }

        @Test
        fun `preserves decimal precision`() {
            val map = mapOf(
                "u1" to BigDecimal("0.4"),
                "u2" to BigDecimal("0.3"),
                "u3" to BigDecimal("0.3")
            )

            val json = converter.fromStringBigDecimalMap(map)
            val result = converter.toStringBigDecimalMap(json)

            assertEquals(0, BigDecimal("0.4").compareTo(result["u1"]))
            assertEquals(0, BigDecimal("0.3").compareTo(result["u2"]))
            assertEquals(0, BigDecimal("0.3").compareTo(result["u3"]))
        }

        @Test
        fun `produces deterministic output regardless of insertion order`() {
            val map1 = linkedMapOf(
                "b" to BigDecimal("0.3"),
                "a" to BigDecimal("0.5"),
                "c" to BigDecimal("0.2")
            )
            val map2 = linkedMapOf(
                "c" to BigDecimal("0.2"),
                "a" to BigDecimal("0.5"),
                "b" to BigDecimal("0.3")
            )

            val json1 = converter.fromStringBigDecimalMap(map1)
            val json2 = converter.fromStringBigDecimalMap(map2)

            assertEquals(json1, json2)
            assertEquals("""{"a":0.5,"b":0.3,"c":0.2}""", json1)
        }
    }

    @Nested
    inner class ToStringBigDecimalMap {

        @Test
        fun `converts valid JSON to map`() {
            val json = """{"userA":0.5,"userB":0.5}"""

            val map = converter.toStringBigDecimalMap(json)

            assertEquals(2, map.size)
            assertEquals(0, BigDecimal("0.5").compareTo(map["userA"]))
            assertEquals(0, BigDecimal("0.5").compareTo(map["userB"]))
        }

        @Test
        fun `converts empty JSON object to empty map`() {
            val map = converter.toStringBigDecimalMap("{}")

            assertTrue(map.isEmpty())
        }

        @Test
        fun `converts blank string to empty map`() {
            val map = converter.toStringBigDecimalMap("")

            assertTrue(map.isEmpty())
        }

        @Test
        fun `converts whitespace-only string to empty map`() {
            val map = converter.toStringBigDecimalMap("   ")

            assertTrue(map.isEmpty())
        }

        @Test
        fun `skips pairs with no colon separator`() {
            // A malformed pair without ':' should be skipped
            val json = """{"validKey":0.5,"noColonPair"}"""

            val map = converter.toStringBigDecimalMap(json)

            assertEquals(1, map.size)
            assertEquals(0, BigDecimal("0.5").compareTo(map["validKey"]))
        }

        @Test
        fun `skips pairs with invalid BigDecimal value`() {
            val json = """{"key1":0.5,"key2":notANumber}"""

            val map = converter.toStringBigDecimalMap(json)

            assertEquals(1, map.size)
            assertEquals(0, BigDecimal("0.5").compareTo(map["key1"]))
        }
    }

    @Nested
    inner class RoundTrip {

        @Test
        fun `map to JSON to map preserves data`() {
            val original = mapOf(
                "u1" to BigDecimal("0.6"),
                "u2" to BigDecimal("0.25"),
                "u3" to BigDecimal("0.15")
            )

            val json = converter.fromStringBigDecimalMap(original)
            val result = converter.toStringBigDecimalMap(json)

            assertEquals(3, result.size)
            assertEquals(0, BigDecimal("0.6").compareTo(result["u1"]))
            assertEquals(0, BigDecimal("0.25").compareTo(result["u2"]))
            assertEquals(0, BigDecimal("0.15").compareTo(result["u3"]))
        }

        @Test
        fun `reads legacy Double-serialized JSON correctly`() {
            // Data written by the old StringDoubleMapConverter: {"u1":0.5,"u2":0.5}
            val legacyJson = """{"u1":0.5,"u2":0.5}"""

            val result = converter.toStringBigDecimalMap(legacyJson)

            assertEquals(2, result.size)
            assertEquals(0, BigDecimal("0.5").compareTo(result["u1"]))
            assertEquals(0, BigDecimal("0.5").compareTo(result["u2"]))
        }
    }
}
