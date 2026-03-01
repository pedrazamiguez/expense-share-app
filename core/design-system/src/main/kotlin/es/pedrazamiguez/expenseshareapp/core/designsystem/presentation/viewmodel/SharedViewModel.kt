package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupNameUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetSelectedGroupUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SharedViewModel(
    private val getSelectedGroupIdUseCase: GetSelectedGroupIdUseCase,
    private val getSelectedGroupNameUseCase: GetSelectedGroupNameUseCase,
    private val setSelectedGroupUseCase: SetSelectedGroupUseCase,
) : ViewModel() {

    val selectedGroupId: StateFlow<String?> = getSelectedGroupIdUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
        initialValue = null,
    )

    val selectedGroupName: StateFlow<String?> = getSelectedGroupNameUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
        initialValue = null,
    )

    fun selectGroup(groupId: String?, groupName: String?) {
        viewModelScope.launch {
            setSelectedGroupUseCase(groupId, groupName)
        }
    }

}
