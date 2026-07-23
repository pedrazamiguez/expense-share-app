package es.pedrazamiguez.splittrip.features.settlement.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.ConsensusChipStyle
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SettlementConsensusItemUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class SettlementConsensusUiMapper(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider
) {
    fun toConsensusItems(
        settlements: List<SettlementRecord>,
        currentUserId: String,
        groupCreatorId: String,
        memberProfiles: Map<String, User>
    ): ImmutableList<SettlementConsensusItemUiModel> {
        return settlements
            .filter { record ->
                record.status in ACTIVE_STATUSES &&
                    (record.settlement.fromUserId == currentUserId || record.settlement.toUserId == currentUserId)
            }
            .sortedWith(
                compareBy { record ->
                    when (record.status) {
                        SettlementStatus.DISPUTED -> 0
                        SettlementStatus.CONFIRMED_BY_PAYER -> 1
                        SettlementStatus.SUGGESTED -> 2
                        else -> 3
                    }
                }
            )
            .map { record ->
                toConsensusItem(
                    record = record,
                    currentUserId = currentUserId,
                    groupCreatorId = groupCreatorId,
                    memberProfiles = memberProfiles
                )
            }
            .toImmutableList()
    }

    private fun toConsensusItem(
        record: SettlementRecord,
        currentUserId: String,
        groupCreatorId: String,
        memberProfiles: Map<String, User>
    ): SettlementConsensusItemUiModel {
        val isCurrentUserPayer = record.settlement.fromUserId == currentUserId
        val counterpartyId = if (isCurrentUserPayer) record.settlement.toUserId else record.settlement.fromUserId
        val counterpartyName = memberProfiles[counterpartyId]?.displayName ?: counterpartyId

        val directionLabel = if (isCurrentUserPayer) {
            resourceProvider.getString(R.string.your_position_settlement_you_owe, counterpartyName)
        } else {
            resourceProvider.getString(R.string.your_position_settlement_owes_you, counterpartyName)
        }

        val formattedAmount = formatCurrencyAmount(
            amount = record.settlement.amount,
            currencyCode = record.settlement.currency,
            locale = localeProvider.getCurrentLocale()
        )

        val statusDetails = resolveStatusDetails(record.status)
        val actions = resolveActionCapabilities(
            status = record.status,
            isCurrentUserPayer = isCurrentUserPayer,
            isPayee = record.settlement.toUserId == currentUserId,
            isGroupCreator = currentUserId == groupCreatorId
        )

        return SettlementConsensusItemUiModel(
            settlementId = record.id,
            counterpartyName = counterpartyName,
            formattedAmount = formattedAmount,
            currencyCode = record.settlement.currency,
            pocketTypeLabel = resolvePocketTypeLabel(record.settlement.sourcePocket),
            directionLabel = directionLabel,
            statusLabel = statusDetails.label,
            statusChipStyle = statusDetails.style,
            isCurrentUserPayer = isCurrentUserPayer,
            canConfirm = actions.canConfirm,
            confirmLabel = actions.confirmLabel,
            canDispute = actions.canDispute,
            disputeReason = record.disputeReason,
            status = record.status
        )
    }

    private fun resolvePocketTypeLabel(sourcePocket: SettlementPocketType): String = when (sourcePocket) {
        SettlementPocketType.POCKET -> resourceProvider.getString(R.string.your_position_settlement_pocket_pocket)
        SettlementPocketType.CASH -> resourceProvider.getString(R.string.your_position_settlement_pocket_cash)
        SettlementPocketType.NET -> resourceProvider.getString(R.string.your_position_settlement_pocket_net)
    }

    private fun resolveStatusDetails(status: SettlementStatus): StatusDetails = when (status) {
        SettlementStatus.SUGGESTED -> StatusDetails(
            label = resourceProvider.getString(R.string.your_position_settlement_status_suggested),
            style = ConsensusChipStyle.SUGGESTED
        )
        SettlementStatus.CONFIRMED_BY_PAYER -> StatusDetails(
            label = resourceProvider.getString(R.string.your_position_settlement_status_confirmed_payer),
            style = ConsensusChipStyle.IN_PROGRESS
        )
        SettlementStatus.DISPUTED -> StatusDetails(
            label = resourceProvider.getString(R.string.your_position_settlement_status_disputed),
            style = ConsensusChipStyle.DISPUTED
        )
        SettlementStatus.RESOLVED -> StatusDetails(label = "", style = ConsensusChipStyle.SUGGESTED)
    }

    private fun resolveActionCapabilities(
        status: SettlementStatus,
        isCurrentUserPayer: Boolean,
        isPayee: Boolean,
        isGroupCreator: Boolean
    ): ActionCapabilities = when (status) {
        SettlementStatus.SUGGESTED -> ActionCapabilities(
            canConfirm = isCurrentUserPayer,
            confirmLabel = if (isCurrentUserPayer) {
                resourceProvider.getString(R.string.your_position_settlement_mark_paid)
            } else {
                ""
            },
            canDispute = true
        )
        SettlementStatus.CONFIRMED_BY_PAYER -> ActionCapabilities(
            canConfirm = isPayee,
            confirmLabel = if (isPayee) {
                resourceProvider.getString(R.string.your_position_settlement_confirm_receipt)
            } else {
                ""
            },
            canDispute = isPayee
        )
        SettlementStatus.DISPUTED -> ActionCapabilities(
            canConfirm = isPayee || isGroupCreator,
            confirmLabel = if (isPayee || isGroupCreator) {
                resourceProvider.getString(R.string.your_position_settlement_resolve_dispute)
            } else {
                ""
            },
            canDispute = false
        )
        SettlementStatus.RESOLVED -> ActionCapabilities(canConfirm = false, confirmLabel = "", canDispute = false)
    }

    private data class StatusDetails(val label: String, val style: ConsensusChipStyle)

    private data class ActionCapabilities(val canConfirm: Boolean, val confirmLabel: String, val canDispute: Boolean)

    companion object {
        private val ACTIVE_STATUSES = setOf(
            SettlementStatus.SUGGESTED,
            SettlementStatus.CONFIRMED_BY_PAYER,
            SettlementStatus.DISPUTED
        )
    }
}
