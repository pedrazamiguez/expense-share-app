package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.layout

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ErrorView
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun ErrorViewPreview() {
    PreviewThemeWrapper {
        ErrorView()
    }
}
