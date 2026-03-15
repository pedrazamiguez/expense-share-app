package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CreateEditSubunitUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val name: String = "",
    val selectedMemberIds: ImmutableList<String> = persistentListOf(),
    val memberShares: Map<String, String> = emptyMap(),
    val availableMembers: ImmutableList<MemberUiModel> = persistentListOf(),
    val nameError: UiText? = null,
    val membersError: UiText? = null,
    val sharesError: UiText? = null
)
