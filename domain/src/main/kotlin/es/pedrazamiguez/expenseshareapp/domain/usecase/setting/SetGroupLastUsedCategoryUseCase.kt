package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository

class SetGroupLastUsedCategoryUseCase(
    private val preferenceRepository: PreferenceRepository
) {

    suspend operator fun invoke(groupId: String, categoryId: String) {
        preferenceRepository.setGroupLastUsedCategory(groupId, categoryId)
    }

}

