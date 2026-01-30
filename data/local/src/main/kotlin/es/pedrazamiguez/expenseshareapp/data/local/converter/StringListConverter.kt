package es.pedrazamiguez.expenseshareapp.data.local.converter

import androidx.room.TypeConverter

/**
 * Room TypeConverter for List<String>.
 * Stores lists as comma-separated strings in the database.
 * Uses a special delimiter to handle edge cases.
 */
class StringListConverter {

    private companion object {
        // Using a delimiter that's unlikely to appear in normal strings
        const val DELIMITER = "|||"
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(DELIMITER)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(DELIMITER)
        }
    }
}
