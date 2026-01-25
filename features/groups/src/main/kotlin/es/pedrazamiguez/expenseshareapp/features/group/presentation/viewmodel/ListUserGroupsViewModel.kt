package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ListUserGroupsViewModel(
    private val getUserGroupsFlowUseCase: GetUserGroupsFlowUseCase,
    private val groupUiMapper: GroupUiMapper
) : ViewModel() {

    private val _groups = MutableStateFlow<List<GroupUiModel>>(emptyList())
    val groups: StateFlow<List<GroupUiModel>> = _groups

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
            getUserGroupsFlowUseCase.invoke().catch { e ->
                    _error.value = e.localizedMessage ?: "Unknown error"
                    _loading.value = false
                }.collect { groups ->
                    _groups.value = groups.map { groupUiMapper.map(it) }
                    _loading.value = false
                }
        }
    }

}
