package es.pedrazamiguez.expenseshareapp.domain.enums

enum class PaymentStatus {
    RECEIVED, PENDING, FINISHED, SCHEDULED, CANCELLED;

    companion object {
        fun fromString(status: String): PaymentStatus {
            return entries.find {
                it.name.equals(
                    status,
                    ignoreCase = true
                )
            } ?: throw IllegalArgumentException("Unknown status: $status")
        }
    }
}
