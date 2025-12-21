package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

import androidx.annotation.StringRes

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val groupName: String = "",
    val groupCurrency: String = "EUR",
    val groupDescription: String = "",
    val groupMembers: List<String> = emptyList(),
    @field:StringRes
    val errorRes: Int? = null,
    val errorMessage: String? = null,
    val isNameValid: Boolean = true
)
