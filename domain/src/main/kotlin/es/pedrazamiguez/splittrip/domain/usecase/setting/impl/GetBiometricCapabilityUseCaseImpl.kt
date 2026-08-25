package es.pedrazamiguez.splittrip.domain.usecase.setting.impl

import es.pedrazamiguez.splittrip.domain.enums.BiometricCapability
import es.pedrazamiguez.splittrip.domain.service.BiometricAuthService
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetBiometricCapabilityUseCase

class GetBiometricCapabilityUseCaseImpl(
    private val biometricAuthService: BiometricAuthService
) : GetBiometricCapabilityUseCase {

    override fun invoke(): BiometricCapability = biometricAuthService.getBiometricCapability()
}
