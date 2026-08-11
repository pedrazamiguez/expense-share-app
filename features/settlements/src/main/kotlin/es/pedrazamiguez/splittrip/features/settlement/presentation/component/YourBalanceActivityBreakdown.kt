package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Cash
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Coin
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CreditCard
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Receipt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ReceiptRefund
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Wallet
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

@Composable
internal fun YourBalanceActivityBreakdown(
    personalPosition: PersonalPositionUiModel,
    modifier: Modifier = Modifier
) {
    FlatCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
        ) {
            PositionBreakdownRow(
                label = stringResource(R.string.your_balance_label_total_contributed),
                formattedValue = personalPosition.formattedTotalContributed,
                icon = TablerIcons.Outline.Wallet
            )
            PositionBreakdownRow(
                label = stringResource(R.string.your_balance_label_total_spent),
                formattedValue = personalPosition.formattedTotalSpent,
                icon = TablerIcons.Outline.Receipt
            )
            PositionBreakdownSubRow(
                label = stringResource(R.string.your_balance_label_cash_spent),
                formattedValue = personalPosition.formattedCashSpent,
                icon = TablerIcons.Outline.Cash
            )
            if (personalPosition.cashSpentByCurrency.isNotEmpty()) {
                CurrencyBreakdownRows(items = personalPosition.cashSpentByCurrency)
            }

            PositionBreakdownSubRow(
                label = stringResource(R.string.your_balance_label_non_cash_spent),
                formattedValue = personalPosition.formattedNonCashSpent,
                icon = TablerIcons.Outline.CreditCard
            )
            if (personalPosition.nonCashSpentByCurrency.isNotEmpty()) {
                CurrencyBreakdownRows(items = personalPosition.nonCashSpentByCurrency)
            }

            personalPosition.formattedRefundableSpent?.let { refundable ->
                PositionBreakdownRow(
                    label = stringResource(R.string.your_balance_label_refundable_spent),
                    formattedValue = refundable,
                    icon = TablerIcons.Outline.ReceiptRefund
                )
                if (personalPosition.refundableSpentByCurrency.isNotEmpty()) {
                    CurrencyBreakdownRows(items = personalPosition.refundableSpentByCurrency)
                }
            }

            personalPosition.formattedTotalFees?.let { fees ->
                PositionBreakdownRow(
                    label = stringResource(R.string.your_balance_label_atm_fees),
                    formattedValue = fees,
                    icon = TablerIcons.Outline.Coin
                )
            }
        }
    }
}
