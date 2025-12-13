package es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.getNameRes
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency

@Composable
fun DefaultCurrencyScreen(
    availableCurrencies: List<Currency>,
    selectedCurrencyCode: String?,
    onCurrencySelected: (String) -> Unit
) {

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        items(availableCurrencies) { currency ->

            val isSelected = currency.name == selectedCurrencyCode
            val currencyName = stringResource(id = currency.getNameRes())

            ListItem(
                headlineContent = { Text(text = "$currencyName (${currency.symbol})") },
                supportingContent = { Text(text = currency.name) },
                trailingContent = {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected"
                        )
                    }
                },
                modifier = Modifier.clickable {
                    onCurrencySelected(currency.name)
                })

        }

    }

}
