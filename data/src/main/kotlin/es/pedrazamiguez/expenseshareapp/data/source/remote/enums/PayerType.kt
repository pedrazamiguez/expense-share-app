package es.pedrazamiguez.expenseshareapp.data.source.remote.enums

enum class PayerType {
    USER, SUBUNIT, GROUP;

    companion object {
        fun fromString(type: String): PayerType {
            return entries.find { it.name.equals(type, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown payer type: $type")
        }
    }
}
