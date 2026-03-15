package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase

class SignInWithGoogleUseCase(
    private val authenticationService: AuthenticationService,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
) {

    suspend operator fun invoke(idToken: String): Result<String> = runCatching {
        val user = authenticationService
            .signInWithGoogle(idToken)
            .getOrThrow()

        registerDeviceTokenUseCase()
            .onFailure {
                // Device token registration is best-effort and should not
                // cause the Google sign-in flow to fail.
            }

        user.userId
    }
}
