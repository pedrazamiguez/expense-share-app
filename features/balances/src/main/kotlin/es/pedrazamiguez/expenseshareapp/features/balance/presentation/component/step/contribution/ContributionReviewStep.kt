package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.contribution

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState

/**
 * Step 3: Read-only summary of all entered data (final confirmation).
 * Always shown as the last wizard step.
 */
@Composable
fun ContributionReviewStep(
    uiState: AddContributionUiState,
    modifier: Modifier = Modifier
) {
    val none = stringResource(R.string.contribution_review_none)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.contribution_review_title),
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
                ReviewRow(
                    label = stringResource(R.string.contribution_review_amount),
                    value = uiState.formattedAmountWithCurrency.ifBlank {
                        uiState.amountInput.ifBlank { none }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ReviewRow(
                    label = stringResource(R.string.contribution_review_scope),
                    value = when (uiState.contributionScope) {
                        PayerType.GROUP ->
                            stringResource(R.string.contribution_review_scope_group)
                        PayerType.USER ->
                            stringResource(R.string.contribution_review_scope_personal)
                        PayerType.SUBUNIT ->
                            uiState.subunitOptions
                                .find { it.id == uiState.selectedSubunitId }?.name ?: none
                    }
                )
            }
        }
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
