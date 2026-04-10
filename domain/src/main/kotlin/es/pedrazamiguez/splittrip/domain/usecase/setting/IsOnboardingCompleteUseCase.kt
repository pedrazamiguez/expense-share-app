package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.OnboardingPreferenceRepository
import kotlinx.coroutines.flow.Flow

class IsOnboardingCompleteUseCase(private val preferenceRepository: OnboardingPreferenceRepository) {

    operator fun invoke(): Flow<Boolean> = preferenceRepository.isOnboardingComplete()
}
