package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService

class AddContributionUseCase(
    private val contributionRepository: ContributionRepository,
    private val groupMembershipService: GroupMembershipService,
    private val contributionValidationService: ContributionValidationService,
    private val subunitRepository: SubunitRepository,
    private val authenticationService: AuthenticationService
) {
    suspend operator fun invoke(groupId: String, contribution: Contribution) {
        groupMembershipService.requireMembership(groupId)

        // Validate amount
        val amountResult = contributionValidationService.validateAmount(contribution.amount)
        if (amountResult is ContributionValidationService.ValidationResult.Invalid) {
            throw IllegalArgumentException("Invalid contribution amount: ${amountResult.error}")
        }

        // Validate subunit assignment
        if (contribution.subunitId != null) {
            val currentUserId = authenticationService.requireUserId()
            val groupSubunits = subunitRepository.getGroupSubunits(groupId)
            val subunitResult = contributionValidationService.validateSubunit(
                subunitId = contribution.subunitId,
                userId = currentUserId,
                groupSubunits = groupSubunits
            )
            if (subunitResult is ContributionValidationService.ValidationResult.Invalid) {
                throw IllegalArgumentException("Invalid subunit assignment: ${subunitResult.error}")
            }
        }

        contributionRepository.addContribution(groupId, contribution)
    }
}
