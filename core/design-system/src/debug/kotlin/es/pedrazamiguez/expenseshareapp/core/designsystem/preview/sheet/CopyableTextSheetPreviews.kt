package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.sheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet.CopyableTextSheet
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun CopyableTextSheetVersionPreview() {
    PreviewThemeWrapper {
        CopyableTextSheet(
            icon = Icons.Outlined.Commit,
            title = "App Version",
            copyableText = "v1.2.3 (456)",
            notAvailableText = "Version not available",
            onDismiss = {}
        )
    }
}

@PreviewComplete
@Composable
private fun CopyableTextSheetInstallationIdPreview() {
    PreviewThemeWrapper {
        CopyableTextSheet(
            icon = Icons.Outlined.QrCode2,
            title = "Installation ID",
            copyableText = "abc123def456-gh78-ij90-klmn1234opqr",
            notAvailableText = "ID not available",
            onDismiss = {}
        )
    }
}

@PreviewComplete
@Composable
private fun CopyableTextSheetNotAvailablePreview() {
    PreviewThemeWrapper {
        CopyableTextSheet(
            icon = Icons.Outlined.QrCode2,
            title = "Installation ID",
            copyableText = null,
            notAvailableText = "ID not available",
            onDismiss = {}
        )
    }
}
