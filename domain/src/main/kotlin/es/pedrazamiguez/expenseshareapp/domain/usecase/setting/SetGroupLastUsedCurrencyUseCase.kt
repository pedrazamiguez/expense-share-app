package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository

class SetGroupLastUsedCurrencyUseCase(private val preferenceRepository: PreferenceRepository) {

    suspend operator fun invoke(groupId: String, currencyCode: String) {
        preferenceRepository.setGroupLastUsedCurrency(groupId, currencyCode)
    }
}
