package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetGroupLastUsedPaymentMethodUseCase(
    private val preferenceRepository: PreferenceRepository
) {

    operator fun invoke(groupId: String): Flow<List<String>> {
        return preferenceRepository.getGroupLastUsedPaymentMethod(groupId)
    }

}

