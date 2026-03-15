package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository

class UnregisterDeviceTokenUseCase(
    private val notificationRepository: NotificationRepository
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        notificationRepository.unregisterCurrentDevice()
    }
}
