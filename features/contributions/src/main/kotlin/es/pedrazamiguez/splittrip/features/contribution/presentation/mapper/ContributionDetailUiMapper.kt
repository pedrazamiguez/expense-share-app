package es.pedrazamiguez.splittrip.features.contribution.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberDisplay
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.contribution.R
import es.pedrazamiguez.splittrip.features.contribution.presentation.model.ContributionDetailUiModel
import java.math.BigDecimal

class ContributionDetailUiMapper(
    private val formattingHelper: FormattingHelper,
    private val resourceProvider: ResourceProvider,
    private val userUiMapper: UserUiMapper
) {

    fun map(
        contribution: Contribution,
        groupCurrency: String,
        memberProfiles: Map<String, User>,
        subunitsMap: Map<String, Subunit>,
        groupMemberIds: List<String>,
        currentUserId: String?
    ): ContributionDetailUiModel {
        val isCurrentUser = contribution.userId == currentUserId
        val youLabel = resourceProvider.getString(R.string.contribution_member_picker_you_label)
        val (contributorName, memberDisplay) = resolveMemberDisplay(
            userId = contribution.userId,
            memberProfiles = memberProfiles,
            groupMemberIds = groupMemberIds,
            currentUserId = currentUserId,
            youLabel = youLabel
        )
        val (creatorName, creatorDisplay) = resolveCreatorDisplay(
            createdBy = contribution.createdBy,
            memberProfiles = memberProfiles,
            groupMemberIds = groupMemberIds,
            currentUserId = currentUserId,
            youLabel = youLabel
        )
        val isForeign = contribution.currency != groupCurrency
        val (scopeLabel, scopeDescription, subunitName) = resolveScopeInfo(
            scope = contribution.contributionScope,
            subunitId = contribution.subunitId,
            subunitsMap = subunitsMap
        )

        return ContributionDetailUiModel(
            id = contribution.id,
            groupId = contribution.groupId,
            formattedAmount = formattingHelper.formatCentsWithCurrency(contribution.amount, contribution.currency),
            formattedEquivalentAmount = resolveEquivalentAmount(contribution, groupCurrency, isForeign),
            isForeignCurrency = isForeign,
            sourceCurrency = contribution.currency,
            formattedExchangeRate = resolveExchangeRate(contribution.exchangeRate, isForeign),
            dateText = formattingHelper.formatShortDate(contribution.contributionDate ?: contribution.createdAt),
            createdAtText = formattingHelper.formatShortDate(contribution.createdAt),
            memberDisplay = memberDisplay,
            isCurrentUser = isCurrentUser,
            contributorName = contributorName,
            contributedByText = resolveContributedByText(isCurrentUser, contributorName),
            createdByText = resolveCreatedByText(contribution.createdBy, currentUserId, creatorName),
            creatorDisplay = creatorDisplay,
            scopeLabel = scopeLabel,
            scopeDescription = scopeDescription,
            scopeType = contribution.contributionScope,
            subunitName = subunitName,
            isLinkedContribution = contribution.linkedExpenseId != null,
            linkedExpenseId = contribution.linkedExpenseId,
            isSettlementContribution = contribution.linkedSettlementId != null,
            syncStatus = contribution.syncStatus
        )
    }

    private fun resolveMemberDisplay(
        userId: String,
        memberProfiles: Map<String, User>,
        groupMemberIds: List<String>,
        currentUserId: String?,
        youLabel: String
    ): Pair<String, MemberDisplay> {
        val name = userUiMapper.mapToDisplayName(
            user = memberProfiles[userId],
            fallbackUserId = userId,
            currentUserId = currentUserId,
            youLabel = youLabel
        )
        val display = if (userId !in groupMemberIds) {
            MemberDisplay.Former(userId, name)
        } else {
            MemberDisplay.Active(userId, name)
        }
        return name to display
    }

    private fun resolveCreatorDisplay(
        createdBy: String,
        memberProfiles: Map<String, User>,
        groupMemberIds: List<String>,
        currentUserId: String?,
        youLabel: String
    ): Pair<String, MemberDisplay?> {
        if (createdBy.isBlank()) return "" to null
        val name = userUiMapper.mapToDisplayName(
            user = memberProfiles[createdBy],
            fallbackUserId = createdBy,
            currentUserId = currentUserId,
            youLabel = youLabel
        )
        val display = if (createdBy !in groupMemberIds) {
            MemberDisplay.Former(createdBy, name)
        } else {
            MemberDisplay.Active(createdBy, name)
        }
        return name to display
    }

    private fun resolveScopeInfo(
        scope: PayerType,
        subunitId: String?,
        subunitsMap: Map<String, Subunit>
    ): Triple<String, String, String?> {
        val subunitName = subunitId?.let { subunitsMap[it]?.name }
        val (label, description) = when (scope) {
            PayerType.USER -> {
                resourceProvider.getString(R.string.contribution_detail_scope_personal) to
                    resourceProvider.getString(R.string.contribution_detail_scope_personal_desc)
            }
            PayerType.GROUP -> {
                resourceProvider.getString(R.string.contribution_detail_scope_group) to
                    resourceProvider.getString(R.string.contribution_detail_scope_group_desc)
            }
            PayerType.SUBUNIT -> {
                val name = subunitName.orEmpty()
                name to resourceProvider.getString(R.string.contribution_detail_scope_subunit_desc, name)
            }
        }
        return Triple(label, description, subunitName)
    }

    private fun resolveEquivalentAmount(
        contribution: Contribution,
        groupCurrency: String,
        isForeign: Boolean
    ): String {
        return if (isForeign && contribution.equivalentBaseAmount != null) {
            formattingHelper.formatCentsWithCurrency(contribution.equivalentBaseAmount!!, groupCurrency)
        } else {
            ""
        }
    }

    private fun resolveExchangeRate(exchangeRate: BigDecimal?, isForeign: Boolean): String? {
        return if (isForeign && exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) != 0) {
            formattingHelper.formatRateForDisplay(exchangeRate.toPlainString())
        } else {
            null
        }
    }

    private fun resolveContributedByText(isCurrentUser: Boolean, contributorName: String): String {
        return if (isCurrentUser) {
            resourceProvider.getString(R.string.contribution_detail_contributed_by_you)
        } else {
            resourceProvider.getString(R.string.contribution_detail_contributed_by, contributorName)
        }
    }

    private fun resolveCreatedByText(createdBy: String, currentUserId: String?, creatorName: String): String {
        return if (createdBy == currentUserId) {
            resourceProvider.getString(R.string.contribution_detail_created_by_you)
        } else {
            resourceProvider.getString(R.string.contribution_detail_created_by, creatorName)
        }
    }
}
