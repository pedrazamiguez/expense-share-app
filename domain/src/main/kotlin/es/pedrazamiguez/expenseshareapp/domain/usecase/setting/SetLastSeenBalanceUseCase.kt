package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository

class SetLastSeenBalanceUseCase(private val preferenceRepository: PreferenceRepository) {

    suspend operator fun invoke(groupId: String, formattedBalance: String) {
        preferenceRepository.setLastSeenBalance(groupId, formattedBalance)
    }
}
