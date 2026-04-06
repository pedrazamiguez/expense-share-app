package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository

class SetUserDefaultCurrencyUseCase(private val preferenceRepository: PreferenceRepository) {

    suspend operator fun invoke(currencyCode: String) {
        preferenceRepository.setUserDefaultCurrency(currencyCode)
    }
}
