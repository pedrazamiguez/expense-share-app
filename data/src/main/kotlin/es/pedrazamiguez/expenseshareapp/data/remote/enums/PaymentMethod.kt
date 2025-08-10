package es.pedrazamiguez.expenseshareapp.data.remote.enums

enum class PaymentMethod {
    CASH, BIZUM, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, PAYPAL, VENMO, OTHER;

    companion object {
        fun fromString(method: String): PaymentMethod {
            return entries.find { it.name.equals(method, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown payment method: $method")
        }
    }
}
