package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.CreateGroupUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.CreateGroupUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.CreateGroupUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateGroupViewModel(
    private val createGroupUseCase: CreateGroupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<CreateGroupUiAction>()
    val actions: SharedFlow<CreateGroupUiAction> = _actions.asSharedFlow()

    fun onEvent(
        event: CreateGroupUiEvent,
        onCreateGroupSuccess: () -> Unit
    ) {
        when (event) {
            is CreateGroupUiEvent.CurrencyChanged -> _uiState.value =
                _uiState.value.copy(groupCurrency = event.currency)

            is CreateGroupUiEvent.DescriptionChanged -> _uiState.value =
                _uiState.value.copy(groupDescription = event.description)

            is CreateGroupUiEvent.NameChanged -> _uiState.value = _uiState.value.copy(
                groupName = event.name,
                isNameValid = event.name.isNotBlank()
            )

            CreateGroupUiEvent.SubmitCreateGroup -> {
                if (_uiState.value.groupName.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isNameValid = false,
                        errorRes = R.string.group_error_name_empty
                    )
                    return
                }
                createGroup(onCreateGroupSuccess)
            }
        }
    }

    private fun createGroup(onCreateGroupSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorRes = null,
                errorMessage = null
            )

            runCatching {
                val groupToCreate = Group(
                    name = _uiState.value.groupName,
                    description = _uiState.value.groupDescription,
                    currency = _uiState.value.groupCurrency,
                )
                createGroupUseCase(groupToCreate)
            }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onCreateGroupSuccess()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message,
                        isLoading = false
                    )
                    _actions.emit(
                        CreateGroupUiAction.ShowError(
                            messageRes = R.string.group_error_creation_failed,
                            message = e.message
                        )
                    )
                }

        }
    }

}
