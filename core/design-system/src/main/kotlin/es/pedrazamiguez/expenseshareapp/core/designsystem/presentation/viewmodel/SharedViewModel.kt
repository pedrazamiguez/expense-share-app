package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SharedViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val selectedGroupId: StateFlow<String?> = userPreferences.selectedGroupId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val selectedGroupName: StateFlow<String?> = userPreferences.selectedGroupName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun selectGroup(groupId: String?, groupName: String?) {
        viewModelScope.launch {
            userPreferences.setSelectedGroup(groupId, groupName)
        }
    }

}
