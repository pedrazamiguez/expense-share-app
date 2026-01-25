package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.ListUserGroupsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListUserGroupsViewModel(
    private val getUserGroupsFlowUseCase: GetUserGroupsFlowUseCase,
    private val groupUiMapper: GroupUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUserGroupsUiState())
    val uiState: StateFlow<ListUserGroupsUiState> = _uiState.asStateFlow()

    init {
        fetchGroupsFlow()
    }

    fun saveScrollPosition(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        _uiState.update {
            it.copy(
                scrollPosition = firstVisibleItemIndex,
                scrollOffset = firstVisibleItemScrollOffset
            )
        }
    }

    private fun fetchGroupsFlow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getUserGroupsFlowUseCase.invoke()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Unknown error"
                        )
                    }
                }
                .collect { groups ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            groups = groups.map { group -> groupUiMapper.map(group) }
                        )
                    }
                }
        }
    }

}
