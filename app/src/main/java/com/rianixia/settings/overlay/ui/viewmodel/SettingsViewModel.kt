package com.rianixia.settings.overlay.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppSettingsState(
    val isLauncherIconEnabled: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppSettingsState())
    val uiState = _uiState.asStateFlow()

    companion object {
        private const val TAG = "SettingsViewModel"
        private const val LAUNCHER_ALIAS = "com.rianixia.settings.overlay.Launcher"
    }

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = getApplication<Application>().packageManager
                val componentName = ComponentName(getApplication(), LAUNCHER_ALIAS)
                val enabledSetting = pm.getComponentEnabledSetting(componentName)
                
                val isIconEnabled = enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

                _uiState.update { 
                    it.copy(isLauncherIconEnabled = isIconEnabled) 
                }
                Log.i(TAG, "loadState: App settings loaded. IconEnabled=$isIconEnabled")
            } catch (e: Exception) {
                Log.e(TAG, "loadState: Error loading settings", e)
            }
        }
    }

    fun toggleLauncherIcon(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = getApplication<Application>().packageManager
                val componentName = ComponentName(getApplication(), LAUNCHER_ALIAS)
                val newState = if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                
                pm.setComponentEnabledSetting(
                    componentName,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
                
                _uiState.update { it.copy(isLauncherIconEnabled = enabled) }
                Log.i(TAG, "toggleLauncherIcon: Success. New state: $enabled")
            } catch (e: Exception) {
                Log.e(TAG, "toggleLauncherIcon: Failed to toggle icon", e)
            }
        }
    }
}