package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CurrencyBreakdownUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.MemberBalanceUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MemberBalanceItem(memberBalance: MemberBalanceUiModel, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    val balanceColor = if (memberBalance.isPositiveBalance) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    val displayName = if (memberBalance.isCurrentUser) {
        stringResource(R.string.balances_member_you)
    } else {
        memberBalance.displayName
    }

    val expandedStateDesc = if (isExpanded) {
        stringResource(R.string.balances_member_expanded)
    } else {
        stringResource(R.string.balances_member_collapsed)
    }

    val toggleContentDesc = if (isExpanded) {
        stringResource(R.string.balances_member_collapse, displayName)
    } else {
        stringResource(R.string.balances_member_expand, displayName)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                stateDescription = expandedStateDesc
                contentDescription = toggleContentDesc
            }
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ── Collapsed summary row ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    // Breakdown row: contributed / cash in hand / spent
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BreakdownLabel(
                            label = stringResource(R.string.balances_member_contributed_label),
                            value = memberBalance.formattedContributed
                        )
                        BreakdownLabel(
                            label = stringResource(R.string.balances_member_cash_in_hand_label),
                            value = memberBalance.formattedCashInHand
                        )
                        BreakdownLabel(
                            label = stringResource(R.string.balances_member_spent_label),
                            value = memberBalance.formattedTotalSpent
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Pocket balance on the right
                    Text(
                        text = memberBalance.formattedPocketBalance,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = toggleContentDesc,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Expanded detail section ─────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Pocket balance row
                    DetailRow(
                        label = stringResource(R.string.balances_member_pocket_balance),
                        value = memberBalance.formattedPocketBalance,
                        valueColor = balanceColor,
                        icon = "🏦"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cash in hand section
                    DetailRow(
                        label = stringResource(R.string.balances_member_cash_in_hand),
                        value = memberBalance.formattedCashInHand,
                        icon = "💵"
                    )

                    if (memberBalance.cashInHandByCurrency.isNotEmpty()) {
                        CurrencyBreakdownRows(items = memberBalance.cashInHandByCurrency)
                    } else {
                        EmptyHintText(text = stringResource(R.string.balances_member_no_cash))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Spending breakdown section
                    Text(
                        text = "📊 ${stringResource(R.string.balances_member_spent_breakdown)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (memberBalance.cashSpentByCurrency.isEmpty() &&
                        memberBalance.nonCashSpentByCurrency.isEmpty()
                    ) {
                        EmptyHintText(text = stringResource(R.string.balances_member_no_expenses))
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Cash expenses
                        if (memberBalance.cashSpentByCurrency.isNotEmpty()) {
                            DetailRow(
                                label = stringResource(R.string.balances_member_cash_expenses),
                                value = memberBalance.formattedCashSpent
                            )
                            CurrencyBreakdownRows(items = memberBalance.cashSpentByCurrency)
                        }

                        // Non-cash expenses
                        if (memberBalance.nonCashSpentByCurrency.isNotEmpty()) {
                            DetailRow(
                                label = stringResource(R.string.balances_member_non_cash_expenses),
                                value = memberBalance.formattedNonCashSpent
                            )
                            CurrencyBreakdownRows(items = memberBalance.nonCashSpentByCurrency)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    icon: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (icon != null) "$icon $label" else label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun CurrencyBreakdownRows(items: ImmutableList<CurrencyBreakdownUiModel>) {
    items.forEach { item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.currency,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.formattedAmount,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                if (item.formattedEquivalent.isNotBlank()) {
                    Text(
                        text = item.formattedEquivalent,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHintText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )
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
