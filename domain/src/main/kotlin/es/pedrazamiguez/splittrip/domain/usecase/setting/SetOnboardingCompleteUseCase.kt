package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.OnboardingPreferenceRepository

class SetOnboardingCompleteUseCase(private val preferenceRepository: OnboardingPreferenceRepository) {

    suspend operator fun invoke() {
        preferenceRepository.setOnboardingComplete()
    }
}
