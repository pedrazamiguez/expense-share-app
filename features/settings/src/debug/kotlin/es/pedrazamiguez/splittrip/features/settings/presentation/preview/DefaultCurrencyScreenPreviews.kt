package es.pedrazamiguez.splittrip.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.domain.enums.Currency
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.DefaultCurrencyScreen

@PreviewComplete
@Composable
private fun DefaultCurrencyScreenPreview() {
    PreviewThemeWrapper {
        DefaultCurrencyScreen(
            availableCurrencies = Currency.entries,
            selectedCurrencyCode = Currency.EUR.name,
            onCurrencySelected = {}
        )
    }
}
