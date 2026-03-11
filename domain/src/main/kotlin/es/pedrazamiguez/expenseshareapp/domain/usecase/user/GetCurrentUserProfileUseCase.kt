package es.pedrazamiguez.expenseshareapp.domain.usecase.user

import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository

/**
 * Fetches the current authenticated user's profile.
 *
 * Local-first: checks Room cache, backfills from Firestore if missing.
 */
class GetCurrentUserProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User? {
        return userRepository.getCurrentUserProfile()
    }
}

