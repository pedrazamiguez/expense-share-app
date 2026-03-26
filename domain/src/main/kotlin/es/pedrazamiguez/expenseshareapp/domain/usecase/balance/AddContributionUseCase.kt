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
        require(amountResult !is ContributionValidationService.ValidationResult.Invalid) {
            val invalid = amountResult as ContributionValidationService.ValidationResult.Invalid
            "Invalid contribution amount: ${invalid.error}"
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
                require(scopeResult !is ContributionValidationService.ValidationResult.Invalid) {
                    val invalid =
                        scopeResult as ContributionValidationService.ValidationResult.Invalid
                    "Invalid contribution scope: ${invalid.error}"
                }
            }

            else -> {
                // GROUP / USER must not have a subunitId — no I/O needed
                val error =
                    ContributionValidationService.ValidationError.INVALID_SUBUNIT_FOR_SCOPE
                require(contribution.subunitId == null) {
                    "Invalid contribution scope: $error"
                }
            }
        }

        contributionRepository.addContribution(groupId, contribution)
    }
}
