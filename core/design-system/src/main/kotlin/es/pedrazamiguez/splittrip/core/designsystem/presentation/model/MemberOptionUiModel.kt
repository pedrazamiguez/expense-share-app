package es.pedrazamiguez.splittrip.core.designsystem.presentation.model

/**
 * Presentation model representing a selectable group member option.
 *
 * Shared across features (contributions, withdrawals) and consumed by common UI
 * components such as
 * [es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.MemberPickerCard].
 *
 * @param userId        Unique identifier of the member.
 * @param displayName   Human-readable name shown in the UI.
 * @param isCurrentUser Whether this member is the currently authenticated user.
 */
data class MemberOptionUiModel(
    val userId: String,
    val displayName: String,
    val isCurrentUser: Boolean
)
