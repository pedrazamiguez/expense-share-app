package es.pedrazamiguez.expenseshareapp.data.local.converter

import androidx.room.TypeConverter

/**
 * Room TypeConverter for Map<String, Double>.
 * Stores the map as a JSON-like string in the database.
 * Used for sub-unit memberShares (userId → weight).
 *
 * Format: `{"key1":0.5,"key2":0.5}` — simple enough to serialize without org.json,
 * which avoids issues with pure JVM unit tests.
 */
class StringDoubleMapConverter {

    @TypeConverter
    fun fromStringDoubleMap(value: Map<String, Double>): String {
        if (value.isEmpty()) return "{}"
        return value.entries.sortedBy { it.key }.joinToString(
            separator = ",",
            prefix = "{",
            postfix = "}"
        ) { (key, doubleValue) -> "\"$key\":$doubleValue" }
    }

    @TypeConverter
    fun toStringDoubleMap(value: String): Map<String, Double> {
        if (value.isBlank() || value == "{}") return emptyMap()

        // Strip outer braces, then split on commas that separate key-value pairs
        val inner = value.removePrefix("{").removeSuffix("}")
        val result = mutableMapOf<String, Double>()

        // Split on "," but handle the pattern "key":value
        val pairs = inner.split(",")
        for (pair in pairs) {
            val colonIndex = pair.lastIndexOf(':')
            if (colonIndex == -1) continue
            val key = pair.substring(0, colonIndex).trim().removeSurrounding("\"")
            val doubleValue = pair.substring(colonIndex + 1).trim().toDoubleOrNull() ?: continue
            result[key] = doubleValue
        }
        return result
    }
}


