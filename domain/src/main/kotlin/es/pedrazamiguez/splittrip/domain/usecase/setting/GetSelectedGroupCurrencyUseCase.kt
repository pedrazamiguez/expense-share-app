package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetSelectedGroupCurrencyUseCase(private val preferenceRepository: GroupPreferenceRepository) {

    operator fun invoke(): Flow<String?> = preferenceRepository.getSelectedGroupCurrency()
}
