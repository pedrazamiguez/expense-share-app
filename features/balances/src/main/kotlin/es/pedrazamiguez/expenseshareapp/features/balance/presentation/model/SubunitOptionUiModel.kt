package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a subunit option in the Add Money dialog.
 *
 * When the current user belongs to a subunit, the dialog displays a toggle
 * to contribute on behalf of the subunit instead of individually.
 */
data class SubunitOptionUiModel(
    val id: String,
    val name: String
)
