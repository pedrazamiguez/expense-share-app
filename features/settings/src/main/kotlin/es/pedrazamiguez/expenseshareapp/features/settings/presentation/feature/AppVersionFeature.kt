package es.pedrazamiguez.expenseshareapp.features.settings.presentation.feature

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.features.settings.R
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.SettingsRow
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.component.sheet.CopyableTextSheet
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.view.SettingItemView
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.AppVersionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppVersionFeature(
    appVersionViewModel: AppVersionViewModel = koinViewModel()
) {
    val appVersion by appVersionViewModel.appVersion.collectAsStateWithLifecycle()
    val showAppVersionSheet by appVersionViewModel.showSheet.collectAsStateWithLifecycle()

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
            icon = Icons.Outlined.Commit,
            title = stringResource(R.string.app_version_title),
            copyableText = "v${appVersion}",
            notAvailableText = stringResource(R.string.app_version_not_available),
            onDismiss = { appVersionViewModel.hideSheet() })
    }

}
