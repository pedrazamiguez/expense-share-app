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

    suspend operator fun invoke(): Result<Unit> = runCatching {
        // 1. Unregister device token (best-effort — failure must not abort sign-out)
        unregisterDeviceTokenUseCase()

        // 2. Clear local preferences (DataStore)
        preferenceRepository.clearAll()

        // 3. Clear local database (Room)
        localDatabaseCleaner.clearAll()

        // 4. Sign out from Firebase Auth
        authenticationService.signOut()
            .getOrThrow()
    }
}


