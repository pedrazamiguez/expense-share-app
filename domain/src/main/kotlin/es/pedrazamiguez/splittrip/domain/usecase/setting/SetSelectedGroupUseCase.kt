package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository

class SetSelectedGroupUseCase(private val preferenceRepository: GroupPreferenceRepository) {

    suspend operator fun invoke(groupId: String?, groupName: String?, currency: String? = null) {
        preferenceRepository.setSelectedGroup(groupId, groupName, currency)
    }
}
