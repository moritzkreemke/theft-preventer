package com.theft_preventer.app.data.remote

import com.theft_preventer.app.data.local.SettingsDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.NetworkInterface
import javax.inject.Inject

class ESP32Service @Inject constructor(
    private val client: HttpClient,
    private val settingsDataStore: SettingsDataStore
) {

    suspend fun getDeviceIp(): String {
        return try {
            val response: String = client.get("${settingsDataStore.getEsp32Ip()}/receive_data").body()
            response.substringAfter("IP: ").trim()
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun registerDevice(): String {
        return try {
            client.get("${settingsDataStore.getEsp32Ip()}/register_device").body()
        } catch (e: Exception) {
            "Registration failed"
        }
    }
    fun getCurrentDeviceIp(): String {
        return NetworkInterface.getNetworkInterfaces().asSequence().flatMap { networkInterface ->
            networkInterface.inetAddresses.asSequence()
                .filter { !it.isLoopbackAddress && it.hostAddress.indexOf(':') < 0 }
                .map { it.hostAddress }
        }.firstOrNull() ?: ""
    }
}