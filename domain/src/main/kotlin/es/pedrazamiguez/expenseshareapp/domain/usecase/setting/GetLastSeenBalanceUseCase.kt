package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.BalancePreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetLastSeenBalanceUseCase(private val balancePreferenceRepository: BalancePreferenceRepository) {

    operator fun invoke(groupId: String): Flow<String?> = balancePreferenceRepository.getLastSeenBalance(groupId)
}
