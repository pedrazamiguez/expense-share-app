package es.pedrazamiguez.splittrip.features.subunit.presentation.model

/**
 * Represents a single member's share in a subunit for display purposes.
 *
 * @param displayName The member's display name (or email/userId fallback).
 * @param shareText The formatted percentage string (e.g., "50 %").
 */
data class MemberShareUiModel(val displayName: String = "", val shareText: String = "")
