package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleanerService
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UnregisterDeviceTokenUseCase

class SignOutUseCase(
    private val unregisterDeviceTokenUseCase: UnregisterDeviceTokenUseCase,
    private val localDatabaseCleaner: LocalDatabaseCleanerService,
    private val authenticationService: AuthenticationService
) {

    suspend operator fun invoke(): Result<Unit> {
        // 1. Unregister device token (best-effort — failure must not abort sign-out)
        runCatching { unregisterDeviceTokenUseCase() }

        // 2. Sign out from Firebase Auth
        //    Also tears down remote snapshot listeners before Room is cleared.
        val signOutResult = authenticationService.signOut()

        // 3. Clear local database (Room) only after successful sign-out, ensuring
        //    that remote snapshot listeners have been torn down first.
        //    DataStore is NOT cleared: keys are scoped by userId, so no cross-user
        //    leakage is possible. Preserving DataStore lets the user keep their
        //    preferences (default currency, MRU lists, onboarding state) if they
        //    sign back in on the same device.
        val cleanupResult = if (signOutResult.isSuccess) {
            runCatching { localDatabaseCleaner.clearAll() }
        } else {
            Result.success(Unit)
        }

        // Prioritize sign-out failure; otherwise surface cleanup result
        return if (signOutResult.isFailure) {
            signOutResult
        } else {
            cleanupResult
        }
    }
}
