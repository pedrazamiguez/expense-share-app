package es.pedrazamiguez.splittrip.domain.usecase.setting.impl

import es.pedrazamiguez.splittrip.domain.repository.UserPreferenceRepository
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetBiometricLockEnabledUseCase
import kotlinx.coroutines.flow.Flow

class GetBiometricLockEnabledUseCaseImpl(
    private val preferenceRepository: UserPreferenceRepository
) : GetBiometricLockEnabledUseCase {

    override fun invoke(): Flow<Boolean> = preferenceRepository.getBiometricLockEnabled()
}
