package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow

interface GetBiometricLockEnabledUseCase : UseCase {
    operator fun invoke(): Flow<Boolean>
}
