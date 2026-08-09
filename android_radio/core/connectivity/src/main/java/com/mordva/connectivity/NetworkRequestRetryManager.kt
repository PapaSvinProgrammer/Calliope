package com.mordva.connectivity

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NetworkRequestRetryManager(
    networkMonitor: NetworkMonitor,
    appScope: CoroutineScope,
) {
    private val pendingRequests = ConcurrentHashMap<String, suspend () -> Unit>()

    init {
        appScope.launch {
            networkMonitor.isOnline
                .filter { it }
                .collect { retryPendingRequests() }
        }
    }

    suspend fun <T> execute(
        key: String,
        request: suspend () -> Result<T>,
    ): RetriableRequest<T> {
        val updates = Channel<Result<T>>(Channel.BUFFERED)
        val operation: suspend () -> Unit = {
            request().also { result ->
                updates.send(result)
                if (result.isSuccess) {
                    pendingRequests.remove(key)
                    updates.close()
                }
            }
        }

        val initialResult = request()
        if (initialResult.exceptionOrNull() is IOException) {
            pendingRequests[key] = operation
        } else {
            updates.close()
        }

        return RetriableRequest(
            initialResult = initialResult,
            retryResults = updates.receiveAsFlow(),
        )
    }

    fun cancel(key: String) {
        pendingRequests.remove(key)
    }

    private suspend fun retryPendingRequests() {
        pendingRequests.entries.toList().forEach { (_, operation) -> operation() }
    }
}
