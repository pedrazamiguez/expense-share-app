package es.pedrazamiguez.expenseshareapp.features.settings.presentation.feature

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.features.settings.R
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.SettingsRow
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.sheet.CopyableTextSheet
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.view.SettingItemView
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.InstallationIdViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun InstallationIdFeature(
    installationIdViewModel: InstallationIdViewModel = koinViewModel()
) {

    val installationId by installationIdViewModel.installationId.collectAsState()
    val showInstallationIdSheet by installationIdViewModel.showSheet.collectAsState()

    SettingsRow(
        SettingItemView(
            icon = Icons.Outlined.QrCode2,
            title = stringResource(R.string.installation_id_title),
            description = stringResource(R.string.installation_id_description),
            onClick = {
                installationIdViewModel.fetchInstallationId()
                installationIdViewModel.showSheet()
            })
    )

    if (showInstallationIdSheet) {
        CopyableTextSheet(
            title = stringResource(R.string.installation_id_title),
            copyableText = installationId,
            notAvailableText = stringResource(R.string.installation_id_not_available),
            onDismiss = { installationIdViewModel.hideSheet() })
    }

}
