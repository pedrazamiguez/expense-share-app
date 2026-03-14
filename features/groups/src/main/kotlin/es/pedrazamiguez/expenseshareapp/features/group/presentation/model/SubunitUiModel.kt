package es.pedrazamiguez.expenseshareapp.features.group.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SubunitUiModel(
    val id: String = "",
    val name: String = "",
    val memberShares: ImmutableList<MemberShareUiModel> = persistentListOf(),
    val memberCount: String = ""
)
