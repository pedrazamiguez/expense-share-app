package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import es.pedrazamiguez.expenseshareapp.core.ui.constant.UiConstants
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.component.FeatureScaffold
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.DefaultCurrencyScreen
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.DefaultCurrencyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun DefaultCurrencyFeature(
    viewModel: DefaultCurrencyViewModel = koinViewModel()
) {

    val selectedCurrency by viewModel.selectedCurrencyCode.collectAsState()

    val navController = LocalRootNavController.current
    val scope = rememberCoroutineScope()

    FeatureScaffold(currentRoute = Routes.SETTINGS_DEFAULT_CURRENCY) {
        DefaultCurrencyScreen(
            availableCurrencies = viewModel.availableCurrencies,
            selectedCurrencyCode = selectedCurrency,
            onCurrencySelected = { newCurrencyCode ->
                viewModel.onCurrencySelected(newCurrencyCode)
                scope.launch {
                    delay(UiConstants.NAV_FEEDBACK_DELAY)
                    navController.popBackStack()
                }
            })
    }

}
