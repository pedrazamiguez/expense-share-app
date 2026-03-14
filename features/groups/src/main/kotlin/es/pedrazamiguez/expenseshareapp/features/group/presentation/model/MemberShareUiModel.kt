package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

/**
 * Represents a single member's share in a sub-unit for display purposes.
 *
 * @param displayName The member's display name (or email/userId fallback).
 * @param shareText The formatted percentage string (e.g., "50 %").
 */
data class MemberShareUiModel(val displayName: String = "", val shareText: String = "")
