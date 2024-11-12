package com.theft_preventer.app.data.repository

import com.theft_preventer.app.data.remote.ApiService
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) {
    suspend fun getEvents(page: Int, perPage: Int): Result<List<ApiService.Event>> {
        return try {
            val token = authRepository.getToken() ?: throw Exception("No token available")
            val events = apiService.getEvents(token, page, perPage)
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}