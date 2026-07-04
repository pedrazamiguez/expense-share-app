package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.AlertTriangle
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ArrowRight
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.SecondaryButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowUiModel

@Composable
internal fun GroupSettlementItem(
    settlement: SettlementRowUiModel,
    onConfirm: () -> Unit,
    onDispute: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlatCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GroupSettlementStatusChip(
                    label = settlement.statusLabel,
                    style = settlement.statusChipStyle
                )
            }

            SettlementPartyRow(settlement = settlement)

            if (settlement.status == es.pedrazamiguez.splittrip.domain.model.SettlementStatus.DISPUTED) {
                SettlementDisputeBanner(reason = settlement.disputeReason)
            }

            if (settlement.canCurrentUserConfirm || settlement.canCurrentUserDispute) {
                HorizontalDivider()
                SettlementActionButtons(
                    canConfirm = settlement.canCurrentUserConfirm,
                    canDispute = settlement.canCurrentUserDispute,
                    onConfirm = onConfirm,
                    onDispute = onDispute
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.ExtraSmall))
        }
    }
}

@Composable
private fun SettlementPartyRow(settlement: SettlementRowUiModel) {
    val debtorColor = if (settlement.isCurrentUserDebtor) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val creditorColor = if (settlement.isCurrentUserCreditor) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = settlement.debtorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = boldIfCurrent(settlement.isCurrentUserDebtor),
                color = debtorColor
            )
            Icon(
                TablerIcons.Outline.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = settlement.creditorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = boldIfCurrent(settlement.isCurrentUserCreditor),
                color = creditorColor
            )
        }
        Text(
            text = settlement.formattedAmount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettlementDisputeBanner(reason: String?) {
    if (reason == null) return

    HorizontalDivider()
    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            TablerIcons.Outline.AlertTriangle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = MaterialTheme.spacing.ExtraSmall)
        )
        BodyText(
            text = reason,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SettlementActionButtons(
    canConfirm: Boolean,
    canDispute: Boolean,
    onConfirm: () -> Unit,
    onDispute: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        if (canConfirm) {
            SecondaryButton(
                text = stringResource(R.string.settlement_overview_confirm),
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
        }
        if (canDispute) {
            SecondaryButton(
                text = stringResource(R.string.settlement_overview_dispute),
                onClick = onDispute,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun boldIfCurrent(isCurrentUser: Boolean): FontWeight =
    if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
