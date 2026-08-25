package es.pedrazamiguez.splittrip.core.designsystem.preview.security

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.security.AppLockGate
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun AppLockGatePreview() {
    PreviewThemeWrapper {
        AppLockGate(
            onUnlockClick = {}
        )
    }
}
