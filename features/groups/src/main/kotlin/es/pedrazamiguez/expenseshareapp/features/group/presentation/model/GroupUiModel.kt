package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

data class GroupUiModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "",
    val membersCountText: String = "",
    val dateText: String = ""
)
