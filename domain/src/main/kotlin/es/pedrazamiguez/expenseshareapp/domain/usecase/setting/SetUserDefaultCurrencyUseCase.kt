package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository

class SetUserDefaultCurrencyUseCase(
    private val preferenceRepository: PreferenceRepository
) {

    suspend operator fun invoke(currencyCode: String) {
        preferenceRepository.setUserDefaultCurrency(currencyCode)
    }

}
