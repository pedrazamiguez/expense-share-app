package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetSelectedGroupCurrencyUseCase(private val preferenceRepository: PreferenceRepository) {

    operator fun invoke(): Flow<String?> = preferenceRepository.getSelectedGroupCurrency()
}
