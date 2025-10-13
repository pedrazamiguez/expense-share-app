package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppVersionViewModel(application: Application) : AndroidViewModel(application) {

    private val _appVersion = MutableStateFlow("Unknown")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _showSheet = MutableStateFlow(false)
    val showSheet: StateFlow<Boolean> = _showSheet

    fun loadAppVersion() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                )

                _appVersion.value = packageInfo.versionName ?: "Unknown"

            } catch (e: Exception) {
                _appVersion.value = "Unknown"
            }
        }
    }

    fun showSheet() {
        _showSheet.value = true
    }

    fun hideSheet() {
        _showSheet.value = false
    }

}
