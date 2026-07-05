package es.pedrazamiguez.splittrip.features.group.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.GroupSettlementOverviewUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowStatusStyle
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupSettlementOverviewUiState
import kotlinx.collections.immutable.toImmutableList

class GroupSettlementOverviewUiMapperImpl(
    private val formattingHelper: FormattingHelper,
    private val resourceProvider: ResourceProvider
) : GroupSettlementOverviewUiMapper {

    override fun toUiState(
        settlements: List<SettlementRecord>,
        memberProfiles: Map<String, User>,
        currentUserId: String
    ): GroupSettlementOverviewUiState {
        val rows = settlements.map { record ->
            toRowModel(record, memberProfiles, currentUserId)
        }

        val pending = rows.filter {
            it.status == SettlementStatus.SUGGESTED ||
                it.status == SettlementStatus.CONFIRMED_BY_PAYER
        }
        val disputed = rows.filter { it.status == SettlementStatus.DISPUTED }
        val resolved = rows.filter { it.status == SettlementStatus.RESOLVED }

        return GroupSettlementOverviewUiState(
            pendingSettlements = pending.toImmutableList(),
            disputedSettlements = disputed.toImmutableList(),
            resolvedSettlements = resolved.toImmutableList(),
            areAllSettlementsResolved = settlements.isNotEmpty() && rows.all { it.status == SettlementStatus.RESOLVED },
            isLoading = false
        )
    }

    private fun toRowModel(
        record: SettlementRecord,
        memberProfiles: Map<String, User>,
        currentUserId: String
    ): SettlementRowUiModel {
        val debtorName = resolveMemberName(record.settlement.fromUserId, memberProfiles, currentUserId)
        val creditorName = resolveMemberName(record.settlement.toUserId, memberProfiles, currentUserId)
        val isDebtor = record.settlement.fromUserId == currentUserId
        val isCreditor = record.settlement.toUserId == currentUserId

        return SettlementRowUiModel(
            settlementId = record.id,
            debtorId = record.settlement.fromUserId,
            creditorId = record.settlement.toUserId,
            debtorName = debtorName,
            creditorName = creditorName,
            formattedAmount = formatAmount(record.settlement.amount, record.settlement.currency),
            isCurrentUserDebtor = isDebtor,
            isCurrentUserCreditor = isCreditor,
            pocketTypeLabel = resolvePocketTypeLabel(record.settlement.sourcePocket),
            currencyCode = record.settlement.currency,
            statusLabel = resolveStatusLabel(record.status),
            statusChipStyle = resolveStatusStyle(record.status),
            canCurrentUserConfirm = canConfirm(record.status, isDebtor, isCreditor),
            canCurrentUserDispute = canDispute(record.status, isDebtor, isCreditor),
            disputedByCurrentUser = record.disputedBy == currentUserId,
            disputeReason = record.disputeReason,
            status = record.status
        )
    }

    private fun resolveMemberName(
        userId: String,
        profiles: Map<String, User>,
        currentUserId: String
    ): String {
        if (userId == currentUserId) {
            return resourceProvider.getString(DesignSystemR.string.balance_you)
        }
        return profiles[userId]?.displayName
            ?: resourceProvider.getString(DesignSystemR.string.user_pending_fallback)
    }

    private fun resolveStatusLabel(status: SettlementStatus): String {
        return when (status) {
            SettlementStatus.SUGGESTED ->
                resourceProvider.getString(R.string.settlement_overview_status_suggested)
            SettlementStatus.CONFIRMED_BY_PAYER ->
                resourceProvider.getString(R.string.settlement_overview_status_confirmed_by_payer)
            SettlementStatus.DISPUTED ->
                resourceProvider.getString(R.string.settlement_overview_status_disputed)
            SettlementStatus.RESOLVED ->
                resourceProvider.getString(R.string.settlement_overview_status_resolved)
        }
    }

    private fun resolveStatusStyle(status: SettlementStatus): SettlementRowStatusStyle {
        return when (status) {
            SettlementStatus.SUGGESTED -> SettlementRowStatusStyle.NEUTRAL
            SettlementStatus.CONFIRMED_BY_PAYER -> SettlementRowStatusStyle.WARNING
            SettlementStatus.DISPUTED -> SettlementRowStatusStyle.ERROR
            SettlementStatus.RESOLVED -> SettlementRowStatusStyle.SUCCESS
        }
    }

    private fun canConfirm(
        status: SettlementStatus,
        isDebtor: Boolean,
        isCreditor: Boolean
    ): Boolean {
        return when (status) {
            SettlementStatus.SUGGESTED -> isDebtor
            SettlementStatus.CONFIRMED_BY_PAYER -> isCreditor
            SettlementStatus.DISPUTED -> false
            SettlementStatus.RESOLVED -> false
        }
    }

    private fun canDispute(
        status: SettlementStatus,
        isDebtor: Boolean,
        isCreditor: Boolean
    ): Boolean {
        return when (status) {
            SettlementStatus.SUGGESTED, SettlementStatus.CONFIRMED_BY_PAYER ->
                isDebtor || isCreditor
            SettlementStatus.DISPUTED, SettlementStatus.RESOLVED -> false
        }
    }

    private fun resolvePocketTypeLabel(type: SettlementPocketType): String {
        return when (type) {
            SettlementPocketType.POCKET ->
                resourceProvider.getString(DesignSystemR.string.settlement_pocket_type_virtual)
            SettlementPocketType.CASH ->
                resourceProvider.getString(DesignSystemR.string.settlement_pocket_type_cash)
            SettlementPocketType.NET ->
                resourceProvider.getString(DesignSystemR.string.settlement_pocket_type_net)
        }
    }

    private fun formatAmount(cents: Long, currencyCode: String): String {
        return formattingHelper.formatCentsWithCurrency(cents, currencyCode)
    }
}
