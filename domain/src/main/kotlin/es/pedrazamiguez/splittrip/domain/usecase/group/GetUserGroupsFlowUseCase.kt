package es.pedrazamiguez.splittrip.domain.usecase.group

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow

class GetUserGroupsFlowUseCase(private val groupRepository: GroupRepository) {
    operator fun invoke(): Flow<List<Group>> = groupRepository.getAllGroupsFlow()
}
