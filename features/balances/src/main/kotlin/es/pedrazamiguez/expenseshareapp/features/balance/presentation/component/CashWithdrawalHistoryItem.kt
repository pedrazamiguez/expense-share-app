package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashWithdrawalUiModel

@Composable
fun CashWithdrawalHistoryItem(withdrawal: CashWithdrawalUiModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
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
        // Primary line: title if present, otherwise the "withdrew cash" label
        val primaryText = withdrawal.title ?: if (withdrawal.isCurrentUser) {
            stringResource(R.string.balances_cash_withdrawal_by_you)
        } else {
            stringResource(R.string.balances_cash_withdrawal_by, withdrawal.displayName)
        }
        Text(
            text = primaryText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Secondary line: "by {name}" when title is present
        if (withdrawal.title != null) {
            Text(
                text = if (withdrawal.isCurrentUser) {
                    stringResource(R.string.balances_cash_withdrawal_by_you)
                } else {
                    stringResource(R.string.balances_cash_withdrawal_by, withdrawal.displayName)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
        if (withdrawal.dateText.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = withdrawal.dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
