package es.pedrazamiguez.splittrip.domain.usecase.group

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository

class CreateGroupUseCase(private val groupRepository: GroupRepository) {

    suspend operator fun invoke(group: Group): Result<String> = runCatching {
        groupRepository.createGroup(group)
    }
}
