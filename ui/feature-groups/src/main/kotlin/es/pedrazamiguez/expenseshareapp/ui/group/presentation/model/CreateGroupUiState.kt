package es.pedrazamiguez.expenseshareapp.ui.group.presentation.model

data class CreateGroupUiState(
    val isLoading: Boolean = false,
    val groupName: String = "",
    val groupCurrency: String = "EUR",
    val groupDescription: String = "",
    val groupMembers: List<String> = emptyList(),
    val error: String? = null,
    val isNameValid: Boolean = true
)
