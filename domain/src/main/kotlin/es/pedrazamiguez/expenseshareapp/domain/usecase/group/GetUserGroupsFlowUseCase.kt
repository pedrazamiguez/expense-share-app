package es.pedrazamiguez.expenseshareapp.domain.usecase.group

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow

class GetUserGroupsFlowUseCase(
    private val groupRepository: GroupRepository
) {
    operator fun invoke(): Flow<List<Group>> = groupRepository.getAllGroupsFlow()
}
