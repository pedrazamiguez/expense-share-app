package es.pedrazamiguez.splittrip.domain.usecase.group

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository

class GetGroupByIdUseCase(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupId: String): Group? = groupRepository.getGroupById(groupId)
}
