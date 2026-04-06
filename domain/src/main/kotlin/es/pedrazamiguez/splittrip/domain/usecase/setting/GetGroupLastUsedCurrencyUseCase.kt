package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetGroupLastUsedCurrencyUseCase(private val preferenceRepository: PreferenceRepository) {

    operator fun invoke(groupId: String): Flow<String?> = preferenceRepository.getGroupLastUsedCurrency(groupId)
}
