package es.pedrazamiguez.splittrip.domain.enums

enum class AddOnType {
    TIP,
    FEE,
    DISCOUNT,
    SURCHARGE;

    companion object {
        fun fromString(type: String): AddOnType = entries.find {
            it.name.equals(
                type,
                ignoreCase = true
            )
        } ?: throw IllegalArgumentException("Unknown add-on type: $type")
    }
}
