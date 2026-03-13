package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Holds the mutable form state for creating or editing a sub-unit.
 *
 * @param id Empty string for create, non-empty for edit.
 * @param name The sub-unit name being edited.
 * @param selectedMemberIds IDs of members currently selected in the form.
 * @param memberShares userId → share text (e.g., "50"). Empty map means equal shares.
 * @param availableMembers All group members with their assignment status for the current context.
 */
data class SubunitFormState(
    val id: String = "",
    val name: String = "",
    val selectedMemberIds: ImmutableList<String> = persistentListOf(),
    val memberShares: Map<String, String> = emptyMap(),
    val availableMembers: ImmutableList<MemberUiModel> = persistentListOf()
) {
    val isEditing: Boolean get() = id.isNotBlank()
}

