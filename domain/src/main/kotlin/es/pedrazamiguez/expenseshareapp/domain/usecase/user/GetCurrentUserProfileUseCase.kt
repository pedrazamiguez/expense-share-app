package es.pedrazamiguez.expenseshareapp.domain.usecase.user

import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository

/**
 * Fetches the current authenticated user's profile.
 *
 * Returns the cached profile when available and delegates to the repository
 * for any necessary Firestore fetches when the local profile is missing
 * or incomplete.
 */
class GetCurrentUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): User? = userRepository.getCurrentUserProfile()
}
