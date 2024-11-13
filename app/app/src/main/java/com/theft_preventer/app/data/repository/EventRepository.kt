package com.theft_preventer.app.data.repository

import com.theft_preventer.app.data.local.SettingsDataStore
import com.theft_preventer.app.data.remote.ApiService
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore
) {

    suspend fun getEvents(page: Int, perPage: Int): Result<List<ApiService.Event>> {
        return try {
            val events = fetchEvents(page, perPage)
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private suspend fun fetchEvents(page: Int, perPage: Int): List<ApiService.Event> {
        try {
            val token = authRepository.getToken() ?: throw Exception("No token available")
            return apiService.getEvents(token, page, perPage)
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Unauthorized) {
                // Token expired, try to re-login
                val username = settingsDataStore.getUsername() ?: throw Exception("No username stored")
                val password = settingsDataStore.getPassword() ?: throw Exception("No password stored")

                val loginResult = authRepository.login(username, password)
                if (loginResult.isSuccess) {
                    // Re-attempt to fetch events with the new token
                    val newToken = authRepository.getToken() ?: throw Exception("No token available after re-login")
                    return apiService.getEvents(newToken, page, perPage)
                } else {
                    throw Exception("Re-login failed: ${loginResult.exceptionOrNull()?.message}")
                }
            } else {
                throw e
            }
        }
    }
}
