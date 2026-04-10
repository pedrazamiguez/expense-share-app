package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetGroupLastUsedPaymentMethodUseCase(private val preferenceRepository: GroupPreferenceRepository) {

    operator fun invoke(groupId: String): Flow<List<String>> =
        preferenceRepository.getGroupLastUsedPaymentMethod(groupId)
}
