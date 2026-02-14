package com.signalsnitch.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalsnitch.app.data.NetworkEvent
import com.signalsnitch.app.service.NetworkMonitorService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _events = MutableStateFlow<List<NetworkEvent>>(emptyList())
    val events: StateFlow<List<NetworkEvent>> = _events.asStateFlow()

    init {
        startEventPolling()
    }

    fun setMonitoring(monitoring: Boolean) {
        _isMonitoring.value = monitoring
    }

    private fun startEventPolling() {
        viewModelScope.launch {
            while (true) {
                synchronized(NetworkMonitorService.eventHistory) {
                    _events.value = NetworkMonitorService.eventHistory.toList()
                }
                delay(500) // Poll every 500ms
            }
        }
    }
}
