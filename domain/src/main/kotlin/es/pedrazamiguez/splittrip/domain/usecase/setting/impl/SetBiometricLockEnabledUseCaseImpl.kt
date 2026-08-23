package es.pedrazamiguez.splittrip.domain.usecase.setting.impl

import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetBiometricLockEnabledUseCase

class SetBiometricLockEnabledUseCaseImpl(
    private val preferenceRepository: UserPreferenceRepository
) : SetBiometricLockEnabledUseCase {

    override suspend fun invoke(enabled: Boolean) {
        preferenceRepository.setBiometricLockEnabled(enabled)
    }
}
