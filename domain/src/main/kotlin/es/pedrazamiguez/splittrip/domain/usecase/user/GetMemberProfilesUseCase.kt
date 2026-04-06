package es.pedrazamiguez.splittrip.domain.usecase.user

import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.domain.repository.UserRepository

/**
 * Fetches user profile information for a list of user IDs.
 *
 * Returns a `Map<String, User>` keyed by userId — the caller (Mapper)
 * decides which fields to use (displayName, profileImagePath, etc.).
 *
 * Local-first: checks Room cache, backfills missing entries from Firestore.
 */
class GetMemberProfilesUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userIds: List<String>): Map<String, User> = userRepository.getUsersByIds(userIds)
}
