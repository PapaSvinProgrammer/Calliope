package com.mordva.network.api

import android.util.Log
import com.mordva.network.BuildConfig
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
}.onFailure { throwable ->
    Log.e(
        TAG,
        "safeExecute failed: ${throwable::class.simpleName} — ${throwable.message}",
        throwable,
    )
}


private const val TAG = "SAFE_EXECUTE"