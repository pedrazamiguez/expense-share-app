package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository

class SetUserDefaultCurrencyUseCase(private val preferenceRepository: UserPreferenceRepository) {

    suspend operator fun invoke(currencyCode: String) {
        preferenceRepository.setUserDefaultCurrency(currencyCode)
    }
}
