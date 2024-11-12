package com.theft_preventer.app.data.remote

import com.theft_preventer.app.data.local.SettingsDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService @Inject constructor(
    private val client: HttpClient,
    private val settingsDataStore: SettingsDataStore
) {
    @Serializable
    data class LoginResponse(val token: String? = null, val error: String? = null, val message: String? = null)

    @Serializable
    data class Event(
        val id: Int,
        val state: String,
        val lux: Int,
        val temp: Double,
        val phone: String,
        val timestamp: Long
    )

    suspend fun login(username: String, password: String): LoginResponse {
        return client.post("${settingsDataStore.getBackendIp()}/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.body()
    }

    suspend fun getEvents(token: String, page: Int, perPage: Int): List<Event> {
        return client.get("${settingsDataStore.getBackendIp()}/api/events") {
            parameter("page", page)
            parameter("per_page", perPage)
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}

@Serializable
data class LoginRequest(val username: String, val password: String)