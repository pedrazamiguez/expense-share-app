package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository

class SetSelectedGroupUseCase(
    private val preferenceRepository: PreferenceRepository
) {

    suspend operator fun invoke(groupId: String?, groupName: String?) {
        preferenceRepository.setSelectedGroup(groupId, groupName)
    }

}
