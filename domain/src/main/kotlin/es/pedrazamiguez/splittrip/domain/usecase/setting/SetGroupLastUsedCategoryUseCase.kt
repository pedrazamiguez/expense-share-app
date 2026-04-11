package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository

class SetGroupLastUsedCategoryUseCase(private val preferenceRepository: GroupPreferenceRepository) {

    suspend operator fun invoke(groupId: String, categoryId: String) {
        preferenceRepository.setGroupLastUsedCategory(groupId, categoryId)
    }
}
