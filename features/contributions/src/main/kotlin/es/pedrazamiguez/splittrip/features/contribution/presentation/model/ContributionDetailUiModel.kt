package es.pedrazamiguez.splittrip.features.contribution.presentation.model

import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberDisplay
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus

data class ContributionDetailUiModel(
    val id: String,
    val groupId: String,
    val formattedAmount: String,
    val formattedEquivalentAmount: String = "",
    val isForeignCurrency: Boolean = false,
    val sourceCurrency: String = "",
    val formattedExchangeRate: String? = null,
    val dateText: String = "",
    val createdAtText: String = "",
    val memberDisplay: MemberDisplay = MemberDisplay.Active("", ""),
    val isCurrentUser: Boolean = false,
    val contributorName: String = "",
    val contributedByText: String = "",
    val createdByText: String = "",
    val creatorDisplay: MemberDisplay? = null,
    val scopeLabel: String = "",
    val scopeDescription: String = "",
    val scopeType: PayerType = PayerType.USER,
    val subunitName: String? = null,
    val isLinkedContribution: Boolean = false,
    val linkedExpenseId: String? = null,
    val isSettlementContribution: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
