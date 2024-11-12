package com.theft_preventer.app.data.presentation.login

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theft_preventer.app.data.local.SettingsDataStore
import com.theft_preventer.app.data.repository.AuthRepository
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val token = authRepository.getToken()
            val esp32Ip = settingsDataStore.getEsp32Ip()
            val backendIp = settingsDataStore.getBackendIp()
            if (token != null) {
                _state.value = _state.value.copy(isLoggedIn = true)
            }
            if (esp32Ip != null) {
                _state.value = _state.value.copy(esp32Ip = esp32Ip)
            }
            if (backendIp != null) {
                _state.value = _state.value.copy(backendIp = backendIp)
            }
        }
    }

    fun onUsernameChanged(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun onEsp32IpChanged(ip: String) {
        _state.value = _state.value.copy(esp32Ip = ip)
    }

    fun onBackendIpChanged(ip: String) {
        _state.value = _state.value.copy(backendIp = ip)
    }


    fun login() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = authRepository.login(_state.value.username, _state.value.password)
            _state.value = _state.value.copy(
                isLoading = false,
                isLoggedIn = result.isSuccess,
                error = result.exceptionOrNull()?.message
            )
        }
    }
    fun saveSettings() {
        viewModelScope.launch {
            settingsDataStore.saveEsp32Ip(_state.value.esp32Ip)
            settingsDataStore.saveBackendIp(_state.value.backendIp)
            Toast.makeText(
                getApplication(context),
                "Settings saved",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val esp32Ip: String = "",
    val backendIp: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)