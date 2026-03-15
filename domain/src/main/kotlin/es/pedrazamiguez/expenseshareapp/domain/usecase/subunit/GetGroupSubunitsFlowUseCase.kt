package es.pedrazamiguez.expenseshareapp.domain.usecase.subunit

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the list of sub-units in a group as a reactive stream.
 *
 * Simple delegation to the repository — no membership check needed for reads.
 */
class GetGroupSubunitsFlowUseCase(private val subunitRepository: SubunitRepository) {
    operator fun invoke(groupId: String): Flow<List<Subunit>> = subunitRepository.getGroupSubunitsFlow(groupId)
}
