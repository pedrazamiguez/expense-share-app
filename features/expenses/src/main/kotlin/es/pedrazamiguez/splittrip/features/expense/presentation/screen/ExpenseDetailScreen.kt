package es.pedrazamiguez.splittrip.features.expense.presentation.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Receipt
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.ExpenseDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    uiState: ExpenseDetailUiState = ExpenseDetailUiState(),
    modifier: Modifier = Modifier,
    onReceiptTap: (() -> Unit)? = null,
    onConfirmPaymentTap: (() -> Unit)? = null
) {
    val scrollBehavior = rememberConnectedScrollBehavior()

    when {
        uiState.isLoading -> ShimmerLoadingList()
        uiState.hasError || uiState.expense == null -> {
            EmptyStateView(
                title = stringResource(R.string.expense_detail_error_loading),
                icon = TablerIcons.Outline.Receipt
            )
        }
        else -> {
            ExpenseDetailContent(
                expense = uiState.expense,
                modifier = modifier,
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                onReceiptTap = onReceiptTap,
                onConfirmPaymentTap = onConfirmPaymentTap
            )
        }
    }
}
