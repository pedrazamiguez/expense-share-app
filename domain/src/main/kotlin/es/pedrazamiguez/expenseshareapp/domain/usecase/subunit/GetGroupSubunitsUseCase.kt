package es.pedrazamiguez.expenseshareapp.domain.usecase.subunit

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository

/**
 * One-shot read of all sub-units for a group from local storage.
 *
 * Unlike [GetGroupSubunitsFlowUseCase], this does NOT trigger cloud subscription
 * side effects, making it safe for validation reads and form initialization.
 */
class GetGroupSubunitsUseCase(private val subunitRepository: SubunitRepository) {
    suspend operator fun invoke(groupId: String): List<Subunit> = subunitRepository.getGroupSubunits(groupId)
}
