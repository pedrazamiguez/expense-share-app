package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository

class SetOnboardingCompleteUseCase(private val preferenceRepository: PreferenceRepository) {

    suspend operator fun invoke() {
        preferenceRepository.setOnboardingComplete()
    }
}
