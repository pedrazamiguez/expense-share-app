package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService

/**
 * Use case for deleting a cash withdrawal from a group.
 *
 * Validates that the caller is a group member before delegating the offline-first
 * delete to [CashWithdrawalRepository].
 *
 * @param groupId The ID of the group owning the withdrawal.
 * @param withdrawalId The ID of the cash withdrawal to delete.
 * @throws es.pedrazamiguez.splittrip.domain.exception.NotGroupMemberException if the caller is
 * not a member of the group.
 */
class DeleteCashWithdrawalUseCase(
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val groupMembershipService: GroupMembershipService
) {
    suspend operator fun invoke(groupId: String, withdrawalId: String) {
        groupMembershipService.requireMembership(groupId)
        cashWithdrawalRepository.deleteWithdrawal(groupId, withdrawalId)
    }
}
