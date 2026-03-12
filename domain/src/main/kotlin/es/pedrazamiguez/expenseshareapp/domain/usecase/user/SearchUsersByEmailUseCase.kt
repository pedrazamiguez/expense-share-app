package es.pedrazamiguez.expenseshareapp.domain.usecase.user

import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository

/**
 * Searches for users by email address.
 *
 * Used during group creation/editing to find users to add as members.
 * The current user is automatically excluded from results.
 */
class SearchUsersByEmailUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String): Result<List<User>> = runCatching {
        userRepository.searchUsersByEmail(email)
    }
}

