package es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InstallationIdViewModel(
    private val cloudMetadataService: CloudMetadataService
) : ViewModel() {

    private val _installationId = MutableStateFlow<String?>(null)
    val installationId: StateFlow<String?> = _installationId

    private val _showSheet = MutableStateFlow(false)
    val showSheet: StateFlow<Boolean> = _showSheet

    fun fetchInstallationId() {
        viewModelScope.launch {
            val result = cloudMetadataService.getAppInstallationId()
            _installationId.value = result.getOrNull()
        }
    }

    fun showSheet() {
        _showSheet.value = true
    }

    fun hideSheet() {
        _showSheet.value = false
    }

}
