package com.theft_preventer.app.di

import android.content.Context
import com.theft_preventer.app.data.local.SettingsDataStore
import com.theft_preventer.app.data.remote.ApiService
import com.theft_preventer.app.data.repository.AuthRepository
import com.theft_preventer.app.data.repository.EventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Provides
    @Singleton
    fun provideApiService(
        client: HttpClient,
        settingsDataStore: SettingsDataStore
    ): ApiService {
        return ApiService(client, settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService, settingsDataStore: SettingsDataStore): AuthRepository {
        return AuthRepository(apiService, settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService,
        authRepository: AuthRepository
    ): EventRepository {
        return EventRepository(apiService, authRepository)
    }
}