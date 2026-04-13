package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CreditCardPay
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Link
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Sitemap
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.User
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Wallet
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SyncStatusBadge
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ContributionUiModel

@Composable
fun ContributionHistoryItem(contribution: ContributionUiModel, modifier: Modifier = Modifier) {
    val isLinked = contribution.isLinkedContribution
    Box(modifier = modifier) {
        FlatCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isLinked) TablerIcons.Outline.CreditCardPay else TablerIcons.Outline.Wallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                ContributionDetailColumn(contribution = contribution, modifier = Modifier.weight(1f))
                Text(
                    text = "+${contribution.formattedAmount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        SyncStatusBadge(syncStatus = contribution.syncStatus)
    }
}

@Composable
private fun ContributionDetailColumn(contribution: ContributionUiModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ContributionPrimaryLabel(contribution)
        ContributionLoggedByLine(contribution.createdByDisplayName)
        if (contribution.isLinkedContribution) {
            LinkedContributionBadge()
        }
        if (contribution.scopeLabel != null) {
            ContributionScopeBadge(contribution = contribution)
        }
        ContributionDateLine(contribution.dateText)
    }
}

@Composable
private fun ContributionPrimaryLabel(contribution: ContributionUiModel) {
    Text(
        text = if (contribution.isLinkedContribution) {
            if (contribution.isCurrentUser) {
                stringResource(R.string.balances_linked_contribution_by_you)
            } else {
                stringResource(R.string.balances_linked_contribution_by, contribution.displayName)
            }
        } else {
            if (contribution.isCurrentUser) {
                stringResource(R.string.balances_contribution_by_you)
            } else {
                stringResource(R.string.balances_contribution_by, contribution.displayName)
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ContributionLoggedByLine(createdByDisplayName: String?) {
    if (createdByDisplayName != null) {
        Text(
            text = stringResource(R.string.balances_logged_by, createdByDisplayName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContributionDateLine(dateText: String) {
    if (dateText.isNotBlank()) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LinkedContributionBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = TablerIcons.Outline.Link,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.balances_linked_contribution_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ContributionScopeBadge(contribution: ContributionUiModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = when {
                contribution.isSubunitContribution -> TablerIcons.Outline.Sitemap
                contribution.isGroupContribution -> TablerIcons.Outline.UsersGroup
                else -> TablerIcons.Outline.User
            },
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = contribution.scopeLabel.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
