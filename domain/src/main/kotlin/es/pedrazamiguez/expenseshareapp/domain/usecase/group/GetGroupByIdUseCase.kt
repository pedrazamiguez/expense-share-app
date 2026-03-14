package es.pedrazamiguez.expenseshareapp.domain.usecase.group

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

class GetGroupByIdUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: String): Group? = groupRepository.getGroupById(groupId)
}
