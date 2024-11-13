package com.theft_preventer.app.data.presentation.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theft_preventer.app.data.remote.ApiService
import com.theft_preventer.app.data.remote.ApiService.Event
import com.theft_preventer.app.data.remote.ESP32Service
import com.theft_preventer.app.data.repository.AuthRepository
import com.theft_preventer.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository,
    private val esp32Service: ESP32Service
) : ViewModel() {

    private val _state = MutableStateFlow(OverviewState())
    val state: StateFlow<OverviewState> = _state.asStateFlow()

    init {
        loadEvents()
        checkDeviceStatus()
    }

    fun checkDeviceStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(deviceStatus = DeviceStatus.LOADING)
            try {
                val deviceIp = esp32Service.getDeviceIp()
                val currentDeviceIp = esp32Service.getCurrentDeviceIp()
                _state.value = when {
                    deviceIp == "Exception" -> _state.value.copy(deviceStatus = DeviceStatus.ESP_NOT_AVAILABLE)
                    deviceIp == currentDeviceIp -> _state.value.copy(deviceStatus = DeviceStatus.REGISTERED)
                    deviceIp.isEmpty() -> _state.value.copy(deviceStatus = DeviceStatus.NOT_REGISTERED)
                    else -> _state.value.copy(deviceStatus = DeviceStatus.NOT_REGISTERED)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(deviceStatus = DeviceStatus.ESP_NOT_AVAILABLE)
            }
        }
    }

    fun registerDevice() {
        viewModelScope.launch {
            _state.value = _state.value.copy(deviceStatus = DeviceStatus.LOADING)
            try {
                val result = esp32Service.registerDevice()
                if (result == "Device registered successfully") {
                    _state.value = _state.value.copy(deviceStatus = DeviceStatus.REGISTERED)
                } else {
                    _state.value = _state.value.copy(deviceStatus = DeviceStatus.NOT_REGISTERED)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(deviceStatus = DeviceStatus.ESP_NOT_AVAILABLE)
            }
        }
    }


fun loadEvents() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = eventRepository.getEvents(page = 1, perPage = 10)
            _state.value = when {
                result.isSuccess -> _state.value.copy(
                    isLoading = false,
                    events = result.getOrNull() ?: emptyList()
                )
                result.isFailure -> _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
                else -> {
                    _state.value
                }
            }
        }
    }

    fun refreshEvents() {
        loadEvents()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
}

data class OverviewState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val deviceStatus: DeviceStatus = DeviceStatus.LOADING
)

enum class DeviceStatus {
    LOADING, REGISTERED, NOT_REGISTERED, ESP_NOT_AVAILABLE
}