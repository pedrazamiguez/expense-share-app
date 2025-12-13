package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ListUserGroupsViewModel(
    private val getUserGroupsFlowUseCase: GetUserGroupsFlowUseCase
) : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchGroupsFlow()
    }

    private fun fetchGroupsFlow() {
        viewModelScope.launch {
            _loading.value = true
            getUserGroupsFlowUseCase
                .invoke()
                .catch { e ->
                    _error.value = e.localizedMessage ?: "Unknown error"
                    _loading.value = false
                }
                .collect { groups ->
                    _groups.value = groups
                    _loading.value = false
                }
        }
    }

}
