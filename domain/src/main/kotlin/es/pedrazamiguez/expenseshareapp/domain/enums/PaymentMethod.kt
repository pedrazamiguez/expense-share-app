package es.pedrazamiguez.expenseshareapp.domain.enums

enum class PaymentMethod {
    CASH,
    BIZUM,
    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    PAYPAL,
    VENMO,
    ALIPAY,
    WECHAT_PAY,
    OTHER;

    companion object {
        fun fromString(method: String): PaymentMethod {
            return entries.find {
                it.name.equals(
                    method,
                    ignoreCase = true
                )
            } ?: throw IllegalArgumentException("Unknown payment method: $method")
        }
    }
}
