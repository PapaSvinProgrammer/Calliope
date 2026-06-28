package com.mordva.network.provider

import com.mordva.network.interceptor.OAuthInterceptor
import com.mordva.network.interceptor.RetryInterceptor
import okhttp3.OkHttpClient

object OkHttpClientProvider {
    private val oauthTokenProvider by lazy { OauthTokenProvider() }
    private val oauthInterceptor by lazy { OAuthInterceptor(oauthTokenProvider) }
    private val retryInterceptor by lazy { RetryInterceptor(oauthTokenProvider) }
    private val client by lazy { createOkHttpClient() }

    fun provide(): OkHttpClient = client

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(oauthInterceptor)
            .addInterceptor(retryInterceptor)
            .build()
    }
}