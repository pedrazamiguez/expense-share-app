package es.pedrazamiguez.splittrip.domain.usecase.setting.impl

import es.pedrazamiguez.splittrip.domain.model.DeveloperInfo
import es.pedrazamiguez.splittrip.domain.repository.AppConfigRepository
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetDeveloperInfoUseCase
import kotlinx.coroutines.flow.StateFlow

class GetDeveloperInfoUseCaseImpl(
    private val appConfigRepository: AppConfigRepository
) : GetDeveloperInfoUseCase {

    override operator fun invoke(): StateFlow<DeveloperInfo> = appConfigRepository.developerInfo
}
