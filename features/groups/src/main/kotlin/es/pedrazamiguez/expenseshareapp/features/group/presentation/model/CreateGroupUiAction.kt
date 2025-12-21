package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

import androidx.annotation.StringRes

sealed interface CreateGroupUiAction {
    data object None : CreateGroupUiAction
    data class ShowError(
        @param:StringRes
        val messageRes: Int? = null, val message: String? = null
    ) : CreateGroupUiAction
}
