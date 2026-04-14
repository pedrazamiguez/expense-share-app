package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService

/**
 * Use case for deleting a manual contribution from a group.
 *
 * Validates that the caller is a group member before delegating the offline-first
 * delete to [ContributionRepository]. Linked (auto-generated, out-of-pocket)
 * contributions are excluded from this flow at the UI layer and should never
 * reach this use case.
 *
 * @param groupId The ID of the group owning the contribution.
 * @param contributionId The ID of the contribution to delete.
 * @throws es.pedrazamiguez.splittrip.domain.exception.NotGroupMemberException if the caller is
 * not a member of the group.
 */
class DeleteContributionUseCase(
    private val contributionRepository: ContributionRepository,
    private val groupMembershipService: GroupMembershipService
) {
    suspend operator fun invoke(groupId: String, contributionId: String) {
        groupMembershipService.requireMembership(groupId)
        contributionRepository.deleteContribution(groupId, contributionId)
    }
}
