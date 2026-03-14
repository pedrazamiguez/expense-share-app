package es.pedrazamiguez.expenseshareapp.domain.usecase.notification

import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.first

class SyncPendingTokenUseCase(
    private val notificationRepository: NotificationRepository
) {

    suspend operator fun invoke(): Result<Unit> = runCatching {
        val pendingToken = notificationRepository.getPendingTokenFlow().first()
            ?: return Result.success(Unit)

        notificationRepository.registerDeviceToken(pendingToken)
    }

}

