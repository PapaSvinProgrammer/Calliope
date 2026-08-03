package com.mordva.network.api

import com.mordva.sdk.api.AuthSdk
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

internal class TokenAuthenticator(
    private val authSdk: AuthSdk,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_RETRIES) {
            return null
        }

        val refreshResult = runBlocking { authSdk.refreshToken() }

        if (refreshResult.isFailure) {
            return null
        }

        val newToken = runBlocking { authSdk.getToken() }.getOrNull().orEmpty()

        return response.request.newBuilder()
            .header(OAUTH, newToken)
            .build()
    }

    private fun responseCount(response: Response): Int {
        var current = response
        var count = 1
        while (current.priorResponse != null) {
            count++
            current = current.priorResponse!!
        }
        return count
    }

    private companion object {
        const val OAUTH = "OAuth"
        const val MAX_RETRIES = 2
    }
}