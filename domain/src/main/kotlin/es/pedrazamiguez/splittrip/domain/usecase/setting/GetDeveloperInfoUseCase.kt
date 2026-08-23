package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.model.DeveloperInfo
import es.pedrazamiguez.splittrip.domain.usecase.UseCase
import kotlinx.coroutines.flow.StateFlow

interface GetDeveloperInfoUseCase : UseCase {
    operator fun invoke(): StateFlow<DeveloperInfo>
}
