package es.pedrazamiguez.expenseshareapp.domain.usecase.groups

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

class GetUserGroupsUseCase(
    private val groupRepository: GroupRepository
) {

    suspend fun invoke(): Result<List<Group>> {
        return try {
            Result.success(groupRepository.getAllGroups())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
