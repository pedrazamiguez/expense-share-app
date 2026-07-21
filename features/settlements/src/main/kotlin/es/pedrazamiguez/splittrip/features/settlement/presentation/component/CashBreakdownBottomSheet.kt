package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.AmountText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.BodyText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.SecondaryBodyText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.SheetTitleText
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.CashBreakdownUiModel
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CashBreakdownBottomSheet(
    breakdown: ImmutableList<CashBreakdownUiModel>,
    formattedTotal: String,
    formattedTotalFees: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        ),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        CashBreakdownSheetContent(
            breakdown = breakdown,
            formattedTotal = formattedTotal,
            formattedTotalFees = formattedTotalFees
        )
    }
}

@Composable
private fun CashBreakdownSheetContent(
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

@Composable
private fun CashBreakdownListSection(
    breakdown: ImmutableList<CashBreakdownUiModel>,
    formattedTotal: String,
    formattedTotalFees: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        breakdown.forEach { item ->
            CashBreakdownEntryRow(item = item)
        }
    }

    Spacer(Modifier.height(MaterialTheme.spacing.Large))

    CashBreakdownSummaryFooter(
        formattedTotal = formattedTotal,
        formattedTotalFees = formattedTotalFees
    )
}

@Composable
private fun CashBreakdownSummaryFooter(
    formattedTotal: String,
    formattedTotalFees: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(MaterialTheme.spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryBodyText(
                text = stringResource(R.string.your_position_cash_breakdown_total)
            )
            AmountText(text = formattedTotal)
        }

        formattedTotalFees?.let { fees ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaptionText(text = stringResource(R.string.your_position_cash_breakdown_fees))
                CaptionText(
                    text = fees,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CashBreakdownEntryRow(
    item: CashBreakdownUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(MaterialTheme.spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraSmall)
    ) {
        EntryHeaderRow(label = item.withdrawalLabel, amount = item.formattedNativeRemaining)
        EntrySubRow(date = item.dateText, equivalent = item.formattedEquivalent)
        EntryMetaDataRow(scope = item.scopeLabel, rate = item.formattedRate)
        EntryFeesRow(fees = item.formattedAddOns)
    }
}

@Composable
private fun EntryHeaderRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EntrySubRow(date: String, equivalent: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaptionText(text = date)
        if (equivalent.isNotBlank()) {
            CaptionText(text = equivalent)
        }
    }
}

@Composable
private fun EntryMetaDataRow(scope: String, rate: String) {
    if (scope.isBlank() && rate.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (scope.isNotBlank()) {
            CaptionText(text = scope)
        }
        if (rate.isNotBlank()) {
            CaptionText(text = rate)
        }
    }
}

@Composable
private fun EntryFeesRow(fees: String) {
    if (fees.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaptionText(text = stringResource(R.string.your_position_cash_breakdown_fees))
        CaptionText(
            text = fees,
            color = MaterialTheme.colorScheme.error
        )
    }
}
