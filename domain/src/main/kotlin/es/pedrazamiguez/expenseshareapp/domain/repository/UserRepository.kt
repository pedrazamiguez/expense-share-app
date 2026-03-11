package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.User

interface UserRepository {
    suspend fun saveGoogleUser(user: User): Result<Unit>

    /**
     * Returns the current authenticated user's profile.
     *
     * Checks the local cache (Room) first, then fetches from the cloud
     * if not found locally, and caches the result.
     */
    suspend fun getCurrentUserProfile(): User?

    /**
     * Returns a map of userId → [User] for the given IDs.
     *
     * Checks the local cache (Room) first, then fetches any missing users
     * from the cloud and caches them locally for future lookups.
     */
    suspend fun getUsersByIds(userIds: List<String>): Map<String, User>
}
