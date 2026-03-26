package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.BalancePreferenceRepository

class SetLastSeenBalanceUseCase(private val balancePreferenceRepository: BalancePreferenceRepository) {

    suspend operator fun invoke(groupId: String, formattedBalance: String) {
        balancePreferenceRepository.setLastSeenBalance(groupId, formattedBalance)
    }
}
