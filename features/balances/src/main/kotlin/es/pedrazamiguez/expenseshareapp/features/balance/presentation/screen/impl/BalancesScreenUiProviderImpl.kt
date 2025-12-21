package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.expenseshareapp.features.balance.R

class BalancesScreenUiProviderImpl(override val route: String = Routes.BALANCES) :
    ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        DynamicTopAppBar(
            title = stringResource(R.string.balances_title),
            subtitle = stringResource(R.string.balances_subtitle),
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.balances_search)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.balances_info)
                    )
                }
            }
        )
    }
}
