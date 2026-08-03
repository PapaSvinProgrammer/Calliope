package com.mordva.sdk.api

import com.mordva.auth_sdk.BuildConfig
import com.mordva.datastore.api.provider.TokenPreferencesProvider
import com.mordva.sdk.impl.AuthRepository
import com.mordva.sdk.impl.AuthSdkImpl
import com.mordva.sdk.impl.AuthService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

object AuthSdkProvider {
    private const val JSON_MEDIA_TYPE = "application/json"

    @Volatile
    private var instance: AuthSdk? = null

    fun provide(config: AuthSdkConfig): AuthSdk =
        instance ?: synchronized(this) {
            instance ?: createSdk(config).also { instance = it }
        }

    private fun createSdk(config: AuthSdkConfig): AuthSdk {
        val okHttpClient = provideOkHttpClient(config)
        val retrofit = provideRetrofit(config, okHttpClient)
        val tokenPreferencesRepository = TokenPreferencesProvider.provide(config.context)

        return AuthSdkImpl(
            authRepository = AuthRepository(
                authService = retrofit.create<AuthService>(),
                tokenPreferencesRepository = tokenPreferencesRepository,
            ),
        )
    }

    private fun provideOkHttpClient(config: AuthSdkConfig): OkHttpClient =
        config.httpClient.newBuilder()
            .addInterceptor(createHttpLoggingInterceptor())
            .build()

    private fun provideRetrofit(
        config: AuthSdkConfig,
        okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(config.apiUrl)
        .addConverterFactory(
            config.json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()),
        )
        .client(okHttpClient)
        .build()

    private fun createHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.IS_DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}