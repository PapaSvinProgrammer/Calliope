package com.mordva.network.interceptor

import com.mordva.network.provider.OauthTokenProvider
import okhttp3.Interceptor
import okhttp3.Response

internal class OAuthInterceptor(
    private val oauthTokenProvider: OauthTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        request = request.newBuilder()
            .addHeader(CONTENT_TYPE, "application/json")
            .addHeader(OAUTH, oauthTokenProvider.provide())
            .build()

        return chain.proceed(request)
    }

    private companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val OAUTH = "OAuth"
    }
}