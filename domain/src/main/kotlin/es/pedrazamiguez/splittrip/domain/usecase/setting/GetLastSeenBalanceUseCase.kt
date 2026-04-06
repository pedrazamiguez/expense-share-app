package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.BalancePreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetLastSeenBalanceUseCase(private val balancePreferenceRepository: BalancePreferenceRepository) {

    operator fun invoke(groupId: String): Flow<String?> = balancePreferenceRepository.getLastSeenBalance(groupId)
}
