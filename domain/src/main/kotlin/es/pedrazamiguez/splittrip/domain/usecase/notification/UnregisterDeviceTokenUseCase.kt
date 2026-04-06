package es.pedrazamiguez.splittrip.domain.usecase.notification

import es.pedrazamiguez.splittrip.domain.repository.NotificationRepository

class UnregisterDeviceTokenUseCase(
    private val notificationRepository: NotificationRepository
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        notificationRepository.unregisterCurrentDevice()
    }
}
