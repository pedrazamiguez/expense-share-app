package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.ui.settings.R
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.SettingsRow
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.sheet.InstallationIdSheet
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.view.SettingItemView
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.InstallationIdViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun InstallationIdFeature(
    installationIdViewModel: InstallationIdViewModel = koinViewModel()
) {

    val installationId by installationIdViewModel.installationId.collectAsState()
    val showInstallationIdDialog by installationIdViewModel.showDialog.collectAsState()

    SettingsRow(
        SettingItemView(
            icon = Icons.Outlined.QrCode2,
            title = stringResource(R.string.installation_id_title),
            description = stringResource(R.string.installation_id_description),
            onClick = {
                installationIdViewModel.fetchInstallationId()
                installationIdViewModel.showDialog()
            })
    )

    if (showInstallationIdDialog && installationId != null) {
        InstallationIdSheet(
            installationId = installationId!!,
            onDismiss = { installationIdViewModel.hideDialog() })
    }

}
