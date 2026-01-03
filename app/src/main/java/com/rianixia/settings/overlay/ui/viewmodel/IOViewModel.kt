package com.rianixia.settings.overlay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rianixia.settings.overlay.data.IORepository
import com.rianixia.settings.overlay.data.IoDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IOUiState(
    val isLoading: Boolean = true,
    val devices: List<IoDevice> = emptyList(),
    val unifiedAvailableSchedulers: List<String> = emptyList(),
    val currentEffectiveScheduler: String = "unknown",
    val targetScheduler: String = "",
    val isMixedState: Boolean = false
)

class IOViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(IOUiState())
    val uiState: StateFlow<IOUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val devices = IORepository.getIoDevices()
            val savedTarget = IORepository.getTargetScheduler()

            if (devices.isEmpty()) {
                _uiState.update { 
                    it.copy(isLoading = false, devices = emptyList()) 
                }
                return@launch
            }

            // 1. Aggregate Available Schedulers (Union of all devices)
            val allSchedulers = devices.flatMap { it.availableSchedulers }.toSet().sorted()

            // 2. Determine Effective Current State
            val firstActive = devices.first().currentScheduler
            val isMixed = devices.any { it.currentScheduler != firstActive }
            val effectiveCurrent = if (isMixed) "Mixed" else firstActive

            _uiState.update {
                it.copy(
                    isLoading = false,
                    devices = devices,
                    unifiedAvailableSchedulers = allSchedulers,
                    currentEffectiveScheduler = effectiveCurrent,
                    targetScheduler = savedTarget.ifBlank { effectiveCurrent },
                    isMixedState = isMixed
                )
            }
        }
    }

    fun setScheduler(scheduler: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(targetScheduler = scheduler) }
            IORepository.setTargetScheduler(scheduler)
        }
    }
}