package es.pedrazamiguez.expenseshareapp.data.source.remote.enums

enum class AddOnType {
    TIP, FEE, DISCOUNT;

    companion object {
        fun fromString(type: String): AddOnType {
            return entries.find { it.name.equals(type, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown add-on type: $type")
        }
    }
}
