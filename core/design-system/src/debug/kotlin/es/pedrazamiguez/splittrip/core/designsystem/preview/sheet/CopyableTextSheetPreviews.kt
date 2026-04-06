package es.pedrazamiguez.splittrip.core.designsystem.preview.sheet

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.sheet.CopyableTextSheet
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper

@PreviewComplete
@Composable
private fun CopyableTextSheetVersionPreview() {
    PreviewThemeWrapper {
        CopyableTextSheet(
            icon = Icons.Outlined.Commit,
            title = stringResource(R.string.preview_sheet_app_version),
            copyableText = "v1.2.3 (456)",
            notAvailableText = stringResource(R.string.preview_sheet_not_available),
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
            title = stringResource(R.string.preview_sheet_installation_id),
            copyableText = "abc123def456-gh78-ij90-klmn1234opqr",
            notAvailableText = stringResource(R.string.preview_sheet_not_available),
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
            title = stringResource(R.string.preview_sheet_installation_id),
            copyableText = null,
            notAvailableText = stringResource(R.string.preview_sheet_not_available),
            onDismiss = {}
        )
    }
}
