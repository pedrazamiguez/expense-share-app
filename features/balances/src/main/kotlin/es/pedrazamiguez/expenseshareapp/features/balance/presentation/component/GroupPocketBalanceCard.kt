package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.SharedElementKeys
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.AnimatedAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.fabSharedTransitionModifier
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CashBalanceUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import kotlinx.collections.immutable.ImmutableList

private val ACTION_ICON_SIZE = 18.dp

@Composable
fun GroupPocketBalanceCard(
    balance: GroupPocketBalanceUiModel,
    modifier: Modifier = Modifier,
    shouldAnimateBalance: Boolean = false,
    previousBalance: String = "",
    balanceRollingUp: Boolean = true,
    onBalanceAnimationComplete: () -> Unit = {},
    onAddMoney: () -> Unit = {},
    onWithdrawCash: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PocketBalanceMainSection(
                balance = balance,
                shouldAnimateBalance = shouldAnimateBalance,
                previousBalance = previousBalance,
                balanceRollingUp = balanceRollingUp,
                onBalanceAnimationComplete = onBalanceAnimationComplete
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            PocketBalanceStatsRow(balance = balance)

            if (balance.cashBalances.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                CashBalancesSection(
                    cashBalances = balance.cashBalances,
                    formattedTotalCashEquivalent = balance.formattedTotalCashEquivalent
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            BalanceCardActionButtons(
                onAddMoney = onAddMoney,
                onWithdrawCash = onWithdrawCash
            )
        }
    }
}

@Composable
private fun PocketBalanceMainSection(
    balance: GroupPocketBalanceUiModel,
    shouldAnimateBalance: Boolean,
    previousBalance: String,
    balanceRollingUp: Boolean,
    onBalanceAnimationComplete: () -> Unit
) {
    if (balance.groupName.isNotBlank()) {
        Text(
            text = balance.groupName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
    Text(
        text = stringResource(R.string.balances_remaining),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
    Spacer(modifier = Modifier.height(4.dp))
    AnimatedAmount(
        formattedAmount = balance.formattedBalance,
        shouldAnimate = shouldAnimateBalance,
        previousAmount = previousBalance,
        rollingUp = balanceRollingUp,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        onAnimationComplete = onBalanceAnimationComplete
    )
    if (balance.formattedAvailableBalance != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.balances_available),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = balance.formattedAvailableBalance,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun PocketBalanceStatsRow(balance: GroupPocketBalanceUiModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(
                text = stringResource(R.string.balances_total_contributed),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = balance.formattedTotalContributed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.balances_total_spent),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = balance.formattedTotalSpent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
    if (balance.formattedTotalExtras != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.balances_total_extras),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = balance.formattedTotalExtras,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CashBalancesSection(
    cashBalances: ImmutableList<CashBalanceUiModel>,
    formattedTotalCashEquivalent: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.balances_cash_balance_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (formattedTotalCashEquivalent.isNotBlank()) {
            Text(
                text = formattedTotalCashEquivalent,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    cashBalances.forEach { cashBalance -> CashBalanceRow(cashBalance = cashBalance) }
}

@Composable
private fun CashBalanceRow(cashBalance: CashBalanceUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = cashBalance.currency,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = cashBalance.formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (cashBalance.formattedEquivalent.isNotBlank()) {
                Text(
                    text = cashBalance.formattedEquivalent,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun BalanceCardActionButtons(
    onAddMoney: () -> Unit,
    onWithdrawCash: () -> Unit
) {
    val addMoneySharedModifier = fabSharedTransitionModifier(SharedElementKeys.ADD_CONTRIBUTION)
    val withdrawSharedModifier = fabSharedTransitionModifier(SharedElementKeys.ADD_CASH_WITHDRAWAL)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAddMoney,
            modifier = Modifier.weight(1f).then(addMoneySharedModifier),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(ACTION_ICON_SIZE)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = stringResource(R.string.balances_add_money))
        }
        Button(
            onClick = onWithdrawCash,
            modifier = Modifier.weight(1f).then(withdrawSharedModifier),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalAtm,
                contentDescription = null,
                modifier = Modifier.size(ACTION_ICON_SIZE)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = stringResource(R.string.balances_withdraw_cash))
        }
    }
}
