package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

/**
 * UI model for displaying a group member in the subunit form dialog.
 *
 * @param userId The unique ID of the member.
 * @param displayName The resolved display name (or email fallback).
 * @param isAssigned Whether the member is already assigned to another subunit.
 * @param assignedSubunitName Name of the subunit the member is assigned to (for disabled hint).
 */
data class MemberUiModel(
    val userId: String = "",
    val displayName: String = "",
    val isAssigned: Boolean = false,
    val assignedSubunitName: String = ""
)
