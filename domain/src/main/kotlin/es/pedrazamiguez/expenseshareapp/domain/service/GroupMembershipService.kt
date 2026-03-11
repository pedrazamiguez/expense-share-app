package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.exception.NotGroupMemberException
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

/**
 * Domain service that validates whether the authenticated user is a member
 * of a given group before allowing write operations.
 *
 * This is a Business Rule (validation), NOT a User Action — hence it lives
 * in a Domain Service, not a UseCase.
 */
class GroupMembershipService(
    private val groupRepository: GroupRepository,
    private val authenticationService: AuthenticationService
) {

    /**
     * Verifies that the current authenticated user is a member of the specified group.
     *
     * @param groupId The group to validate membership against.
     * @throws NotGroupMemberException if the user is not a member or the group does not exist.
     * @throws IllegalStateException if no user is authenticated.
     */
    suspend fun requireMembership(groupId: String) {
        val userId = authenticationService.requireUserId()
        val group = groupRepository.getGroupById(groupId)
            ?: throw NotGroupMemberException(groupId = groupId, userId = userId)

        if (userId !in group.members) {
            throw NotGroupMemberException(groupId = groupId, userId = userId)
        }
    }
}

