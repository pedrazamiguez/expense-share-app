package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetSupportedCurrenciesUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.CreateGroupUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateGroupViewModel(
    private val createGroupUseCase: CreateGroupUseCase,
    private val getSupportedCurrenciesUseCase: GetSupportedCurrenciesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<CreateGroupUiAction>()
    val actions: SharedFlow<CreateGroupUiAction> = _actions.asSharedFlow()

    fun onEvent(
        event: CreateGroupUiEvent, onCreateGroupSuccess: () -> Unit
    ) {
        Timber.i("Event: $event")

        when (event) {
            is CreateGroupUiEvent.LoadCurrencies -> loadCurrencies()

            is CreateGroupUiEvent.CurrencySelected -> {
                _uiState.update { state ->
                    // Remove the selected currency from extra currencies if it was there
                    val updatedExtraCurrencies = state.extraCurrencies
                        .filter { it.code != event.currency.code }
                        .toImmutableList()
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
                    }.toImmutableList()
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
        // Optimization: Don't reload if we already have data (e.g., on screen rotation)
        if (_uiState.value.availableCurrencies.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCurrencies = true) }

            getSupportedCurrenciesUseCase()
                .onSuccess { sortedCurrencies ->
                    val defaultCurrency = sortedCurrencies.find { it.code == "EUR" }
                        ?: sortedCurrencies.firstOrNull()

                    _uiState.update {
                        it.copy(
                            availableCurrencies = sortedCurrencies.toImmutableList(),
                            selectedCurrency = it.selectedCurrency ?: defaultCurrency,
                            isLoadingCurrencies = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingCurrencies = false,
                            errorRes = R.string.group_error_load_currencies
                        )
                    }
                    Timber.e(e, "Failed to load currencies")
                }
        }
    }


    private fun createGroup(onCreateGroupSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorRes = null, errorMessage = null)
            }

            val state = _uiState.value
            val groupName = state.groupName

            val result = createGroupUseCase(
                Group(
                    name = groupName,
                    description = state.groupDescription,
                    currency = state.selectedCurrency?.code ?: "EUR",
                    extraCurrencies = state.extraCurrencies.map { it.code }
                )
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _actions.emit(
                    CreateGroupUiAction.ShowSuccess(
                        message = UiText.StringResource(R.string.group_created_success, groupName)
                    )
                )
                onCreateGroupSuccess()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(errorMessage = e.message, isLoading = false)
                }
                _actions.emit(
                    CreateGroupUiAction.ShowError(
                        message = UiText.StringResource(R.string.group_error_creation_failed)
                    )
                )
            }
        }
    }

}
