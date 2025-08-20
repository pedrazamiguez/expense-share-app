package es.pedrazamiguez.expenseshareapp.domain.model

data class Currency(
    val code: String,
    val symbol: String,
    val defaultName: String,
    val decimalDigits: Int
) {
    init {
        require(code.matches(Regex("[A-Z]{3}"))) { "Currency code must be a 3-letter ISO 4217 code" }
        require(decimalDigits >= 0) { "Decimal digits cannot be negative" }
    }
}
