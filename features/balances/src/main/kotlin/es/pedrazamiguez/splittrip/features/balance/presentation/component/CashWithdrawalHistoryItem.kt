package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashWithdrawalUiModel

@Composable
fun CashWithdrawalHistoryItem(withdrawal: CashWithdrawalUiModel, modifier: Modifier = Modifier) {
    FlatCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalAtm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(12.dp))
            WithdrawalDetailColumn(withdrawal = withdrawal, modifier = Modifier.weight(1f))
            WithdrawalAmountColumn(withdrawal = withdrawal)
        }
    }
}

@Composable
private fun WithdrawalDetailColumn(withdrawal: CashWithdrawalUiModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        WithdrawalPrimaryLabel(withdrawal)
        WithdrawalTitleSubtitle(withdrawal.title)
        WithdrawalLoggedByLine(withdrawal.createdByDisplayName)
        WithdrawalScopeBadge(withdrawal)
        WithdrawalDateLine(withdrawal.dateText)
    }
}

@Composable
private fun WithdrawalPrimaryLabel(withdrawal: CashWithdrawalUiModel) {
    Text(
        text = if (withdrawal.isCurrentUser) {
            stringResource(R.string.balances_cash_withdrawal_by_you)
        } else {
            stringResource(R.string.balances_cash_withdrawal_by, withdrawal.displayName)
        },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun WithdrawalTitleSubtitle(title: String?) {
    if (!title.isNullOrBlank()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WithdrawalLoggedByLine(createdByDisplayName: String?) {
    if (createdByDisplayName != null) {
        Text(
            text = stringResource(R.string.balances_logged_by, createdByDisplayName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WithdrawalScopeBadge(withdrawal: CashWithdrawalUiModel) {
    if (withdrawal.scopeLabel != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = when {
                    withdrawal.isSubunitWithdrawal -> Icons.Outlined.Group
                    withdrawal.isGroupWithdrawal -> Icons.Outlined.Groups
                    else -> Icons.Outlined.Person
                },
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = withdrawal.scopeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun WithdrawalDateLine(dateText: String) {
    if (dateText.isNotBlank()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WithdrawalAmountColumn(withdrawal: CashWithdrawalUiModel) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = withdrawal.formattedAmount,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary
        )
        if (withdrawal.isForeignCurrency && withdrawal.formattedDeducted.isNotBlank()) {
            Text(
                text = withdrawal.formattedDeducted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
