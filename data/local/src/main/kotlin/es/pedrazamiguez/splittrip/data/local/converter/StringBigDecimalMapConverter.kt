package es.pedrazamiguez.splittrip.data.local.converter

import androidx.room.TypeConverter
import java.math.BigDecimal

/**
 * Room TypeConverter for Map<String, BigDecimal>.
 * Stores the map as a JSON-like string in the database.
 * Used for subunit memberShares (userId → weight).
 *
 * Format: `{"key1":0.5,"key2":0.5}` — simple enough to serialize without org.json,
 * which avoids issues with pure JVM unit tests.
 *
 * Backwards-compatible with data written by the former `StringDoubleMapConverter`:
 * values like `0.5` parse correctly as `BigDecimal`.
 */
class StringBigDecimalMapConverter {

    @TypeConverter
    fun fromStringBigDecimalMap(value: Map<String, BigDecimal>): String {
        if (value.isEmpty()) return "{}"
        return value.entries.sortedBy { it.key }.joinToString(
            separator = ",",
            prefix = "{",
            postfix = "}"
        ) { (key, bigDecimalValue) -> "\"$key\":${bigDecimalValue.toPlainString()}" }
    }

    @TypeConverter
    fun toStringBigDecimalMap(value: String): Map<String, BigDecimal> {
        if (value.isBlank() || value == "{}") return emptyMap()

        // Strip outer braces, then split on commas that separate key-value pairs
        val inner = value.removePrefix("{").removeSuffix("}")

        // Split on "," and parse each "key":value pair using functional operators
        return inner.split(",")
            .mapNotNull { pair ->
                val colonIndex = pair.lastIndexOf(':')
                if (colonIndex == -1) return@mapNotNull null
                val key = pair.substring(0, colonIndex).trim().removeSurrounding("\"")
                val bigDecimalValue = pair.substring(colonIndex + 1).trim().toBigDecimalOrNull()
                    ?: return@mapNotNull null
                key to bigDecimalValue
            }
            .toMap()
    }
}
