package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetLastSeenBalanceUseCase(private val preferenceRepository: PreferenceRepository) {

    operator fun invoke(groupId: String): Flow<String?> = preferenceRepository.getLastSeenBalance(groupId)
}
