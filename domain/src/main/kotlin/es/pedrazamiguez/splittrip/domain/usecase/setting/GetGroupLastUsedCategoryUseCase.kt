package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class GetGroupLastUsedCategoryUseCase(private val preferenceRepository: PreferenceRepository) {

    operator fun invoke(groupId: String): Flow<List<String>> = preferenceRepository.getGroupLastUsedCategory(groupId)
}
