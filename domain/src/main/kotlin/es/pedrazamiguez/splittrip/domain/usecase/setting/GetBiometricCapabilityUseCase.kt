package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.enums.BiometricCapability
import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface GetBiometricCapabilityUseCase : UseCase {
    operator fun invoke(): BiometricCapability
}
