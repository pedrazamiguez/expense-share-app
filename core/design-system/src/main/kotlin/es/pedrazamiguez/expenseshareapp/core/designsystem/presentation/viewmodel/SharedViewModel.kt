package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    private val _selectedGroupName = MutableStateFlow<String?>(null)
    val selectedGroupName: StateFlow<String?> = _selectedGroupName.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.selectedGroupId.collect { groupId ->
                _selectedGroupId.value = groupId
                // Clear group name when loading from preferences (will be set when group is selected)
                if (groupId == null) {
                    _selectedGroupName.value = null
                }
            }
        }
    }

    fun selectGroup(groupId: String?, groupName: String? = null) {
        _selectedGroupId.value = groupId
        _selectedGroupName.value = groupName
        viewModelScope.launch {
            userPreferences.setSelectedGroupId(groupId)
        }
    }

}
