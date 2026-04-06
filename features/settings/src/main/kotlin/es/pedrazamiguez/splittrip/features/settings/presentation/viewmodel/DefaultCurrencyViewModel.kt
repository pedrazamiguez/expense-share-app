package es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.core.common.constant.AppConstants
import es.pedrazamiguez.splittrip.domain.enums.Currency
import es.pedrazamiguez.splittrip.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetUserDefaultCurrencyUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DefaultCurrencyViewModel(
    private val getUserDefaultCurrencyUseCase: GetUserDefaultCurrencyUseCase,
    private val setUserDefaultCurrencyUseCase: SetUserDefaultCurrencyUseCase
) : ViewModel() {

    val availableCurrencies = Currency.entries

    val selectedCurrencyCode: StateFlow<String?> = getUserDefaultCurrencyUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
            replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
        ),
        initialValue = null
    )

    fun onCurrencySelected(currencyCode: String) {
        viewModelScope.launch {
            setUserDefaultCurrencyUseCase(currencyCode)
        }
    }
}
