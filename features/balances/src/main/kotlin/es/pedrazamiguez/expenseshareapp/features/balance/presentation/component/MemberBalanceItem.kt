package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.MemberBalanceUiModel

@Composable
fun MemberBalanceItem(memberBalance: MemberBalanceUiModel, modifier: Modifier = Modifier) {
    val balanceColor = if (memberBalance.isPositiveBalance) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = balanceColor
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (memberBalance.isCurrentUser) {
                        stringResource(R.string.balances_member_you)
                    } else {
                        memberBalance.displayName
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                // Breakdown row: contributed / withdrawn / owes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BreakdownLabel(
                        label = stringResource(R.string.balances_member_contributed_label),
                        value = memberBalance.formattedContributed
                    )
                    BreakdownLabel(
                        label = stringResource(R.string.balances_member_withdrawn_label),
                        value = memberBalance.formattedWithdrawn
                    )
                    BreakdownLabel(
                        label = stringResource(R.string.balances_member_owes_label),
                        value = memberBalance.formattedOwes
                    )
                }
            }

            // Net balance on the right
            Text(
                text = memberBalance.formattedNetBalance,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
        }
    }
}

@Composable
private fun BreakdownLabel(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

