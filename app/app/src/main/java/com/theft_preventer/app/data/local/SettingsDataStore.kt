package com.theft_preventer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsDataStore @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val ESP_32_IP_KEY = stringPreferencesKey("esp32_ip")
        private val BACKEND_IP_KEY = stringPreferencesKey("backend_ip")
        private val USERNAME_TOKEN = stringPreferencesKey("username")
        private val PASSWORD_TOKEN = stringPreferencesKey("password")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }.firstOrNull()
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    suspend fun saveEsp32Ip(ip: String) {
        dataStore.edit { preferences ->
            preferences[ESP_32_IP_KEY] = ip
        }
    }
    suspend fun getEsp32Ip(): String? {
        return dataStore.data.map { preferences ->
            preferences[ESP_32_IP_KEY]
        }.firstOrNull()
    }

    suspend fun saveBackendIp(ip: String) {
        dataStore.edit { preferences ->
            preferences[BACKEND_IP_KEY] = ip
        }
    }
    suspend fun getBackendIp(): String? {
        return dataStore.data.map { preferences ->
            preferences[BACKEND_IP_KEY]
        }.firstOrNull()
    }

    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_TOKEN] = username
        }
    }
    suspend fun getUsername(): String? {
        return dataStore.data.map { preferences ->
            preferences[USERNAME_TOKEN]
        }.firstOrNull()
    }

    suspend fun savePassword(password: String) {
        dataStore.edit { preferences ->
            preferences[PASSWORD_TOKEN] = password
        }
    }
    suspend fun getPassword(): String? {
        return dataStore.data.map { preferences ->
            preferences[PASSWORD_TOKEN]
        }.firstOrNull()
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")