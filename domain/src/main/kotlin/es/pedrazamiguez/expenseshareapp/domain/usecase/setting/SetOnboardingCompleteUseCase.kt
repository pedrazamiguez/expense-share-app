package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository

class SetOnboardingCompleteUseCase(
    private val preferenceRepository: PreferenceRepository
) {

    suspend operator fun invoke() {
        preferenceRepository.setOnboardingComplete()
    }

}
