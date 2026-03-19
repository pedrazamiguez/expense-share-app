package es.pedrazamiguez.expenseshareapp.data.local.converter

import androidx.room.TypeConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import java.math.BigDecimal

/**
 * Room TypeConverter for `List<AddOn>`.
 * Stores the list as a JSON string in the database (same pattern as [CashTrancheListConverter]).
 *
 * Uses manual JSON serialization (no `org.json`) to stay compatible with pure JVM unit tests,
 * matching the approach of [StringBigDecimalMapConverter].
 *
 * Exchange rates are serialized as String (via [BigDecimal.toPlainString]) to avoid
 * IEEE 754 floating-point precision loss, consistent with the project's decimal-precision rules.
 */
class AddOnListConverter {

    @TypeConverter
    fun fromAddOnList(value: List<AddOn>?): String? {
        if (value.isNullOrEmpty()) return null
        return value.joinToString(separator = ",", prefix = "[", postfix = "]") { addOn ->
            buildString {
                append("{")
                append("\"id\":\"${escapeJson(addOn.id)}\"")
                append(",\"type\":\"${addOn.type.name}\"")
                append(",\"mode\":\"${addOn.mode.name}\"")
                append(",\"inputValue\":\"${escapeJson(addOn.inputValue)}\"")
                append(",\"isPercentage\":${addOn.isPercentage}")
                append(",\"amountCents\":${addOn.amountCents}")
                append(",\"currency\":\"${escapeJson(addOn.currency)}\"")
                append(",\"exchangeRate\":\"${addOn.exchangeRate.toPlainString()}\"")
                append(",\"groupAmountCents\":${addOn.groupAmountCents}")
                append(",\"paymentMethod\":\"${addOn.paymentMethod.name}\"")
                addOn.description?.let { desc ->
                    append(",\"description\":\"${escapeJson(desc)}\"")
                }
                append("}")
            }
        }
    }

    @TypeConverter
    fun toAddOnList(value: String?): List<AddOn>? {
        if (value.isNullOrBlank()) return null

        val result = mutableListOf<AddOn>()
        // Strip outer brackets
        val inner = value.trim().removePrefix("[").removeSuffix("]")
        if (inner.isBlank()) return null

        // Split by objects: find each {...} block
        var depth = 0
        var objectStart = -1
        for (i in inner.indices) {
            when (inner[i]) {
                '{' -> {
                    if (depth == 0) objectStart = i
                    depth++
                }
                '}' -> {
                    depth--
                    if (depth == 0 && objectStart >= 0) {
                        val objectStr = inner.substring(objectStart + 1, i)
                        result.add(parseAddOn(objectStr))
                        objectStart = -1
                    }
                }
            }
        }
        return if (result.isEmpty()) null else result
    }

    private fun parseAddOn(objectContent: String): AddOn {
        val fields = parseJsonFields(objectContent)
        return AddOn(
            id = fields["id"] ?: "",
            type = runCatching { AddOnType.fromString(fields["type"] ?: "") }
                .getOrDefault(AddOnType.FEE),
            mode = runCatching { AddOnMode.fromString(fields["mode"] ?: "") }
                .getOrDefault(AddOnMode.ON_TOP),
            inputValue = fields["inputValue"] ?: "",
            isPercentage = fields["isPercentage"]?.toBooleanStrictOrNull() ?: false,
            amountCents = fields["amountCents"]?.toLongOrNull() ?: 0L,
            currency = fields["currency"] ?: "EUR",
            exchangeRate = fields["exchangeRate"]?.toBigDecimalOrNull() ?: BigDecimal.ONE,
            groupAmountCents = fields["groupAmountCents"]?.toLongOrNull() ?: 0L,
            paymentMethod = runCatching {
                PaymentMethod.fromString(fields["paymentMethod"] ?: "")
            }.getOrDefault(PaymentMethod.OTHER),
            description = fields["description"]
        )
    }

    /**
     * Simple JSON field parser for a flat object.
     * Handles: "key":"stringValue", "key":numericValue, "key":booleanValue
     */
    private fun parseJsonFields(objectContent: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var i = 0
        val len = objectContent.length

        while (i < len) {
            // Find key start (opening quote)
            val keyStart = objectContent.indexOf('"', i)
            if (keyStart == -1) break
            val keyEnd = objectContent.indexOf('"', keyStart + 1)
            if (keyEnd == -1) break
            val key = objectContent.substring(keyStart + 1, keyEnd)

            // Find colon
            val colonIndex = objectContent.indexOf(':', keyEnd + 1)
            if (colonIndex == -1) break

            // Determine value type (string vs number/boolean)
            var valueStart = colonIndex + 1
            while (valueStart < len && objectContent[valueStart] == ' ') valueStart++

            if (valueStart >= len) break

            val fieldValue: String
            if (objectContent[valueStart] == '"') {
                // String value — find closing quote (handle escaped quotes)
                val valueEnd = findClosingQuote(objectContent, valueStart + 1)
                fieldValue = unescapeJson(objectContent.substring(valueStart + 1, valueEnd))
                i = valueEnd + 1
            } else {
                // Number or boolean — read until comma or end
                val valueEnd = objectContent.indexOfAny(charArrayOf(',', '}'), valueStart)
                val effectiveEnd = if (valueEnd == -1) len else valueEnd
                fieldValue = objectContent.substring(valueStart, effectiveEnd).trim()
                i = effectiveEnd
            }

            result[key] = fieldValue

            // Skip past comma
            if (i < len && objectContent[i] == ',') i++
        }
        return result
    }

    private fun findClosingQuote(str: String, startAfterQuote: Int): Int {
        var j = startAfterQuote
        while (j < str.length) {
            if (str[j] == '"' && (j == 0 || str[j - 1] != '\\')) return j
            j++
        }
        return str.length
    }

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun unescapeJson(value: String): String =
        value.replace("\\\"", "\"").replace("\\\\", "\\")
}
