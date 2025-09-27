package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.enums

enum class ExpenseCategory {
    // Expenses that add money
    CONTRIBUTION, REFUND,

    // Expenses that subtract money
    TRANSPORT, FOOD, LODGING, ACTIVITIES, INSURANCE, ENTERTAINMENT, SHOPPING, OTHER;

    companion object {
        fun fromString(category: String): ExpenseCategory {
            return entries.find {
                it.name.equals(
                    category,
                    ignoreCase = true
                )
            } ?: throw IllegalArgumentException("Unknown category: $category")
        }
    }
}
