package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.provider.DeviceTokenProvider
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository

class RegisterDeviceTokenUseCase(
    private val deviceTokenProvider: DeviceTokenProvider,
    private val notificationRepository: NotificationRepository,
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        val token = deviceTokenProvider
            .getDeviceToken()
            .getOrThrow()
        notificationRepository.registerDeviceToken(token)
    }

}
