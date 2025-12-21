package es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.expenseshareapp.features.settings.R

class DefaultCurrencyScreenUiProviderImpl(
    override val route: String = Routes.SETTINGS_DEFAULT_CURRENCY
) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalRootNavController.current
        DynamicTopAppBar(
            title = stringResource(R.string.settings_preferences_currency_title),
            onBack = { navController.popBackStack() }
        )
    }

}
