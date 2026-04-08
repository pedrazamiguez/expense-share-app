package es.pedrazamiguez.splittrip.core.designsystem.preview.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun GradientButtonEnabledPreview() {
    PreviewThemeWrapper {
        GradientButton(
            text = "Next: Currency",
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@PreviewThemes
@Composable
private fun GradientButtonDisabledPreview() {
    PreviewThemeWrapper {
        GradientButton(
            text = "Next: Currency",
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@PreviewThemes
@Composable
private fun GradientButtonLoadingPreview() {
    PreviewThemeWrapper {
        GradientButton(
            text = "Next: Currency",
            onClick = {},
            isLoading = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}
