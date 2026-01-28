package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.repository.DeviceRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository

class RegisterDeviceTokenUseCase(
    private val deviceRepository: DeviceRepository,
    private val notificationRepository: NotificationRepository
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        val token = deviceRepository
            .getDeviceToken()
            .getOrThrow()
        notificationRepository.registerDeviceToken(token)
    }

}
