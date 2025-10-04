package es.pedrazamiguez.expenseshareapp.domain.usecase

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

class CreateGroupUseCase(
    private val groupRepository: GroupRepository
) {

    suspend operator fun invoke(group: Group): Result<String> {
        return try {
            Result.success(groupRepository.createGroup(group))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
