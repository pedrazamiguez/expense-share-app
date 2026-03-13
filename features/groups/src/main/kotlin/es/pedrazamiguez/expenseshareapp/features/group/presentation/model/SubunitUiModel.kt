package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SubunitUiModel(
    val id: String = "",
    val name: String = "",
    val memberNames: ImmutableList<String> = persistentListOf(),
    val memberCount: String = "",
    val sharesSummary: String = ""
)

