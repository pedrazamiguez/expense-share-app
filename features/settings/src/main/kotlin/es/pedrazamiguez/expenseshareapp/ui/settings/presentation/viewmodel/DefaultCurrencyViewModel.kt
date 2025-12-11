package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.config.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DefaultCurrencyViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val availableCurrencies = Currency.entries

    val selectedCurrencyCode: StateFlow<String?> = userPreferences.defaultCurrency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
        initialValue = null
    )

    fun onCurrencySelected(currencyCode: String) {
        viewModelScope.launch {
            userPreferences.setDefaultCurrency(currencyCode)
        }
    }

}
