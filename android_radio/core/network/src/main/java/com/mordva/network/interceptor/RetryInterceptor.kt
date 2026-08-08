package com.mordva.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class RetryInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        var previousResponse: Response? = null
        var lastException: IOException? = null

        for (attempt in 0..DEFAULT_MAX_RETRIES) {
            try {
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
        const val HTTP_SERVICE_UNAVAILABLE = 503

        const val HTTP_SERVER_ERROR_RANGE_START = 500
        const val HTTP_SERVER_ERROR_RANGE_END = 599
        val httpErrorsRange = listOf(
            HTTP_SERVER_ERROR_RANGE_START..HTTP_SERVER_ERROR_RANGE_END,
            HTTP_SERVICE_UNAVAILABLE,
        )

        const val DEFAULT_MAX_RETRIES = 3
    }
}
