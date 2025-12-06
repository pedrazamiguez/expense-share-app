package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.component.FeatureScaffold
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.DefaultCurrencyScreen
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.DefaultCurrencyViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DefaultCurrencyFeature(
    viewModel: DefaultCurrencyViewModel = koinViewModel()
) {

    val selectedCurrency by viewModel.selectedCurrencyCode.collectAsState()

    FeatureScaffold {
        DefaultCurrencyScreen(
            availableCurrencies = viewModel.availableCurrencies,
            selectedCurrencyCode = selectedCurrency,
            onCurrencySelected = viewModel::onCurrencySelected
        )
    }

}
