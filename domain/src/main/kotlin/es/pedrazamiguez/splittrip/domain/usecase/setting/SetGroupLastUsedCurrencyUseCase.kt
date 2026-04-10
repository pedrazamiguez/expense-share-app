package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository

class SetGroupLastUsedCurrencyUseCase(private val preferenceRepository: GroupPreferenceRepository) {

    suspend operator fun invoke(groupId: String, currencyCode: String) {
        preferenceRepository.setGroupLastUsedCurrency(groupId, currencyCode)
    }
}
