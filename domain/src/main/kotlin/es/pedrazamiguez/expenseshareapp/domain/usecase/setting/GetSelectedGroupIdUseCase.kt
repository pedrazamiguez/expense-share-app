package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetSelectedGroupIdUseCase(
    private val preferenceRepository: PreferenceRepository
) {

    operator fun invoke(): Flow<String?> {
        return preferenceRepository.getSelectedGroupId()
    }

}
