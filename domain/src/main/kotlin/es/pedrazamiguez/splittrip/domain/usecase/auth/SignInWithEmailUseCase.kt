package es.pedrazamiguez.splittrip.domain.usecase.auth

import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.notification.RegisterDeviceTokenUseCase

class SignInWithEmailUseCase(
    private val authenticationService: AuthenticationService,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
) {

    suspend operator fun invoke(email: String, password: String): Result<String> = runCatching {
        val userId = authenticationService
            .signIn(email, password)
            .getOrThrow()

        registerDeviceTokenUseCase()
            .onFailure {
                // Device token registration is best-effort and should not
                // cause the email sign-in flow to fail.
            }

        userId
    }
}
