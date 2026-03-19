package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a sub-unit option in the Add Money dialog.
 *
 * When the current user belongs to a sub-unit, the dialog displays a toggle
 * to contribute on behalf of the sub-unit instead of individually.
 */
data class SubunitOptionUiModel(
    val id: String,
    val name: String
)
