package es.pedrazamiguez.expenseshareapp.data.source.remote.enums

enum class Currency(val fullName: String, val symbol: String) {
    // Euro
    EUR("Euro", "€"),

    // United States Dollar
    USD("United States Dollar", "$"),

    // British Pound Sterling
    GBP(
        "British Pound Sterling", "£"
    ),

    // Mexican Peso
    MXN("Mexican Peso", "$"),

    // Japanese Yen
    JPY("Japanese Yen", "¥"),

    // Australian Dollar
    AUD("Australian Dollar", "A$"),

    // Canadian Dollar
    CAD(
        "Canadian Dollar", "CA$"
    ),

    // Swiss Franc
    CHF("Swiss Franc", "CHF"),

    // Chinese Yuan
    CNY(
        "Chinese Yuan", "¥"
    ),

    // Swedish Krona
    SEK("Swedish Krona", "kr"),

    // New Zealand Dollar
    NZD("New Zealand Dollar", "NZ$");

    companion object {
        fun fromString(currencyCode: String): Currency {
            return entries.find { it.name.equals(currencyCode, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown currency code: $currencyCode")
        }
    }
}
