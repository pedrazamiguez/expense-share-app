package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.SheetTitleText
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.CashBreakdownUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun CashBreakdownSheetContent(
    breakdown: ImmutableList<CashBreakdownUiModel>,
    formattedTotal: String,
    formattedTotalFees: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = MaterialTheme.spacing.Large,
                end = MaterialTheme.spacing.Large,
                top = MaterialTheme.spacing.ExtraLarge,
                bottom = MaterialTheme.spacing.Section
            )
    ) {
        SheetTitleText(
            text = stringResource(R.string.your_position_cash_breakdown_title),
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.ExtraSmall)
        )
        Text(
            text = stringResource(R.string.your_position_section_personal_position),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.Default)
        )

        if (breakdown.isEmpty()) {
            BodyText(
                text = stringResource(R.string.your_position_cash_breakdown_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            CashBreakdownListSection(
                breakdown = breakdown,
                formattedTotal = formattedTotal,
                formattedTotalFees = formattedTotalFees
            )
        }
    }
}
