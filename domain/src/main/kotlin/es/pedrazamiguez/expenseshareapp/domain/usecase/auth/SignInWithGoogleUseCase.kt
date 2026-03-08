package es.pedrazamiguez.expenseshareapp.domain.usecase.auth

import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase

class SignInWithGoogleUseCase(
    private val authenticationService: AuthenticationService,
    private val userRepository: UserRepository,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
) {

    suspend operator fun invoke(
        idToken: String,
        email: String,
        displayName: String?,
        photoUrl: String?
    ): Result<String> = runCatching {
        val userId = authenticationService
            .signInWithGoogle(idToken)
            .getOrThrow()

        val user = User(
            userId = userId,
            email = email,
            displayName = displayName,
            profileImagePath = photoUrl
        )

        userRepository.saveGoogleUser(user)

        registerDeviceTokenUseCase()

        userId
    }
}

