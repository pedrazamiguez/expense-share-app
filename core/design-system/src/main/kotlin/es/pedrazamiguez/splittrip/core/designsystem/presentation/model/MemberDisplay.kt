package es.pedrazamiguez.splittrip.core.designsystem.presentation.model

/**
 * Sealed interface representing a group member's status (Active vs Former)
 * for presentation.
 */
sealed interface MemberDisplay {
    val userId: String
    val displayName: String

    data class Active(
        override val userId: String,
        override val displayName: String
    ) : MemberDisplay

    data class Former(
        override val userId: String,
        override val displayName: String
    ) : MemberDisplay
}
