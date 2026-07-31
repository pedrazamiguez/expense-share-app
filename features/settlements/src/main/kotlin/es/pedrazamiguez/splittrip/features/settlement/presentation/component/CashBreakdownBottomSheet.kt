package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        contentWindowInsets = { WindowInsets.safeDrawing },
        modifier = modifier
    ) {
        CashBreakdownSheetContent(
            breakdown = breakdown,
            formattedTotal = formattedTotal,
            formattedTotalFees = formattedTotalFees
        )
    }
}
