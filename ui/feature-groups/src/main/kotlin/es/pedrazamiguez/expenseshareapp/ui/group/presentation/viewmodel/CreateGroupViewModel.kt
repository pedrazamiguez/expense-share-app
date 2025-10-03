package es.pedrazamiguez.expenseshareapp.ui.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.model.CreateGroupUiAction
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.model.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.model.CreateGroupUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<CreateGroupUiAction>()
    val actions: SharedFlow<CreateGroupUiAction> = _actions.asSharedFlow()

    fun onEvent(
        event: CreateGroupUiEvent,
        onCreateGroupSuccess: () -> Unit
    ) {
        when (event) {
            is CreateGroupUiEvent.CurrencyChanged -> _uiState.value = _uiState.value.copy(groupCurrency = event.currency)
            is CreateGroupUiEvent.DescriptionChanged -> _uiState.value = _uiState.value.copy(groupDescription = event.description)
            is CreateGroupUiEvent.NameChanged -> _uiState.value = _uiState.value.copy(groupName = event.name)
            CreateGroupUiEvent.SubmitCreateGroup -> createGroup(onCreateGroupSuccess)
        }
    }

    private fun createGroup(onCreateGroupSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            // TODO: Call use case to create group
        }
    }

}