package com.mordva.network.interceptor

import com.mordva.network.provider.OauthTokenProvider
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class RetryInterceptor(
    private val oauthTokenProvider: OauthTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        var previousResponse: Response? = null
        var lastException: IOException? = null

        for (attempt in 0..DEFAULT_MAX_RETRIES) {
            try {
                val request = buildRequestForAttempt(
                    originalRequest = originalRequest,
                    previousResponse = previousResponse,
                    attempt = attempt,
                )

                previousResponse?.close()

                val response = chain.proceed(request)
                previousResponse = response

                if (shouldReturnResponse(response, attempt)) {
                    return response
                }
            } catch (exception: IOException) {
                lastException = exception

                if (isLastAttempt(attempt)) {
                    throw exception
                }
            }
        }

        previousResponse?.close()

        throw lastException ?: IOException("Request failed after $DEFAULT_MAX_RETRIES retries")
    }

    private fun buildRequestForAttempt(
        originalRequest: Request,
        previousResponse: Response?,
        attempt: Int,
    ): Request {
        if (!shouldRefreshToken(previousResponse, attempt)) {
            return originalRequest
        }

        oauthTokenProvider.update()

        return originalRequest.newBuilder()
            .header(OAUTH_HEADER, oauthTokenProvider.provide())
            .build()
    }

    private fun shouldRefreshToken(
        previousResponse: Response?,
        attempt: Int,
    ): Boolean {
        return attempt > FIRST_ATTEMPT && previousResponse?.code == HTTP_UNAUTHORIZED
    }

    private fun shouldReturnResponse(
        response: Response,
        attempt: Int,
    ): Boolean {
        return !shouldRetry(response.code, attempt)
    }

    private fun shouldRetry(
        responseCode: Int,
        attempt: Int,
    ): Boolean {
        if (isLastAttempt(attempt)) return false
        return responseCode in httpErrorsRange
    }

    private fun isLastAttempt(attempt: Int): Boolean {
        return attempt == DEFAULT_MAX_RETRIES
    }

    private companion object {
        const val FIRST_ATTEMPT = 0

        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_SERVICE_UNAVAILABLE = 503

        const val HTTP_CLIENT_ERROR_RANGE_START = 400
        const val HTTP_CLIENT_ERROR_RANGE_END = 499
        val httpErrorsRange = listOf(
            HTTP_CLIENT_ERROR_RANGE_START..HTTP_CLIENT_ERROR_RANGE_END,
            HTTP_SERVICE_UNAVAILABLE,
        )

        const val DEFAULT_MAX_RETRIES = 3
        const val OAUTH_HEADER = "OAuth"
    }
}
