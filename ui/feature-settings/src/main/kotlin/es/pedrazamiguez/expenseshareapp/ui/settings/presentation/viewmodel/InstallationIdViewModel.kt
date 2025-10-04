package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    fun fetchInstallationId() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = cloudMetadataService.getAppInstallationId()
            _installationId.value = result.getOrElse { "Error fetching ID" }
            _isLoading.value = false
        }
    }

    fun showDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }

}
