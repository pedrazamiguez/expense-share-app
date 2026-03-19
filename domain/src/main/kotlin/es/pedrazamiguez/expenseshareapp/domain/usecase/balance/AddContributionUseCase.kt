package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
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

        // Validate contribution scope
        when (contribution.contributionScope) {
            PayerType.SUBUNIT -> {
                // SUBUNIT requires a valid subunit + user membership — fetch subunits
                val currentUserId = authenticationService.requireUserId()
                val groupSubunits = subunitRepository.getGroupSubunits(groupId)
                val scopeResult = contributionValidationService.validateContributionScope(
                    contributionScope = contribution.contributionScope,
                    subunitId = contribution.subunitId,
                    userId = currentUserId,
                    groupSubunits = groupSubunits
                )
                if (scopeResult is ContributionValidationService.ValidationResult.Invalid) {
                    throw IllegalArgumentException("Invalid contribution scope: ${scopeResult.error}")
                }
            }

            else -> {
                // GROUP / USER must not have a subunitId — no I/O needed
                if (contribution.subunitId != null) {
                    throw IllegalArgumentException(
                        "Invalid contribution scope: ${ContributionValidationService.ValidationError.INVALID_SUBUNIT_FOR_SCOPE}"
                    )
                }
            }
        }

        contributionRepository.addContribution(groupId, contribution)
    }
}
