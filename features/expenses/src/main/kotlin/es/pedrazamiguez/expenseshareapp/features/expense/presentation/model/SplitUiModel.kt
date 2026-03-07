package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

/**
 * UI model representing a single user's share of an expense.
 *
 * [formattedAmount] is the locale-aware display string of the user's share.
 * [amountInput] is the raw user input for EXACT mode (editable).
 * [percentageInput] is the raw user input for PERCENT mode (editable).
 */
data class SplitUiModel(
    val userId: String,
    val displayName: String,
    val amountCents: Long = 0L,
    val formattedAmount: String = "",
    val amountInput: String = "",
    val percentageInput: String = "",
    val isExcluded: Boolean = false
)

