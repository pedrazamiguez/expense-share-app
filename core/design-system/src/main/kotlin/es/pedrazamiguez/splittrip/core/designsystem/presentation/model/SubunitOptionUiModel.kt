package es.pedrazamiguez.splittrip.core.designsystem.presentation.model

/**
 * Presentation model representing a selectable subunit option.
 *
 * Shared across features (contributions, withdrawals, balances) and consumed
 * by common UI components such as
 * [es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.PayerTypeScopeCard].
 *
 * @param id   Unique identifier of the subunit.
 * @param name Human-readable subunit name displayed in the UI.
 */
data class SubunitOptionUiModel(
    val id: String,
    val name: String
)
