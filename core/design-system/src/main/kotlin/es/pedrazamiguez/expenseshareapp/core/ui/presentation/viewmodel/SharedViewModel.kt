package es.pedrazamiguez.expenseshareapp.core.ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.selectedGroupId.collect { groupId ->
                _selectedGroupId.value = groupId
            }
        }
    }

    fun selectGroup(groupId: String?) {
        _selectedGroupId.value = groupId
        viewModelScope.launch {
            userPreferences.setSelectedGroupId(groupId)
        }
    }

}
