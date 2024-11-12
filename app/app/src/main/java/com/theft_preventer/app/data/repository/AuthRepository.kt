package com.theft_preventer.app.data.repository

import com.theft_preventer.app.data.local.SettingsDataStore
import com.theft_preventer.app.data.remote.ApiService
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val settingsDataStore: SettingsDataStore
) {
    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = apiService.login(username, password)
            if (response.token != null) {
                settingsDataStore.saveToken(response.token)
                Result.success(response.token)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getToken(): String? {
        return settingsDataStore.getToken()
    }

    suspend fun logout() {
        settingsDataStore.clearToken()
    }
}