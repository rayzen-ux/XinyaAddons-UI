package com.rianixia.settings.overlay.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class SystemTunerState(
    val isFlagSecureDisabled: Boolean = false,
    val isRotationButtonForced: Boolean = false
)

class SystemViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SystemTunerState())
    val uiState = _uiState.asStateFlow()

    // Property Keys
    private val PROP_DISABLE_SECURE = "persist.sys.rianixia.systemui-tuner.disable_flag_secure"
    private val PROP_ROTATION_BTN = "persist.sys.rianixia.systemui-tuner.rotation_button"

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch(Dispatchers.IO) {
            val secure = getSystemProperty(PROP_DISABLE_SECURE) == "1"
            val rotation = getSystemProperty(PROP_ROTATION_BTN) == "1"
            
            _uiState.update { 
                it.copy(
                    isFlagSecureDisabled = secure, 
                    isRotationButtonForced = rotation
                ) 
            }
        }
    }

    fun toggleFlagSecure(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            setSystemProperty(PROP_DISABLE_SECURE, if (enabled) "1" else "0")
            _uiState.update { it.copy(isFlagSecureDisabled = enabled) }
        }
    }

    fun toggleRotationButton(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            setSystemProperty(PROP_ROTATION_BTN, if (enabled) "1" else "0")
            _uiState.update { it.copy(isRotationButtonForced = enabled) }
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine()?.trim() ?: ""
        } catch (e: Exception) { "" }
    }

    private fun setSystemProperty(key: String, value: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("setprop", key, value)).waitFor()
        } catch (e: Exception) { e.printStackTrace() }
    }
}