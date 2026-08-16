package es.pedrazamiguez.splittrip.domain.enums

enum class ExpenseCategory {
    TRANSPORT,
    FOOD,
    LODGING,
    ACTIVITIES,
    INSURANCE,
    ENTERTAINMENT,
    SHOPPING,
    OTHER;

    companion object {
        fun fromString(category: String): ExpenseCategory = entries.find {
            it.name.equals(
                category,
                ignoreCase = true
            )
        } ?: throw IllegalArgumentException("Unknown category: $category")
    }
}
