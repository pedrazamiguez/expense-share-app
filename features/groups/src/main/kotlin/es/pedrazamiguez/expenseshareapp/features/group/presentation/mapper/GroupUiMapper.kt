package es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel

interface GroupUiMapper {
    fun toGroupUiModel(group: Group): GroupUiModel
    fun toGroupUiModelList(groups: List<Group>): List<GroupUiModel>
}
