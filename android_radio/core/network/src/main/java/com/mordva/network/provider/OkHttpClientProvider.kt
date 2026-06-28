package com.mordva.network.provider

import com.mordva.network.BuildConfig
import com.mordva.network.interceptor.OAuthInterceptor
import com.mordva.network.interceptor.RetryInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object OkHttpClientProvider {
    private val oauthTokenProvider by lazy { OauthTokenProvider() }
    private val client by lazy { createOkHttpClient() }

    private val httpLoggingInterceptor by lazy { createHttpLoggingInterceptor() }
    private val oauthInterceptor by lazy { OAuthInterceptor(oauthTokenProvider) }
    private val retryInterceptor by lazy { RetryInterceptor(oauthTokenProvider) }

    fun provide(): OkHttpClient = client

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(oauthInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(httpLoggingInterceptor)
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