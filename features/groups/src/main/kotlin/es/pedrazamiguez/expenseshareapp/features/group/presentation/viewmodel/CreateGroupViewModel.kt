package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateGroupUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateGroupViewModel(
    private val createGroupUseCase: CreateGroupUseCase,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    companion object {
        // Common currencies to prioritize at the top of the list
        private val COMMON_CURRENCY_CODES = listOf(
            "EUR", "USD", "GBP", "CHF", "JPY", "CAD", "AUD", "CNY", "INR", "MXN",
            "BRL", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK", "NZD", "ZAR", "RUB"
        )
    }

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<CreateGroupUiAction>()
    val actions: SharedFlow<CreateGroupUiAction> = _actions.asSharedFlow()

    fun onEvent(
        event: CreateGroupUiEvent, onCreateGroupSuccess: () -> Unit
    ) {
        when (event) {
            is CreateGroupUiEvent.LoadCurrencies -> loadCurrencies()

            is CreateGroupUiEvent.CurrencySelected -> {
                _uiState.update { state ->
                    // Remove the selected currency from extra currencies if it was there
                    val updatedExtraCurrencies = state.extraCurrencies.filter {
                        it.code != event.currency.code
                    }
                    state.copy(
                        selectedCurrency = event.currency,
                        extraCurrencies = updatedExtraCurrencies
                    )
                }
            }

            is CreateGroupUiEvent.ExtraCurrencyToggled -> {
                _uiState.update { state ->
                    val currentExtras = state.extraCurrencies
                    val updatedExtras = if (currentExtras.any { it.code == event.currency.code }) {
                        currentExtras.filter { it.code != event.currency.code }
                    } else {
                        currentExtras + event.currency
                    }
                    state.copy(extraCurrencies = updatedExtras)
                }
            }

            is CreateGroupUiEvent.DescriptionChanged -> _uiState.update {
                it.copy(groupDescription = event.description)
            }

            is CreateGroupUiEvent.NameChanged -> _uiState.update {
                it.copy(groupName = event.name, isNameValid = event.name.isNotBlank())
            }

            CreateGroupUiEvent.SubmitCreateGroup -> {
                if (_uiState.value.groupName.isBlank()) {
                    _uiState.update {
                        it.copy(isNameValid = false, errorRes = R.string.group_error_name_empty)
                    }
                    return
                }
                createGroup(onCreateGroupSuccess)
            }
        }
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCurrencies = true) }

            val currencies = withContext(Dispatchers.IO) {
                currencyRepository.getCurrencies()
            }

            // Sort currencies: common ones first, then alphabetically
            val sortedCurrencies = sortCurrenciesWithCommonFirst(currencies)
            val defaultCurrency = sortedCurrencies.find { it.code == "EUR" } ?: sortedCurrencies.firstOrNull()

            _uiState.update {
                it.copy(
                    availableCurrencies = sortedCurrencies,
                    selectedCurrency = it.selectedCurrency ?: defaultCurrency,
                    isLoadingCurrencies = false
                )
            }
        }
    }

    private fun sortCurrenciesWithCommonFirst(currencies: List<Currency>): List<Currency> {
        val (common, others) = currencies.partition { it.code in COMMON_CURRENCY_CODES }
        val sortedCommon = common.sortedBy { COMMON_CURRENCY_CODES.indexOf(it.code) }
        val sortedOthers = others.sortedBy { it.code }
        return sortedCommon + sortedOthers
    }

    private fun createGroup(onCreateGroupSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorRes = null, errorMessage = null)
            }

            val state = _uiState.value
            val result = createGroupUseCase(
                Group(
                    name = state.groupName,
                    description = state.groupDescription,
                    currency = state.selectedCurrency?.code ?: "EUR",
                    extraCurrencies = state.extraCurrencies.map { it.code }
                )
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                onCreateGroupSuccess()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
                _actions.emit(
                    CreateGroupUiAction.ShowError(
                        messageRes = R.string.group_error_creation_failed, message = e.message
                    )
                )
            }
        }
    }

}
