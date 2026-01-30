package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.R
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun EmptyStateViewPreview() {
    PreviewThemeWrapper {
        EmptyStateView(
            title = stringResource(R.string.empty_state_no_expenses_title),
            description = stringResource(R.string.empty_state_no_expenses_description)
        )
    }
}
