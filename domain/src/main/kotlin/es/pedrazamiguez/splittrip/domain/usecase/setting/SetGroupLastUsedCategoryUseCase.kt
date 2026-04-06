package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository

class SetGroupLastUsedCategoryUseCase(private val preferenceRepository: PreferenceRepository) {

    suspend operator fun invoke(groupId: String, categoryId: String) {
        preferenceRepository.setGroupLastUsedCategory(groupId, categoryId)
    }
}
