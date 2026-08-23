package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface SetBiometricLockEnabledUseCase : UseCase {
    suspend operator fun invoke(enabled: Boolean)
}
