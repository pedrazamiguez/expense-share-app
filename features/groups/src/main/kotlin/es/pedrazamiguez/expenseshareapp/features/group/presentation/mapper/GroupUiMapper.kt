package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel
import kotlinx.collections.immutable.ImmutableList

interface GroupUiMapper {
    fun toGroupUiModel(group: Group): GroupUiModel
    fun toGroupUiModelList(groups: List<Group>): ImmutableList<GroupUiModel>
}
