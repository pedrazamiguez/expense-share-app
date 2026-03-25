package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.withdrawal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatAmountWithCurrency
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import java.util.Locale

/**
 * Step 5: Read-only summary of all entered data (final confirmation).
 * Always shown as the last wizard step.
 */
@Composable
fun ReviewStep(
    uiState: AddCashWithdrawalUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.withdrawal_review_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReviewAmountSection(uiState)
                ReviewFeeSection(uiState)
                ReviewDetailsSection(uiState)
            }
        }
    }
}

@Composable
private fun ReviewAmountSection(uiState: AddCashWithdrawalUiState) {
    val none = stringResource(R.string.withdrawal_review_none)
    val locale = rememberLocale()

    ReviewRow(
        label = stringResource(R.string.withdrawal_review_amount),
        value = uiState.selectedCurrency?.code?.let { code ->
            formatAmountWithCurrency(uiState.withdrawalAmount, code, locale)
        }?.ifBlank { none } ?: uiState.withdrawalAmount.ifBlank { none }
    )
    ReviewRow(
        label = stringResource(R.string.withdrawal_review_currency),
        value = uiState.selectedCurrency?.displayText ?: none
    )

    if (uiState.showExchangeRateSection) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ReviewRow(
            label = stringResource(R.string.withdrawal_review_exchange_rate),
            value = uiState.displayExchangeRate
        )
        ReviewRow(
            label = stringResource(R.string.withdrawal_review_deducted),
            value = uiState.groupCurrency?.code?.let { code ->
                formatAmountWithCurrency(uiState.deductedAmount, code, locale)
            }?.ifBlank { none } ?: uiState.deductedAmount.ifBlank { none }
        )
    }
}

@Composable
private fun ReviewFeeSection(uiState: AddCashWithdrawalUiState) {
    if (!uiState.hasFee || uiState.feeAmount.isBlank()) return

    val locale = rememberLocale()

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    ReviewRow(
        label = stringResource(R.string.withdrawal_review_atm_fee),
        value = (uiState.feeCurrency ?: uiState.groupCurrency)?.code?.let { code ->
            formatAmountWithCurrency(uiState.feeAmount, code, locale)
        } ?: uiState.feeAmount
    )
    if (uiState.showFeeExchangeRateSection && uiState.feeConvertedAmount.isNotBlank()) {
        ReviewRow(
            label = stringResource(R.string.withdrawal_review_fee_converted),
            value = uiState.groupCurrency?.code?.let { code ->
                formatAmountWithCurrency(uiState.feeConvertedAmount, code, locale)
            } ?: uiState.feeConvertedAmount
        )
    }
}

@Composable
private fun ReviewDetailsSection(uiState: AddCashWithdrawalUiState) {
    val none = stringResource(R.string.withdrawal_review_none)

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    ReviewRow(
        label = stringResource(R.string.withdrawal_review_scope),
        value = when (uiState.withdrawalScope) {
            PayerType.GROUP -> stringResource(R.string.balances_withdraw_cash_scope_group)
            PayerType.USER -> stringResource(R.string.balances_withdraw_cash_scope_personal)
            PayerType.SUBUNIT ->
                uiState.subunitOptions
                    .find { it.id == uiState.selectedSubunitId }?.name ?: none
        }
    )
    if (uiState.title.isNotBlank()) {
        ReviewRow(
            label = stringResource(R.string.withdrawal_review_withdrawal_title),
            value = uiState.title
        )
    }
    if (uiState.notes.isNotBlank()) {
        ReviewRow(
            label = stringResource(R.string.withdrawal_review_notes),
            value = uiState.notes
        )
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Returns the current configuration locale, recomposing when the configuration changes
 * (e.g. the user switches language in system settings).
 */
@Composable
private fun rememberLocale(): Locale {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        configuration.locales[0] ?: Locale.getDefault()
    }
}
