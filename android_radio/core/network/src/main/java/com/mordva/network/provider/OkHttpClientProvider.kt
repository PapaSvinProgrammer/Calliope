package com.mordva.network.provider

import com.mordva.network.BuildConfig
import com.mordva.network.interceptor.OAuthInterceptor
import com.mordva.network.interceptor.RetryInterceptor
import com.mordva.network.interceptor.SSLErrorHandleInterceptor
import com.mordva.network.api.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class OkHttpClientProvider(
    private val oauthTokenProvider: OauthTokenProvider,
    private val tokenAuthenticator: TokenAuthenticator,
) {
    private val sslErrorHandleInterceptor by lazy { SSLErrorHandleInterceptor() }
    private val httpLoggingInterceptor by lazy { createHttpLoggingInterceptor() }
    private val oauthInterceptor by lazy { OAuthInterceptor(oauthTokenProvider) }
    private val retryInterceptor by lazy { RetryInterceptor() }

    fun provide(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(sslErrorHandleInterceptor)
            .addInterceptor(oauthInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    private fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLogInterceptor = HttpLoggingInterceptor()

        val logLevel = if (BuildConfig.IS_DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }

        httpLogInterceptor.setLevel(logLevel)
        return httpLogInterceptor
    }
}