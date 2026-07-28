package com.mordva.network.api

import com.mordva.network.provider.OauthTokenProvider
import com.mordva.network.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authRepository: AuthRepository,
    private val oauthTokenProvider: OauthTokenProvider,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_RETRIES) {
            return null
        }

        val refreshResult = runBlocking { authRepository.refreshToken() }
        if (refreshResult.isFailure) {
            return null
        }

        val newToken = oauthTokenProvider.token.value

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