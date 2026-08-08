package com.mordva.network.interceptor

import com.mordva.sdk.api.AuthSdk
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

internal class OAuthInterceptor(
    private val authSdk: AuthSdk,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val authToken = runBlocking { authSdk.getToken() }

        authToken.onSuccess {
            request = request.newBuilder()
                .addHeader(CONTENT_TYPE, "application/json")
                .addHeader(OAUTH, it)
                .build()
        }

        return chain.proceed(request)
    }

    private companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val OAUTH = "OAuth"
    }
}