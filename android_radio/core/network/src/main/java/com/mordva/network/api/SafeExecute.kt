package com.mordva.network.api

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T : Any> safeExecute(
    block: suspend () -> Response<T>,
): Result<T> = runCatching {
    val response = block()

    if (response.isSuccessful) {
        response.body() ?: error("Response body is null for ${response.raw().request.url}")
    } else {
        throw HttpException(response)
    }
}
