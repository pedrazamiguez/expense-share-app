package es.pedrazamiguez.expenseshareapp.domain.usecase.group

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

class CreateGroupUseCase(
    private val groupRepository: GroupRepository
) {

    suspend operator fun invoke(group: Group): Result<String> = runCatching {
        groupRepository.createGroup(group)
    }

}