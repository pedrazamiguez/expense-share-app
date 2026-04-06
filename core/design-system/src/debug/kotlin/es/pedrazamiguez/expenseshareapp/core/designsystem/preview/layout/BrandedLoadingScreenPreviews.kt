package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.BrandedLoadingScreen
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun BrandedLoadingScreenPreview() {
    PreviewThemeWrapper {
        BrandedLoadingScreen(
            painter = rememberVectorPainter(Icons.Outlined.Payments)
        )
    }
}
