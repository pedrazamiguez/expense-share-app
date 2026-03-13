package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitFormState
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SubunitManagementUiState(
    val isLoading: Boolean = true,
    val groupId: String = "",
    val groupName: String = "",
    val subunits: ImmutableList<SubunitUiModel> = persistentListOf(),
    val isDialogVisible: Boolean = false,
    val editingSubunit: SubunitFormState? = null,
    val nameError: UiText? = null,
    val membersError: UiText? = null
)

