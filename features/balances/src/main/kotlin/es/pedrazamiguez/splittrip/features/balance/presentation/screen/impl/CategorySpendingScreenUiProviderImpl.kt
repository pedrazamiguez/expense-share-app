package es.pedrazamiguez.splittrip.features.balance.presentation.screen.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.MainAction
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.splittrip.features.balance.R

class CategorySpendingScreenUiProviderImpl(
    override val route: String = Routes.CATEGORY_SPENDING
) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val tabNavController = LocalTabNavController.current

        DynamicTopAppBar(
            title = stringResource(R.string.balances_category_spending_title),
            onBack = { tabNavController.navigateUp() }
        )
    }

    override val mainAction: MainAction?
        @Composable
        get() = null
}
