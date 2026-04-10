package es.pedrazamiguez.splittrip.domain.enums

enum class Currency(val fullName: String, val symbol: String) {
    // Euro
    EUR(
        "Euro",
        "€"
    ),

    // United States Dollar
    USD(
        "United States Dollar",
        "$"
    ),

    // British Pound Sterling
    GBP(
        "British Pound Sterling",
        "£"
    ),

    // Mexican Peso
    MXN(
        "Mexican Peso",
        "$"
    ),

    // Japanese Yen
    JPY(
        "Japanese Yen",
        "¥"
    ),

    // Australian Dollar
    AUD(
        "Australian Dollar",
        "A$"
    ),

    // Canadian Dollar
    CAD(
        "Canadian Dollar",
        "CA$"
    ),

    // Swiss Franc
    CHF(
        "Swiss Franc",
        "CHF"
    ),

    // Chinese Yuan
    CNY(
        "Chinese Yuan",
        "¥"
    ),

    // Swedish Krona
    SEK(
        "Swedish Krona",
        "kr"
    ),

    // New Zealand Dollar
    NZD(
        "New Zealand Dollar",
        "NZ$"
    ),

    // Thai Baht
    THB(
        "Thai Baht",
        "฿"
    ),

    // South Korean Won
    KRW(
        "South Korean Won",
        "₩"
    ),

    // Indian Rupee
    INR(
        "Indian Rupee",
        "₹"
    ),

    // Brazilian Real
    BRL(
        "Brazilian Real",
        "R$"
    ),

    // Argentine Peso
    ARS(
        "Argentine Peso",
        "$"
    ),

    // Colombian Peso
    COP(
        "Colombian Peso",
        "$"
    ),

    // Chilean Peso
    CLP(
        "Chilean Peso",
        "$"
    ),

    // Peruvian Sol
    PEN(
        "Peruvian Sol",
        "S/"
    ),

    // Danish Krone
    DKK(
        "Danish Krone",
        "kr"
    ),

    // Norwegian Krone
    NOK(
        "Norwegian Krone",
        "kr"
    ),

    // Polish Zloty
    PLN(
        "Polish Zloty",
        "zł"
    ),

    // Czech Koruna
    CZK(
        "Czech Koruna",
        "Kč"
    ),

    // Hungarian Forint
    HUF(
        "Hungarian Forint",
        "Ft"
    ),

    // Turkish Lira
    TRY(
        "Turkish Lira",
        "₺"
    ),

    // Moroccan Dirham
    MAD(
        "Moroccan Dirham",
        "MAD"
    ),

    // UAE Dirham
    AED(
        "UAE Dirham",
        "د.إ"
    ),

    // South African Rand
    ZAR(
        "South African Rand",
        "R"
    ),

    // Singapore Dollar
    SGD(
        "Singapore Dollar",
        "S$"
    ),

    // Hong Kong Dollar
    HKD(
        "Hong Kong Dollar",
        "HK$"
    ),

    // Indonesian Rupiah
    IDR(
        "Indonesian Rupiah",
        "Rp"
    );

    companion object {
        fun fromString(currencyCode: String): Currency = entries.find {
            it.name.equals(
                currencyCode,
                ignoreCase = true
            )
        } ?: throw IllegalArgumentException("Unknown currency code: $currencyCode")
    }
}
