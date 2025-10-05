package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import es.pedrazamiguez.expenseshareapp.core.ui.extension.hardcoded
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
            title = "Installation ID".hardcoded,
            description = "Unique identifier".hardcoded,
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
