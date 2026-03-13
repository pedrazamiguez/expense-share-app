package es.pedrazamiguez.expenseshareapp.data.local.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StringDoubleMapConverterTest {

    private val converter = StringDoubleMapConverter()

    @Nested
    inner class FromStringDoubleMap {

        @Test
        fun `converts non-empty map to JSON string`() {
            val map = mapOf("userA" to 0.5, "userB" to 0.5)

            val json = converter.fromStringDoubleMap(map)

            // Parse back to verify the JSON is valid and correct
            val result = converter.toStringDoubleMap(json)
            assertEquals(map, result)
        }

        @Test
        fun `converts empty map to empty JSON object string`() {
            val json = converter.fromStringDoubleMap(emptyMap())

            assertEquals("{}", json)
        }

        @Test
        fun `converts single entry map correctly`() {
            val map = mapOf("soloUser" to 1.0)

            val json = converter.fromStringDoubleMap(map)
            val result = converter.toStringDoubleMap(json)

            assertEquals(map, result)
        }

        @Test
        fun `preserves decimal precision`() {
            val map = mapOf("u1" to 0.4, "u2" to 0.3, "u3" to 0.3)

            val json = converter.fromStringDoubleMap(map)
            val result = converter.toStringDoubleMap(json)

            assertEquals(0.4, result["u1"])
            assertEquals(0.3, result["u2"])
            assertEquals(0.3, result["u3"])
        }
    }

    @Nested
    inner class ToStringDoubleMap {

        @Test
        fun `converts valid JSON to map`() {
            val json = """{"userA":0.5,"userB":0.5}"""

            val map = converter.toStringDoubleMap(json)

            assertEquals(2, map.size)
            assertEquals(0.5, map["userA"])
            assertEquals(0.5, map["userB"])
        }

        @Test
        fun `converts empty JSON object to empty map`() {
            val map = converter.toStringDoubleMap("{}")

            assertTrue(map.isEmpty())
        }

        @Test
        fun `converts blank string to empty map`() {
            val map = converter.toStringDoubleMap("")

            assertTrue(map.isEmpty())
        }
    }

    @Nested
    inner class RoundTrip {

        @Test
        fun `map to JSON to map preserves data`() {
            val original = mapOf("u1" to 0.6, "u2" to 0.25, "u3" to 0.15)

            val json = converter.fromStringDoubleMap(original)
            val result = converter.toStringDoubleMap(json)

            assertEquals(original, result)
        }
    }
}

