package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.LocalDatabaseCleaner
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.UnregisterDeviceTokenUseCase

class SignOutUseCase(
    private val unregisterDeviceTokenUseCase: UnregisterDeviceTokenUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val localDatabaseCleaner: LocalDatabaseCleaner,
    private val authenticationService: AuthenticationService
) {

    suspend operator fun invoke(): Result<Unit> {
        // Result of clearing local preferences + database
        var cleanupResult: Result<Unit> = Result.success(Unit)
        // Result of remote sign-out (Firebase Auth)
        var signOutResult: Result<Unit> = Result.success(Unit)

        try {
            // 1. Unregister device token (best-effort — failure must not abort sign-out)
            runCatching { unregisterDeviceTokenUseCase() }

            // 2. Clear local preferences (DataStore)
            cleanupResult = runCatching {
                preferenceRepository.clearAll()
            }
        } finally {
            // 3. Sign out from Firebase Auth (must always be attempted;
            //    also tears down remote snapshot listeners before Room is cleared)
            signOutResult = authenticationService.signOut()

            // 4. Clear local database (Room) — safe now that listeners are stopped
            runCatching { localDatabaseCleaner.clearAll() }
                .onFailure { if (cleanupResult.isSuccess) cleanupResult = Result.failure(it) }
        }

        // Prioritize sign-out failure; otherwise surface cleanup result
        return if (signOutResult.isFailure) {
            signOutResult
        } else {
            cleanupResult
        }
    }
}


