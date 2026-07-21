package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

@Composable
internal fun PersonalPositionActivityBreakdown(
    personalPosition: PersonalPositionUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(MaterialTheme.spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        PositionBreakdownRow(
            label = stringResource(R.string.my_position_label_total_contributed),
            formattedValue = personalPosition.formattedTotalContributed
        )
        PositionBreakdownRow(
            label = stringResource(R.string.my_position_label_total_spent),
            formattedValue = personalPosition.formattedTotalSpent
        )
        PositionBreakdownSubRow(
            label = stringResource(R.string.my_position_label_cash_spent),
            formattedValue = personalPosition.formattedCashSpent
        )
        PositionBreakdownSubRow(
            label = stringResource(R.string.my_position_label_non_cash_spent),
            formattedValue = personalPosition.formattedNonCashSpent
        )

        personalPosition.formattedRefundableSpent?.let { refundable ->
            PositionBreakdownRow(
                label = stringResource(R.string.my_position_label_refundable_spent),
                formattedValue = refundable
            )
        }
    }
}
