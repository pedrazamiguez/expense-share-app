package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.ui.settings.R
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.SettingsRow
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.component.sheet.CopyableTextSheet
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.view.SettingItemView
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.AppVersionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppVersionFeature(
    appVersionViewModel: AppVersionViewModel = koinViewModel()
) {
    val appVersion = appVersionViewModel.appVersion.collectAsState()
    val showAppVersionSheet by appVersionViewModel.showSheet.collectAsState()

    SettingsRow(
        SettingItemView(
            icon = Icons.Outlined.Commit,
            title = stringResource(R.string.app_version_title),
            description = stringResource(R.string.app_version_description),
            onClick = {
                appVersionViewModel.loadAppVersion()
                appVersionViewModel.showSheet()
            })
    )

    if (showAppVersionSheet) {
        CopyableTextSheet(
            title = stringResource(R.string.app_version_title),
            copyableText = "v${appVersion.value}",
            notAvailableText = stringResource(R.string.app_version_not_available),
            onDismiss = { appVersionViewModel.hideSheet() })
    }

}
