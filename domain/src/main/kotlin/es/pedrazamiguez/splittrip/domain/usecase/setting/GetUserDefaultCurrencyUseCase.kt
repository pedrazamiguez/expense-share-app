package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetUserDefaultCurrencyUseCase(private val preferenceRepository: UserPreferenceRepository) {

    operator fun invoke(): Flow<String> = preferenceRepository.getUserDefaultCurrency()
}
